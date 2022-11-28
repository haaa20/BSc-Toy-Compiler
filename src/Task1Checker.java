import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collection;
import java.util.HashMap;

public class Task1Checker extends SimpleLangBaseVisitor<SLType> {
    private static class Evaluate {
        // A simple function to evaluate a TYPE node
        static SLType typeOf(SimpleLangParser.TypeContext ctx) {
            return switch (ctx.getText()) {
                case "int" -> SLType.INT;
                case "bool" -> SLType.BOOL;
                case "unit" -> SLType.UNIT;
                default -> null;
            };
        }

        private static boolean symbolIsIn(String sy, String symbols) {
            for (String s : symbols.split("\s")) {
                if (s.equals(sy)) {return true;}
            }
            return false;
        }

        static SLType[] generateSignature(SimpleLangParser.VardecContext ctx) {
            int expectedNo = ctx.IDFR().size();
            SLType[] expectedTypes = new SLType[expectedNo];

            for (int i = 0; i < expectedNo; i++) {
                expectedTypes[i] = Evaluate.typeOf(ctx.type(i));
            }

            return expectedTypes;
        }

        static boolean isCompOp(String op) {
            return symbolIsIn(op, "== < > <= >=");
        }

        static boolean isIntOp(String op) {
            return symbolIsIn(op, "+ - * /");
        }

        static boolean isBoolOp(String op) {
            return symbolIsIn(op, "& | ^");
        }
    }

    ScopeStack<SLType> scopeStack;
    HashMap<String, SLType[]> signature;

    public Task1Checker() {
        this.scopeStack = new ScopeStack<>();
        this.signature = new HashMap<>();
    }

    @Override
    public SLType visitProg(SimpleLangParser.ProgContext ctx) {
        // Iterate through every function declaration, and add them to the scope
        // We want to do this FIRST as the order in which functions are declared should not matter
        // to any of the blocks of code
        SLType funcType;
        String funcName;
        for (SimpleLangParser.DecContext dec : ctx.dec()) {
            funcType = Evaluate.typeOf(dec.type());
            funcName = dec.IDFR().getText();
            addFunction(funcType, funcName);

            signature.put(funcName, Evaluate.generateSignature(dec.vardec()));
        }

        if (!scopeStack.getGlobal().contains("main")) {
            throw new TypeException().noMainFuncError();
        }
        else if (scopeStack.get("main") != SLType.INT) {
            throw new TypeException().mainReturnTypeError();
        }

        visitChildren(ctx);
        return SLType.UNIT;
    }

    @Override
    public SLType visitDec(SimpleLangParser.DecContext ctx) {
        // IF YOU'RE WONDERING WHY THERE'S NO SCOPE STACK HASHING
        // ...remember we already did that in visitProg()
        // As we are entering a function, push a new layer of scope
        scopeStack.pushScope();
        visitChildren(ctx);
        // Once we have finished traversing the function sub-tree, pop off the scoped variables
        scopeStack.popScope();
        return SLType.UNIT;
    }

    @Override
    public SLType visitVardec(SimpleLangParser.VardecContext ctx) {
        // We should have just entered a function, so we will be fresh on a new layer of scope
        for (int i = 0; i < ctx.IDFR().size(); i++) {
            scopeStack.put(ctx.IDFR(i).getText(), Evaluate.typeOf(ctx.type(i)));
        }

        return SLType.UNIT;
    }

    @Override
    public SLType visitFuncCall(SimpleLangParser.FuncCallContext ctx) {
        String funcId = ctx.IDFR().getText();

        // Checking for unknown function names
        if (!scopeStack.globalContains(funcId)) {
            throw new TypeException().undefinedFuncError();
        }

        // Checking there are the correct number/types of parameters
        SLType[] expected = signature.get(funcId);
        SimpleLangParser.ArgsContext given = ctx.args();
        if (given.exp().size() != expected.length) {
            throw new TypeException().argumentNumberError();
        }
        for (int i = 0; i < expected.length; i++) {
            if (visit(given.exp(i)) != expected[i]) {
                throw new TypeException().argumentError();
            }
        }
        return scopeStack.get(funcId);
    }



    @Override
    public SLType visitOperation(SimpleLangParser.OperationContext ctx) {
        // This one's structure is slightly unintuitive but bare with us
        String symbol = ctx.binop().getText();
        SLType expected = SLType.INT;
        SLType operandA, operandB;
        operandA = visit(ctx.exp(0));
        operandB = visit(ctx.exp(1));

        // We should be expecting two integers, unless we are doing a boolean operation
        if (Evaluate.isCompOp(symbol)) {
            if (operandA != expected || operandB != expected) {
                throw new TypeException().comparisonError();
            }

            return SLType.BOOL;
        }
        else if (Evaluate.isIntOp(symbol)) {
            if (operandA != expected || operandB != expected) {
                throw new TypeException().arithmeticError();
            }

            return SLType.INT;
        }
        else {
            expected = SLType.BOOL;
            if (operandA != expected || operandB != expected) {
                throw new TypeException().logicalError();
            }

            return SLType.BOOL;
        }
    }

