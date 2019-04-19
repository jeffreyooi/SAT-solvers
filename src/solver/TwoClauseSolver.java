package solver;

import java.util.Map;

import datastruct.Variable;
import db.ClauseDB;

public class TwoClauseSolver extends CDCLSolver {
    public TwoClauseSolver(ClauseDB db) {
        super(db);
    }

    @Override
    protected Variable pickBranchingVariable() {
        super.pickBranchingVariable();
        Map<String, Integer> twoClauseLiteralCountMap = db.getTwoClauseLiteralCountMap();
        return graph.getNextUnassignedVariable(twoClauseLiteralCountMap);
    }
}
