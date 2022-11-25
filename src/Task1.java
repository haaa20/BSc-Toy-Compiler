import SimpleLang.*;
import org.antlr.v4.runtime.*;

public class Task1 {
    public static void main(String[] args) throws Exception{
        // Reading the input
        // For as long as I'm just getting my head around this I'll just do a text file input
        CharStream input = CharStreams.fromFileName("src/testInput");
        
        // Black box here
        // SimpleLang.g4 goes in, a parser tree comes out
        SimpleLangLexer lexer = new SimpleLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SimpleLangParser parser = new SimpleLangParser(tokens);
        SimpleLangParser.ProgContext tree = parser.prog();

        // Constructing a listener and walking through the parser tree
        try {
            Task1Checker visitor = new Task1Checker();
            visitor.visit(tree);
        } catch (TypeException ex) {
            System.out.println(String.format("ERROR \n -- %s", ex.report()));
        }

    }
}
