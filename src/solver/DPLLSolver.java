package solver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import datastruct.Clause;
import datastruct.Pair;
import datastruct.Variable;
import db.ClauseDB;

public class DPLLSolver extends Solver {

    private int decisionLevel;

    private Stack<Pair<String, Boolean>> assignmentTree;

    private Map<String, Boolean> assignedVariables;
    private Set<String> unassignedVariables;

    public DPLLSolver(ClauseDB db) {
        super(db);
        decisionLevel = 0;
        assignmentTree = new Stack<>();
        assignedVariables = new HashMap<>();
        initialize();
    }

    private void initialize() {
        Set<Clause> clauses = db.getAllClauses();
        clauses.forEach(c -> c.getLiterals().forEach(l -> unassignedVariables.add(l.getName())));
    }

    public String evaluate() {
        if (!unitPropagation(db.getAllClauses())) {
            return UNSAT;
        }

        while (!allVariablesAssigned()) {
            Variable decision = pickBranchingVariable();
            if (decision == null) {
                return UNSAT;
            }

            decisionLevel += 1;

            unassignedVariables.remove(decision.getName());
            assignedVariables.put(decision.getName(), decision.getAssignment());

            if (unitPropagation(db.getAllClauses())) {
                assignmentTree.push(new Pair<>(decision.getName(), decision.getAssignment()));
                continue;
            }

            decision.setAssignment(false);
            assignedVariables.replace(decision.getName(), decision.getAssignment());

            if (unitPropagation(db.getAllClauses())) {
                assignmentTree.push(new Pair<>(decision.getName(), decision.getAssignment()));
                continue;
            }

            int backtrackLevel = conflictAnalysis();
            if (backtrackLevel < 0) {
                return UNSAT;
            }

            backtrack(backtrackLevel);
        }

        return null;
    }

    boolean unitPropagation(Set<Clause> clauses) {

        return false;
    }

    Variable pickBranchingVariable() {
        if (unassignedVariables.isEmpty()) {
            return null;
        }
        String[] vars = new String[unassignedVariables.size()];
        unassignedVariables.toArray(vars);
        return new Variable(vars[0], true);
    }

    /**
     * Check if all variables are assigned to exit the while loop
     * @return true if all variables are assigned, false otherwise.
     */
    private boolean allVariablesAssigned() {
        return assignedVariables.size() == db.getNumberOfLiterals();
    }

    /**
     * Returns the decision level to backtrack to, which is one level up from current decision level.
     * @return decision level to backtrack to.
     */
    int conflictAnalysis() {
        return decisionLevel - 1;
    }

    /**
     * Revert the state to the decision level specified.
     * @param backtrackLevel decision level to backtrack to.
     */
    void backtrack(int backtrackLevel) {
        Pair<String, Boolean> assigned = assignmentTree.pop();
        assignedVariables.remove(assigned.getFirst());
        unassignedVariables.add(assigned.getFirst());
        decisionLevel = backtrackLevel;
    }
}
