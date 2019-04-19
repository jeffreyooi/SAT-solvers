package solver;

import datastruct.Variable;
import db.ClauseDB;

public class RandomSolver extends CDCLSolver {
    public RandomSolver(ClauseDB db) {
        super(db);
    }

    @Override
    protected Variable pickBranchingVariable() {
        super.pickBranchingVariable();
        return graph.getNextUnassignedVariable(true);
    }
}
