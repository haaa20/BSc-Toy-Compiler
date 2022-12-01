import SimpleLang.SLObject;
import SimpleLang.SLType;
import SimpleLang.SimpleLangBaseVisitor;
import SimpleLang.SimpleLangParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Iterator;

public class Task1Simulator extends SimpleLangBaseVisitor<SLObject> {

    private class FuncWrapper {
        String[] params;
        SimpleLangParser.BodyContext body;

        public FuncWrapper(String[] params, SimpleLangParser.BodyContext body) {
            this.params = params;
            this.body = body;
        }

        public SLObject run(SLObject[] args) {
            // here each element in the args array should map to the one in the parameter
            // Also, each function should push it a new layer to scopeStack,
            // and pop it before returning
            SimpleLangParser.ExpContext exp = null;
            SLObject output;
            scopeStack.pushScope();

            // map the value of each arg to the parameter name declared in the signature
            // which, remember, we stored in an array
            for (int i = 0; i < params.length; i++) {
                scopeStack.put(params[i], args[i]);
            }

            // Now we add the variables declared in the body itself to the hash map
            for (int i = 0; i < body.IDFR().size(); i++) {
                scopeStack.put(body.IDFR(i).getText(), visit(body.exp(i)));
            }

            // Finally, visit the body itself, before popping the scope
            output = visitEne(body.ene());
            scopeStack.popScope();
            return output;
        }
    }

    //    This is a simulation
    //    This is for demonstration
    //    This is a lonely illusion
    //    This is my only delusion
    //    This is the realm of my wildest dreams
    //    These are my wildest dreams
    //    If it's all on my face
    //    It's all in my mind
    //    You don't get to be unkind

    HashMap<String, FuncWrapper> functions;
    ScopeStack<SLObject> scopeStack;
    String printOut;

    public Task1Simulator() {
        this.functions = new HashMap<>();
        this.scopeStack = new ScopeStack<>();
        this.printOut = "";
    }

    @Override
    public SLObject visitProg(SimpleLangParser.ProgContext ctx) {
        // Add all function context trees to a hash map
        // Then the only tree we should explicitly visit is the main one

        // Iterate through each function declaration
        for (SimpleLangParser.DecContext func: ctx.dec()) {
            int paramCount = func.vardec().IDFR().size();
            String funcName = func.IDFR().getText();
            String[] params = new String[paramCount];

            for (int i = 0; i < paramCount; i++) {
                params[i] = func.vardec().IDFR(i).getText();
            }

            functions.put(funcName, new FuncWrapper(params, func.body()));
        }

        return null;
        // As this is the root of the tree I think it's safe to return null
    }

    @Override
    public SLObject visitEne(SimpleLangParser.EneContext ctx) {
        // Iterate through each expression in the ene, returning
        // the value that the last one evaluates to
        Iterator<SimpleLangParser.ExpContext> lines = ctx.exp().iterator();
        SimpleLangParser.ExpContext exp = lines.next();

        while (lines.hasNext()) {
            visit(exp);
            exp = lines.next();
        }

        return visit(exp);
    }

    @Override
    public SLObject visitAnInt(SimpleLangParser.AnIntContext ctx) {
        int value = Integer.parseInt(ctx.getText());
        return new SLObject(value);
    }

    @Override
    public SLObject visitABool(SimpleLangParser.ABoolContext ctx) {
        if (ctx.getText().equals("true")) {
            return new SLObject(true);
        }
        return new SLObject(false);
    }

    @Override
    public SLObject visitAssignment(SimpleLangParser.AssignmentContext ctx) {
        String varName = ctx.IDFR().getText();
        SLObject exp = visit(ctx.exp());
        scopeStack.put(varName, exp);
        return new SLObject();
    }

