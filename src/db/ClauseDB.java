package db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import datastruct.Clause;
import datastruct.Literal;

public class ClauseDB {

    private Set<Clause> clauses;

    private Set<String> literals;

    private List<Clause> learntClauses;
    private Clause lastLearntClause;

    public ClauseDB() {
        clauses = new HashSet<>();
        literals = new HashSet<>();
        learntClauses = new ArrayList<>();
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

    public void insertLearntClause(Clause clause) {
        if (learntClauses.contains(clause)) {
            throw new IllegalStateException("Clause already learnt");
        }
        lastLearntClause = clause;
        learntClauses.add(clause);
        insertClause(clause);
    }

    public Clause getLastLearntClause() {
        return lastLearntClause;
    }

    public void clearLastLearntClause() {
        lastLearntClause = null;
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
