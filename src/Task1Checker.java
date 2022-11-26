import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;

public class Task1Checker extends SimpleLangBaseVisitor<SLType> {
    private static class Evaluate {
        // A simple function to evaluate a TYPE node
        static SLType typeOf(ParseTree ctx) {
            return switch (ctx.getText()) {
                case "int" -> SLType.INT;
                case "bool" -> SLType.BOOL;
                case "unit" -> SLType.UNIT;
                default -> null;
            };
        }
    }

    ScopeStack<SLType> scopeStack;

    public Task1Checker() {
        this.scopeStack = new ScopeStack<>();
    }

    @Override
    public SLType visitIdentifier(SimpleLangParser.IdentifierContext ctx) {
        // The variable name must exist at a scope layer higher than global...
        // as only functions are scoped globally
        String varName = ctx.getText();

        if (!scopeStack.slice(1).containsKey(varName)) {
            throw new TypeException().undefinedVarError();
        }
        return scopeStack.get(varName);
    }

    @Override
    public SLType visitProg(SimpleLangParser.ProgContext ctx) {
        // Iterate through every function declaration, and add them to the scope
        // We want to do this FIRST as the order in which functions are declared should not matter
        // to any of the blocks of code
        for (SimpleLangParser.DecContext dec : ctx.dec()) {
            addFunction(
                    Evaluate.typeOf(dec.type()),
                    dec.IDFR().getText()
            );
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
        return scopeStack.get(funcId);
    }

    @Override
    public SLType visitBody(SimpleLangParser.BodyContext ctx) {
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

            // Adding the var to the scope
            scopeStack.put(varName, varType);
        }

        super.visitBody(ctx);
        scopeStack.popScope();
        return SLType.UNIT;
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
        SLType rightExp = visitChildren(ctx.exp());

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
