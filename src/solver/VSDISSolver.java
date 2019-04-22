package solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.Config;
import datastruct.Clause;
import datastruct.Variable;
import db.ClauseDB;

/**
 * Conflict-History solver with conflict history search for pick branching variable
 */
public class VSDISSolver extends CDCLSolver {

    private static final float ALPHA = 0.4f;

    private Map<String, Float> variableScore;
    private List<String> branchingHeuristicsSortedList;

    public VSDISSolver(ClauseDB db) {
        super(db);
        variableScore = new HashMap<>();
        branchingHeuristicsSortedList = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        db.getAllLiterals().forEach(l -> {
            variableScore.put(l, 0f);
            branchingHeuristicsSortedList.add(l);
        });
    }

    @Override
    public String evaluate() {
        // If unit propagation failed before even evaluation, return UNSAT
        if (!unitPropagation(db.getAllClauses())) {
            return UNSAT;
        }

        while (!allVariablesAssigned()) {
            Variable decision = pickBranchingVariable();
            if (decision == null) {
                return UNSAT;
            }

            decisionLevel += 1;

            // Store decision
            graph.addDecisionNode(decision, decisionLevel);

            // Propagation does not result in any conflict, continue
            if (implicationPropagation(db.getAllClauses(), decision)) {
                continue;
            }

            if (Config.logging == Config.Logging.DEBUG) {
                System.out.println("Decision made during conflict: " + decision);
                System.out.println("Assignment when conflict:");
                System.out.println(graph.assignmentsToString());
                System.out.println(graph.edgesToString());
                System.out.println();

                System.out.println("Conflicting clause: " + conflictedClause.toString());
                System.out.println("Conflicting assignment: " + conflictedVariable.toString());
            }

            // Perform conflict analysis to learn new clause and level to backtrack to
            int backtrackLevel = conflictAnalysis();

            if (backtrackLevel == -1) {
                return UNSAT;
            }

            backtrack(backtrackLevel);

            if (!forceSatisfyClause(db.getLastLearntClause())) {
                return UNSAT;
            }
            db.clearLastLearntClause();
            decayAllVariableActivities();
        }

        return graph.assignmentsToString();
    }

    @Override
    Variable pickBranchingVariable() {
        Set<String> unassignedVariables = graph.getUnassignedVariables();
        String variableToAssign = null;
        float maxScore = -1;
        for (String v : unassignedVariables) {
            if (variableToAssign == null) {
                variableToAssign = v;
                maxScore = variableScore.get(v);
                continue;
            }
            float score = variableScore.get(v);
            if (score > maxScore) {
                variableToAssign = v;
                maxScore = score;
            }
        }
        if (variableToAssign == null) {
            return null;
        }
        return new Variable(variableToAssign, true);
    }

    @Override
    void addLearntClause(Clause learntClause) {
        learntClause.getLiterals().forEach(l -> {
            float existingScore = variableScore.get(l.getName());
            variableScore.replace(l.getName(), existingScore + 1);
        });
        branchingHeuristicsSortedList.sort((first, second)
                -> Float.compare(variableScore.get(first), variableScore.get(second)) * -1);
        super.addLearntClause(learntClause);
    }

    private void decayAllVariableActivities() {
        variableScore.forEach((l, f) -> f = f * ALPHA);
    }

    @Override
    public void reset() {
        super.reset();
        variableScore.clear();
        branchingHeuristicsSortedList.clear();
        initialize();
    }
}
