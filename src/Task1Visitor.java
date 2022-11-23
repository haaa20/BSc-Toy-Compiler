import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class Task1Visitor extends SimpleLangBaseVisitor<SLType> {
    ScopeStack scopeStack;

    // A simple function to return the type of a TYPE node
    private SLType typeOf(ParseTree ctx) {
        return switch (ctx.getText()) {
            case "int" -> SLType.INT;
            case "bool" -> SLType.BOOL;
            case "unit" -> SLType.UNIT;
            default -> null;
        };
    }
    
    @Override
    public SLType visit(ParseTree tree) {
        // Instancing any fields we will need to run the analysis, and traversing the tree
        scopeStack = new ScopeStack();
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
    public SLType visitDec(SimpleLangParser.DecContext ctx) {
        SLType funcType = typeOf(ctx.type());
        String funcId = ctx.IDFR().getText();

        // Error checks
        if (scopeStack.globalContains(funcId)) {
            throw new TypeException().duplicatedFuncError();
        }

        // At this point it SHOULDN'T matter whether we do globalPut() or just put()
        // But I'm using globalPut() just to be safe for now
        scopeStack.globalPut(funcId, funcType);
        return super.visitDec(ctx);
    }

}
