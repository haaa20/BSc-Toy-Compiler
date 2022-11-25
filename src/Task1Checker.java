import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;

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

        return super.visitProg(ctx);
    }

    @Override
    public SLType visitFuncCall(SimpleLangParser.FuncCallContext ctx) {
        // Checking for unknown function names
        if (!scopeStack.globalContains(ctx.IDFR().getText())) {
            throw new TypeException().undefinedFuncError();
        }
        return super.visitFuncCall(ctx);
    }

    @Override
    public SLType visitBody(SimpleLangParser.BodyContext ctx) {
        HashMap<String, SLType> bodyScope = new HashMap<>();

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
            bodyScope.put(varName, varType);
        }

        scopeStack.pushScope(bodyScope);
        SLType visit = super.visitBody(ctx);
        scopeStack.popScope();
        return visit;
    }

    @Override
    public SLType visitAssignment(SimpleLangParser.AssignmentContext ctx) {
        String varName = ctx.IDFR().getText();

        // Checking to see if the varName does not exist OR if it actually refers to a function
        // Either way - it's an error
        if (!scopeStack.containsKey(varName) || scopeStack.globalContains(varName)) {
            throw new TypeException().undefinedVarError();
        }

        return super.visitAssignment(ctx);
    }

    @Override
    public SLType visitType(SimpleLangParser.TypeContext ctx) {
        // Check for variables of type unit, which is not allowed
        if (!(ctx.getParent() instanceof SimpleLangParser.DecContext)
                && Evaluate.typeOf(ctx) == SLType.UNIT) {
            throw new TypeException().unitVarError();
        }

        return super.visitType(ctx);
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
