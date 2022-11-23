import SimpleLang.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;

/** @noinspection SpellCheckingInspection*/
public class Task1Visitor extends SimpleLangBaseVisitor<SLType> {
    Set<String> functions;
    HashMap<String, SLType> scopedIdfr;
    Stack<Set<String>> scopeStack;

    private void pushScope(HashMap<String, SLType> newIdfrs) {
        // Remember the stack only references the IDFRs...
        // Only the hash map links IDFRs to their types
        for (String idfr : newIdfrs.keySet()) {
            // Check for collision between var and func names
            if (functions.contains(idfr)) {
                throw new TypeException().clashedVarError();
            }
            // Check for collision between var names
            else if (scopedIdfr.containsKey(idfr)) {
                throw new TypeException().duplicatedVarError();
            }
            else {
                scopedIdfr.put(idfr, newIdfrs.get(idfr));
            }
        }
        scopeStack.push(newIdfrs.keySet());
    }

    private void popScope() {
        // Pop the toppermost set of idfrs and remove them from the current scope
        Set<String> popped = scopeStack.pop();
        for (String id : popped) {
            scopedIdfr.remove(id);
        }
    }

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
        // Instancing any fields we will need to run the analysis
        functions = new HashSet<>();
        scopedIdfr = new HashMap<>();

        // PRE TRAVERSAL
        SLType result = super.visit(tree);
        // POST TRAVERSAL

        // Checking for the main function
        if (scopedIdfr.get("main") != SLType.INT) {
            // If the main function is missing
            if (!scopedIdfr.containsKey("main")) {
                throw new TypeException().noMainFuncError();
            }
            // If the main function returns the wrong type
            else {
                throw new TypeException().mainReturnTypeError();
            }
        }

        return result;
    }

    @Override
    public SLType visitDec(SimpleLangParser.DecContext ctx) {
        String funcName = ctx.IDFR().getText();
        SLType funcType = typeOf(ctx.type());
        HashMap<String, SLType> scope = new HashMap<>();

        // Check for duplicate function names
        if (functions.contains(funcName)) {
            throw new TypeException().duplicatedFuncError();
        }

        // Iterate through every PARAM idfr-type pair and add them to this function's scope
        for (int i = 0; i < ctx.vardec().IDFR().size(); i++) {
            String idfr = ctx.vardec().IDFR(i).getText();
            SLType type = typeOf(ctx.vardec().type(i));
            scope.put(idfr, type);
        }
        // Iterate through every


        return super.visitDec(ctx);
    }

}
