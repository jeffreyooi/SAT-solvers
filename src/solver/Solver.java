package solver;

import java.util.Set;

import datastruct.Clause;
import datastruct.Variable;
import db.ClauseDB;

abstract class Solver implements ISolver {
    static final String UNSAT = "UNSAT";

    private int pickBranchingVariableCount;

    ClauseDB db;

    Solver(ClauseDB db) {
        this.db = db;
        pickBranchingVariableCount = 0;
    }

    abstract boolean unitPropagation(Set<Clause> clauses);

    Variable pickBranchingVariable() {
        pickBranchingVariableCount += 1;
        return null;
    }

    abstract int conflictAnalysis();

    abstract void backtrack(int level);

    public int getPickBranchingVariableCount() {
        return pickBranchingVariableCount;
    }
}
