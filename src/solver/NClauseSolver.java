package solver;

import java.util.Map;

import datastruct.Variable;
import db.ClauseDB;

public class NClauseSolver extends CDCLSolver {
    public NClauseSolver(ClauseDB db) {
        super(db);
    }

    @Override
    protected Variable pickBranchingVariable() {
        super.pickBranchingVariable();
        Map<String, Integer> nClauseLiteralCountMap = db.getLiteralCountMap();
        return graph.getNextUnassignedVariable(nClauseLiteralCountMap);
    }
}