    @Override
    public SLType visitConditional(SimpleLangParser.ConditionalContext ctx) {
        // Check that the condition itself evaluates to a boolean
        if (!visit(ctx.exp()).equals(SLType.BOOL)) {
            throw new TypeException().conditionError();
        }

        // Check that the first and second codeBlocks (the if and else respectively) return the same
        // type
        // If they do, this is the type we want to return, otherwise throw an error
        SLType expected = visit(ctx.block(0));

        if (!expected.equals(visit(ctx.block(1)))) {
            throw new TypeException().branchMismatchError();
        }

        return expected;
    }

    @Override
    public SLType visitWhileLoop(SimpleLangParser.WhileLoopContext ctx) {
        if (!visit(ctx.exp()).equals(SLType.BOOL)) {
            throw new TypeException().conditionError();
        }

        visitLoopBlock(ctx.block());

        return super.visitWhileLoop(ctx);
    }

    @Override
    public SLType visitUntilLoop(SimpleLangParser.UntilLoopContext ctx) {
        if (!visit(ctx.exp()).equals(SLType.BOOL)) {
            throw new TypeException().conditionError();
        }

        visitLoopBlock(ctx.block());

        return super.visitUntilLoop(ctx);
    }

    @Override
    public SLType visitBody(SimpleLangParser.BodyContext ctx) {
        // Push a new layer to the scope stack
        scopeStack.pushScope();

        // The sequence (type IDFR ':=' exp ';') will always repeat n number of times, so we can iterate through
        // any one of them and access all the others
        for (int i = 0; i < ctx.IDFR().size(); i++) {
            SLType varType = Evaluate.typeOf(ctx.type(i));
            String varName = ctx.IDFR(i).getText();
            // Checking if there is collision between a var name and the functions, or the other vars
            // currently in scope
            if (scopeStack.containsKey(varName)) {
                if (scopeStack.globalContains(varName)) {
                    throw new TypeException().clashedVarError();
                }
                else {
                    throw new TypeException().duplicatedVarError();
                }
            }

            // Checking the value our var is being assigned to is the right type
            if (varType != visit(ctx.exp(i))) {
                throw new TypeException().assignmentError();
            }

            // Adding the var to the scope
            scopeStack.put(varName, varType);
        }

        visit(ctx.ene());
        scopeStack.popScope();
        return SLType.UNIT;
    }

    @Override
    public SLType visitBlock(SimpleLangParser.BlockContext ctx) {
        return visit(ctx.ene());
    }

    public SLType visitLoopBlock(SimpleLangParser.BlockContext ctx) {
        int lastExpIdx = ctx.ene().exp().size() - 1;
        if (visit(ctx.ene().exp(lastExpIdx)) != SLType.UNIT) {
            throw new TypeException().loopBodyError();
        }
        return visitBlock(ctx);
    }

    @Override
    public SLType visitIdentifier(SimpleLangParser.IdentifierContext ctx) {
        // The variable name must exist at a scope layer higher than global...
        // as only functions are scoped globally
        String varName = ctx.getText();

        if (!scopeStack.slice(1).containsKey(varName)) {
            System.out.println(ctx.getText());
            throw new TypeException().undefinedVarError();
        }
        return scopeStack.get(varName);
    }

    @Override
    public SLType visitAnInt(SimpleLangParser.AnIntContext ctx) {
        return SLType.INT;
    }

    @Override
    public SLType visitABool(SimpleLangParser.ABoolContext ctx) {
        System.out.println("Beep");
        return SLType.BOOL;
    }

    @Override
    public SLType visitAssignment(SimpleLangParser.AssignmentContext ctx) {
        String varName = ctx.IDFR().getText();

        // Checking to see if the varName does not exist OR if it actually refers to a function
        // Either way - it's an error
        if (!scopeStack.containsKey(varName) || scopeStack.globalContains(varName)) {
            throw new TypeException().undefinedVarError();
        }

        // Evaluate the expression to the right
        SLType rightExp = visit(ctx.exp());

        // Checking the value our var is being assigned to is the right type
        if (scopeStack.get(varName) != rightExp) {
            throw new TypeException().assignmentError();
        }

        return SLType.UNIT;
    }

    @Override
    public SLType visitType(SimpleLangParser.TypeContext ctx) {
        // Check for variables of type unit, which is not allowed
        if (!(ctx.getParent() instanceof SimpleLangParser.DecContext)
                && Evaluate.typeOf(ctx) == SLType.UNIT) {
            throw new TypeException().unitVarError();
        }

        // This should always be a unit
        return SLType.UNIT;
    }

    private void addFunction(SLType funcType, String funcId) {
        // Error check for duplicate function
        if (scopeStack.globalContains(funcId)) {
            throw new TypeException().duplicatedFuncError();
        }

        // At this point it SHOULDN'T matter whether we do globalPut() or just put()
        // But I'm using globalPut() just to be safe for now
        scopeStack.globalPut(funcId, funcType);
    }
}
