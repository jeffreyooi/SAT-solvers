package parser;

import datastruct.Clause;
import datastruct.Literal;
import db.ClauseDB;

/**
 * A DIMACS parser that parses to CNF object based on the content of the String.
 */
public class DimacsParser {

    private static final String COMMENT_TOKEN = "c";
    private static final String PROBLEM_TOKEN = "p";

    private ClauseDB db;

    private boolean hasAttribute = false;

    private int numberOfClauses;
    private int numberOfLiterals;

    /**
     * To keep track of how many variables are there. This does not store if the variable is positive or
     * negative.
     */

    public DimacsParser(ClauseDB db) {
        numberOfClauses = 0;
        numberOfLiterals = 0;
        this.db = db;
    }

    public boolean parse(String dimacsString) {
        String[] subStrings = dimacsString.trim().split("\\n");
        // If there is nothing, just return
        if (subStrings.length == 0) {
            return false;
        }

        for (String s : subStrings) {
            if (isCommentLine(s)) {
                continue;
            }
            if (isProblemLine(s)) {
                if (!hasAttribute) {
                    boolean parseResult = parseProblemLine(s);
                    if (!parseResult) {
                        return false;
                    }
                    hasAttribute = true;
                    continue;
                } else {
                    return false;
                }
            }
            // Tricky: to catch certain cases where the clause is marked end by 0 but does not go to new line
            String[] clauseStrings = s.trim().split("[ ][0]");

            if (clauseStrings.length == 1 && clauseStrings[0].equals("0")) {
                continue;
            }

            for (String clauseString : clauseStrings) {
                Clause clause = parseClause(clauseString);
                db.insertClause(clause);
            }
        }

        for (Clause c : db.getAllClauses()) {
            for (Literal l : c.getLiterals()) {
                System.out.print(String.format("%s%s ", l.isPositive() ? "" : "-", l.getName()));
            }
            System.out.println();
        }

        return db.getNumberOfClauses() == numberOfClauses && db.getNumberOfLiterals() == numberOfLiterals;
    }

    /**
     * Parse the problem line of DIMACS.
     * @param line problem line of DIMACS file.
     */
    private boolean parseProblemLine(String line) {
        String[] subStrings = line.trim().split("[\\s]+");

        // If the number of substrings is not 4, the format is invalid.
        if (subStrings.length != 4) {
            return false;
        }

        // Third and fourth indicates number of literals and number of clauses.
        try {
            numberOfLiterals = Integer.parseInt(subStrings[2]);
            numberOfClauses = Integer.parseInt(subStrings[3]);
            return true;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return false;
        }
    }

    private static Clause parseClause(String line) {
        // Trim whitespaces in front and back
        line = line.trim();
        // Split by spaces to get each literal
        String[] literalStrings = line.split("[\\s]+");
        if (literalStrings.length == 0) {
            return null;
        }
        Clause clause = new Clause();
        for (String literalString : literalStrings) {
            literalString = literalString.trim();
            Literal literal = parseLiteral(literalString);
            clause.addLiteral(literal);
        }
        return clause;
    }

    private static Literal parseLiteral(String string) {
        try {
            // Ensure that it is a valid format
            int valid = Integer.parseInt(string);
            return new Literal(String.valueOf(Math.abs(valid)), valid >= 0);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return null;
        }
    }

    private boolean isCommentLine(String line) {
        return line.startsWith(COMMENT_TOKEN);
    }

    private boolean isProblemLine(String line) {
        return line.startsWith(PROBLEM_TOKEN);
    }
}