    @Override
    public SLObject visitOperation(SimpleLangParser.OperationContext ctx) {
        // Yikes this one's gonna take some doing'
        Operator op = Operator.evaluate(ctx.binop().getText());
        SLObject a = visit(ctx.exp(0)); // The two operands
        SLObject b = visit(ctx.exp(1));
        SLObject c; // The rightmost term ie answer

        if (op == Operator.PLUS) {
            c = new SLObject(a.getValue() + b.getValue());
        }
        else if (op == Operator.MINUS) {
            c = new SLObject(a.getValue() - b.getValue());
        }
        else if (op == Operator.MULTI) {
            c = new SLObject(a.getValue() * b.getValue());
        }
        else if (op == Operator.DIV) {
            c = new SLObject(a.getValue() / b.getValue());
        }
        else if (op == Operator.AND) {
            c = new SLObject(a.getBool() && b.getBool());
        }
        else if (op == Operator.OR) {
            c = new SLObject(a.getBool() || b.getBool());
        }
        else if (op == Operator.XOR) {
            c = new SLObject(a.getBool() ^ b.getBool());
        }
        else if (op == Operator.EQ) {
            c = new SLObject(a.getValue() == b.getValue());
        }
        else if (op == Operator.LESS) {
            c = new SLObject(a.getValue() < b.getValue());
        }
        else if (op == Operator.MORE) {
            c = new SLObject(a.getValue() > b.getValue());
        }
        else if (op == Operator.LESSEQ) {
            c = new SLObject(a.getValue() <= b.getValue());
        }
        else if (op == Operator.MOREEQ) {
            c = new SLObject(a.getValue() >= b.getValue());
        }
        else {
            c = new SLObject();
        }

        return c;
    }

    @Override
    public SLObject visitBlock(SimpleLangParser.BlockContext ctx) {
        return visitEne(ctx.ene());
    }

    @Override
    public SLObject visitConditional(SimpleLangParser.ConditionalContext ctx) {
        Boolean conditionMet = visit(ctx.exp()).getBool();

        if (conditionMet) {
            return visitBlock(ctx.block(0));
        }
        else {
            return visitBlock(ctx.block(1));
        }
    }

    @Override
    public SLObject visitPrint(SimpleLangParser.PrintContext ctx) {
        SLObject out = visit(ctx.exp());

        if (out.getType() == SLType.INT) {
            printOut = printOut.concat(out.toString());
        }
        else if (ctx.exp().getText().equals("space")) {
            printOut = printOut.concat(" ");
        }
        else { // if == "newline"
            printOut = printOut.concat("\n");
        }

        return new SLObject();
    }

    @Override
    public SLObject visitFuncCall(SimpleLangParser.FuncCallContext ctx) {
        SLObject[] args = evaluateArgs(ctx.args());
        FuncWrapper f = functions.get(ctx.IDFR().getText());
        return f.run(args);
    }

    @Override
    public SLObject visitNewLine(SimpleLangParser.NewLineContext ctx) {
        return new SLObject();
    }

    @Override
    public SLObject visitSpace(SimpleLangParser.SpaceContext ctx) {
        return new SLObject();
    }

    @Override
    public SLObject visitSkip(SimpleLangParser.SkipContext ctx) {
        return new SLObject();
    }

    @Override
    public SLObject visitIdentifier(SimpleLangParser.IdentifierContext ctx) {
        return scopeStack.get(ctx.getText());
    }

    public void runMain(String[] rawArgs) {
        FuncWrapper main;
        main = functions.get("main");
        int output = main.run(evaluateArgs(rawArgs)).getValue();

        if (!printOut.isEmpty()) {
            System.out.println(printOut);
        }
        System.out.println("\nNORMAL_TERMINATION\n" + output);
    }

    // For when we a passing the inputs as text (ie for the main function)
    private SLObject[] evaluateArgs(String[] rawArgs) {
        SLObject[] args = new SLObject[rawArgs.length];

        for (int i = 0; i < args.length; i++) {
            if (rawArgs[i].equals("true")) {
                args[i] = new SLObject(true);
            }
            else if (rawArgs[i].equals("false")) {
                args[i] = new SLObject(false);
            }
            else try {
                args[i] = new SLObject(Integer.parseInt(rawArgs[i]));
            }
            catch (NumberFormatException e) {
                args[i] = new SLObject();
            }
        }
        return args;
    }

    // For when we a passing the inputs as expressions
    private SLObject[] evaluateArgs(SimpleLangParser.ArgsContext rawArgs) {
        SLObject[] args = new SLObject[rawArgs.exp().size()];

        for (int i = 0; i < args.length; i++) {
            args[i] = visit(rawArgs.exp(i));
        }

        return args;
    }
}
