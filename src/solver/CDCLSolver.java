package solver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import datastruct.Clause;
import datastruct.ImplicationGraph;
import datastruct.Literal;
import datastruct.Variable;
import db.ClauseDB;

public class CDCLSolver implements ISolver {

    private static final String UNSAT = "UNSAT";

    private ImplicationGraph graph;

    private ClauseDB db;

    private int decisionLevel;

    public CDCLSolver(ClauseDB db) {
        this.db = db;
        initialize();
    }

    private void initialize() {
        graph = new ImplicationGraph();
        // Set everything as unassigned in implication graph
        graph.initialize(db.getAllClauses());
    }

    public String evaluate() {
        // if(unitPropagation(graph, variable to apply unit resolution) == conflict
        // return unsat
        if (!unitPropagation(db.getAllClauses())) {
            return UNSAT;
        }


        // dl <- 0
        decisionLevel = 0;

        // while (not allVariablesAssigned(graph, variable to apply unit resolution)
        while (!allVariablesAssigned()) {
            // pick branching variable(graph, variable)
            Variable decision = pickBranchingVariable(false);
            if (decision == null) {
                return UNSAT;
            }

            // dl <- dl + 1
            decisionLevel += 1;

            // Store decision
            // v <- v union {assignment, variable}
            graph.addDecisionNode(decision, decisionLevel);

            // if unit propagation(graph, variable) == conflict
                // beta (decisionLevel to backtrack to) = conflict analysis
                    // if beta < 0 return unsat
                    // else backtrack(beta), decision level = beta

            // Propagation does not result in any conflict, continue
            if (unitPropagation(db.getAllClauses())) {
                continue;
            }


        }

        return graph.assignmentsToString();
    }

    /**
     * Simplify clauses using unit
     */

    private boolean unitPropagation(Set<Clause> clauses) {
        List<Clause> clauseList = new ArrayList<>(clauses);
        clauseList.sort((o1, o2) -> {
            int num1 = o1.getNumberOfLiterals();
            int num2 = o2.getNumberOfLiterals();
            return Integer.compare(num1, num2);
        });

        // For every clause, choose a variable, assign, then check if we can imply / force assignment
        // on other literals in clauses
        for (Clause c : clauseList) {
            for (Literal l : c.getLiterals()) {
                if (graph.getAssignment(l.getName()) == null) {
                    continue;
                }
                Variable v = new Variable(l.getName(), l.isPositive());
                graph.addDecisionNode(v, decisionLevel);
                ++decisionLevel;
                // After adding a decision of unit clause, propagate the result and add implications
                // (clauses that contain literal that will be forced to a value)
                if (!implicationPropagation(clauses, v)) {
                    return false;
                }
            }
        }

        return true;
    }

    // TODO fix bug here
    private boolean implicationPropagation(Set<Clause> clauses, Variable decision) {
        if (graph.hasConflict(clauses)) {
            return false;
        }

        for (Clause c : clauses) {
            // Clause do not contain the literal which we just assigned, continue
            Literal l = c.getLiteral(decision.getName());
            if (l == null) {
                continue;
            }

            Boolean assignment = graph.getAssignment(decision.getName());
            if (assignment == null) {
                continue;
            }

            List<Literal> literalsWithoutAssignment = new ArrayList<>();
            for (Literal lit : c.getLiterals()) {
                if (graph.getAssignment(lit.getName()) != null) {
                    continue;
                }
                literalsWithoutAssignment.add(lit);
            }

            // If there is only 1 left, we can force the value, else continue
            if (literalsWithoutAssignment.size() != 1) {
                continue;
            }

            Literal lit = literalsWithoutAssignment.get(0);
            Variable v = new Variable(lit.getName(), lit.isPositive());
            graph.addImplicationNode(decision, v, decisionLevel);
            ++decisionLevel;
            // Recursively check until we cannot imply / force any other values
            if (!implicationPropagation(clauses, v)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Select a variable to assign and the respective value.
     */
    private Variable pickBranchingVariable(boolean assignment) {
        return graph.getNextUnassignedVariable(assignment);
    }

    /**
     * Analyze most recent conflict and learning a new clause from the conflict and returns the
     * decision level to backtrack to
     */
    private int conflictAnalysis() {
        return -1;
    }

    /**
     * Backtracks to the decision level computed by conflict analysis
     */
    private void backtrack(int decisionLevel) {

    }

    private boolean allVariablesAssigned() {
        return graph.allVariablesAssigned(db.getNumberOfLiterals());
    }
}
