package solver;

import java.util.Set;

import datastruct.Clause;
import datastruct.Variable;
import db.ClauseDB;

abstract class Solver implements ISolver {
    static final String UNSAT = "UNSAT";

    ClauseDB db;

    Solver(ClauseDB db) {
        this.db = db;
    }

    abstract boolean unitPropagation(Set<Clause> clauses);

    abstract Variable pickBranchingVariable();

    abstract int conflictAnalysis();

    abstract void backtrack(int level);
}
