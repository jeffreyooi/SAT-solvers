package db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datastruct.Clause;
import datastruct.Literal;

public class ClauseDB {

    private Set<Clause> clauses;

    private Set<String> literals;

    private List<Clause> learntClauses;
    private Clause lastLearntClause;

    private Map<String, Integer> twoClauseLiteralCountMap;
    private Map<String, Integer> literalCountMap;

    public ClauseDB() {
        clauses = new HashSet<>();
        literals = new HashSet<>();
        learntClauses = new ArrayList<>();
        twoClauseLiteralCountMap = new HashMap<>();
        literalCountMap = new HashMap<>();
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
        updateLiteralCount(clause);
    }

    public void insertLearntClause(Clause clause) {
        if (learntClauses.contains(clause)) {
            throw new IllegalStateException("Clause already learnt");
        }
        lastLearntClause = clause;
        learntClauses.add(clause);
        insertClause(clause);
    }

    private void updateLiteralCount(Clause clause) {
        int size = clause.getNumberOfLiterals();
        for (Literal l : clause.getLiterals()) {
            String lName = l.getName();

            if (!literalCountMap.containsKey(lName)) {
                literalCountMap.put(lName, 0);
            }
            int lCount = literalCountMap.get(lName);
            literalCountMap.replace(lName, ++lCount);

            if (size != 2) {
                continue;
            }

            if (!twoClauseLiteralCountMap.containsKey(lName)) {
                twoClauseLiteralCountMap.put(lName, 0);
            }
            lCount = twoClauseLiteralCountMap.get(lName);
            twoClauseLiteralCountMap.replace(lName, ++lCount);
        }
    }

    public Map<String, Integer> getLiteralCountMap() {
        return literalCountMap;
    }

    public Map<String, Integer> getTwoClauseLiteralCountMap() {
        return twoClauseLiteralCountMap;
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

    public Set<String> getAllLiterals() {
        return literals;
    }

    public void reset() {
        clauses.clear();
        literals.clear();
        learntClauses.clear();
        lastLearntClause = null;
    }

    public void clearLearntClauses() {
        learntClauses.clear();
        lastLearntClause = null;
    }
}
