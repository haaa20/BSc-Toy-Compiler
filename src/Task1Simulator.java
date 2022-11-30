import SimpleLang.SLObject;
import SimpleLang.SLType;
import SimpleLang.SimpleLangBaseVisitor;
import SimpleLang.SimpleLangParser;

import java.util.HashMap;

public class Task1Simulator extends SimpleLangBaseVisitor<SLObject> {

    private class FuncWrapper {
        String[] params;
        SimpleLangParser.BodyContext body;

        public FuncWrapper(String[] params, SimpleLangParser.BodyContext body) {
            this.params = params;
            this.body = body;
        }

        public SLObject run(SLObject[] args) {
            // here each element in the args array should pam to the one in the parameter
            HashMap<String, SLObject> vars = new HashMap<>();

            for (int i = 0; i < body.IDFR().size(); i++) {
                vars.put(body.IDFR(i).getText(), visit(body.exp(i)));
            }

            return null;
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

    public Task1Simulator() {
        this.functions = new HashMap<>();
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
    public SLObject visitAnInt(SimpleLangParser.AnIntContext ctx) {
        int value = Integer.parseInt(ctx.getText());
        return new SLObject(value);
    }

    @Override
    public SLObject visitABool(SimpleLangParser.ABoolContext ctx) {
        if (ctx.getText() == "true") {
            return new SLObject(true);
        }
        return new SLObject(false);
    }

    @Override
    public SLObject visitOperation(SimpleLangParser.OperationContext ctx) {
        // Yikes this one's gonna take some doin'
        return super.visitOperation(ctx);
    }

    @Override
    public SLObject visitFuncCall(SimpleLangParser.FuncCallContext ctx) {
        SLObject[] args = evaluateArgs(ctx.args());
        FuncWrapper f = functions.get(ctx.IDFR().getText());
        return f.run(args);
    }

    public void runMain(String[] rawArgs) {
        FuncWrapper main;
        main = functions.get("main");

        // main.run(args);
    }

    // For when we a passing the inputs as text (ie for the main function)
    private SLObject[] evaluateArgs(String[] rawArgs) {
        SLObject[] args = new SLObject[rawArgs.length];

        for (int i = 0; i < args.length; i++) {

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
