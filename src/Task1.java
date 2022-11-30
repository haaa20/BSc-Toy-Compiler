import SimpleLang.*;
import org.antlr.v4.runtime.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Task1 {
    public static void main(String[] args) throws Exception {
        // Reading the input
        // For as long as I'm just getting my head around this I'll just do a text file input
        CharStream inputProg = CharStreams.fromFileName("src/testInput");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        // Black box here
        // SimpleLang.g4 goes in, a parser tree comes out
        SimpleLangLexer lexer = new SimpleLangLexer(inputProg);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SimpleLangParser parser = new SimpleLangParser(tokens);
        SimpleLangParser.ProgContext tree = parser.prog();

        if (tree.exception != null) {
            System.err.println("You're input isn't grammatically correct");
            return;
        }

        // Constructing a listener and walking through the parser tree
        // Checking if the input is a valid program
        System.out.println("Please enter your program for compilation below:");

        try {
            Task1Checker visitor = new Task1Checker();
            visitor.visit(tree);
        } catch (TypeException ex) {
            System.err.printf("ERROR \n -- %s%n", ex.report());
            System.out.println("\tThe program you have entered cannot compile correctly,");
            System.out.println("\tplease fix and try again");
            return;
        }

        // If we get this far, then we can start simulating the program
        System.out.println("Program compiled successfully, please enter your input(s) below:");
        Task1Simulator simulator = new Task1Simulator();
        String[] inputArgs;
        simulator.visit(tree);
        inputArgs = reader.readLine().split("\s+|(,\s*)");
        simulator.runMain(inputArgs);
    }
}
