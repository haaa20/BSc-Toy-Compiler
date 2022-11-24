import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;

public class Task1Visitor extends SimpleLangBaseVisitor<SLType> {
    ScopeStack<SLType> scopeStack;

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

    @Override
    public SLType visit(ParseTree tree) {
        // Instancing any fields we will need to run the analysis, and traversing the tree
        scopeStack = new ScopeStack<SLType>();
        SLType result = super.visit(tree);

        if (!scopeStack.getGlobal().contains("main")) {
            throw new TypeException().noMainFuncError();
        }
        if (scopeStack.get("main") != SLType.INT) {
            throw new TypeException().mainReturnTypeError();
        }

        return result;
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
    public SLType visitOperation(SimpleLangParser.OperationContext ctx) {
        SimpleLangParser.BinopContext op = ctx.binop();
        if (op.COMPOP() != null) {

        }
        else if (op.INTOP() != null) {

        }
        else { // if (op.BOOLOP() != null)

        }

        return super.visitOperation(ctx);
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
    public SLType visitDec(SimpleLangParser.DecContext ctx) {
        SLType funcType = Evaluate.typeOf(ctx.type());
        String funcId = ctx.IDFR().getText();

        // Error check for duplicate function
        if (scopeStack.globalContains(funcId)) {
            throw new TypeException().duplicatedFuncError();
        }

        // At this point it SHOULDN'T matter whether we do globalPut() or just put()
        // But I'm using globalPut() just to be safe for now
        scopeStack.globalPut(funcId, funcType);
        return super.visitDec(ctx);
    }

}
