package db;

import java.util.HashSet;
import java.util.Set;

import datastruct.Clause;
import datastruct.Literal;

public class ClauseDB {

    private Set<Clause> clauses;

    private Set<String> literals;

    public ClauseDB() {
        clauses = new HashSet<>();
        literals = new HashSet<>();
    }

    /**
     * Insert a clause into database.
     * @param clause clause
     */
    public void insertClause(Clause clause) {
        clauses.add(clause);
        for (Literal l : clause.getLiterals()) {
            literals.add(l.getName());
        }
    }

    public Set<Clause> getAllClauses() {
        return clauses;
    }

    public int getNumberOfClauses() {
        return clauses.size();
    }

    public int getNumberOfLiterals() {
        return literals.size();
    }
}
