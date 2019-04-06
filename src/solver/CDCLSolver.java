package solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import datastruct.Clause;
import datastruct.ImplicationGraph;
import datastruct.Literal;
import datastruct.Node;
import datastruct.Variable;
import db.ClauseDB;

public class CDCLSolver implements ISolver {

    private static final String UNSAT = "UNSAT";

    private ImplicationGraph graph;

    private ClauseDB db;

    private int decisionLevel;

    private Clause conflictedClause;

    private Stack<ImplicationGraph> history;

    public CDCLSolver(ClauseDB db) {
        this.db = db;
        initialize();
    }

    private void initialize() {
        graph = new ImplicationGraph();
        // Set everything as unassigned in implication graph
        graph.initialize(db.getAllClauses());
        history = new Stack<>();
    }

    public String evaluate() {
        // if(unitPropagation(graph, variable to apply unit resolution) == conflict
        // return unsat
        if (!unitPropagation(db.getAllClauses())) {
            return UNSAT;
        }

        // dl <- 0
        decisionLevel = 0;
        history.push(graph.copy());

        // while (not allVariablesAssigned(graph, variable to apply unit resolution)
        while (!allVariablesAssigned()) {
            // pick branching variable(graph, variable)
            Variable decision = pickBranchingVariable(true);
            if (decision == null) {
                return UNSAT;
            }

            // dl <- dl + 1
            decisionLevel += 1;

            // Store decision
            // v <- v union {assignment, variable}
            graph.addDecisionNode(decision, decisionLevel);

            history.push(graph.copy());

            // if unit propagation(graph, variable) == conflict
                // beta (decisionLevel to backtrack to) = conflict analysis
                    // if beta < 0 return unsat
                    // else backtrack(beta), decision level = beta

            // Propagation does not result in any conflict, continue

            if (implicationPropagation(db.getAllClauses(), decision)) {
                continue;
            }

            System.out.println("Decision made during conflict: " + decision);
            System.out.println("Assignment when conflict:");
            System.out.println(graph.assignmentsToString());
            System.out.println(graph.edgesToString());
            System.out.println();

            System.out.println("Conflicting clause: " + conflictedClause.toString());
            System.out.println("Conflicting assignment: " + graph.getConflictedNode());

            int backtrackLevel = conflictAnalysis();

            if (backtrackLevel == -1) {
                return UNSAT;
            }

            backtrack(backtrackLevel);

//            if (!unitPropagation(db.getAllClauses())) {
//                return UNSAT;
//            }
            if (!forceSatisfyClause(db.getLastLearntClause(), db.getAllClauses())) {
                return UNSAT;
            }
            db.clearLastLearntClause();
        }

        return graph.assignmentsToString();
    }

    /**
     * Simplify clauses using unit
     */
    private boolean unitPropagation(Set<Clause> clauses) {
        // For every clause, choose a variable, assign, then check if we can imply / force assignment
        // on other literals in clauses
        for (Clause c : clauses) {
            Map<String, Boolean> assignment = graph.getAssignmentForClause(c);
            // Only can imply if there's only 1 literal unassigned in a clause
            Variable v = c.getImpliedVariable(assignment);
            if (v == null) {
                continue;
            }
            graph.addDecisionNode(v, decisionLevel);
            if (!implicationPropagation(clauses, v)) {
                return false;
            }
        }

        return true;
    }

    private boolean forceSatisfyClause(Clause clause, Set<Clause> clauses) {
        System.out.println(String.format("Forcing clause %s to be true", clause.toString()));
        Map<String, Boolean> assignment = graph.getAssignmentForClause(clause);

        List<Literal> unassignedLiterals = new ArrayList<>();
        for (Literal l : clause.getLiterals()) {
            if (!assignment.containsKey(l.getName())) {
                unassignedLiterals.add(l);
            }
        }
        if (unassignedLiterals.size() != 1) {
            for (String k : assignment.keySet()) {
                System.err.println(String.format("%s: %s", k, assignment.get(k) ? "true" : "false"));
            }
            throw new IllegalStateException("There should always be only 1 unassigned literal that can be implied from learnt clause");
        }
        Literal literalToImply = unassignedLiterals.get(0);
        Variable v = new Variable(literalToImply.getName(), false);
        assignment.put(literalToImply.getName(), false);
        if (!clause.isSatisfied(assignment)) {
            v.setAssignment(true);
        }
        graph.addImplicationNode(v, decisionLevel, clause);
        for (String k : assignment.keySet()) {
            System.out.println(String.format("%s: %s", k, assignment.get(k) ? "true" : "false"));
        }
        System.out.println();
//        return implicationPropagation(clauses, v);
        return true;
    }

    private boolean checkClauseSatisfiable(Clause clause) {
        boolean sat = true;
        for (Literal l : clause.getLiterals()) {
            Boolean assignment = graph.getAssignment(l.getName());
            if (assignment == null) {
                continue;
            }

            if (!l.isSatisfied(assignment)) {
                sat = false;
                break;
            }
        }
        return sat;
    }

    private boolean implicationPropagationV2(Set<Clause> clauses, Variable decision) {

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

            c.getLiterals().forEach(lit -> {
                if (graph.getAssignment(lit.getName()) == null) {
                    literalsWithoutAssignment.add(lit);
                }
            });

            // If there is only 1 left, we can force the value, else continue
            if (literalsWithoutAssignment.size() != 1) {
                continue;
            }

            Literal lit = literalsWithoutAssignment.get(0);
            boolean sat = checkClauseSatisfiable(c);
            boolean assign = false;
            if (!sat) {
                assign = lit.isPositive();
            }
            Variable impliedVariable = new Variable(lit.getName(), assign);
            graph.addImplicationNode(impliedVariable, decisionLevel, c);
        }

        return true;
    }

    private boolean implicationPropagation(Set<Clause> clauses, Variable decision) {
        Clause conflicted = graph.getConflictedClause(clauses);
        if (conflicted != null) {
            conflictedClause = conflicted;
            graph.setConflictedNode(decision, decisionLevel);
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

            c.getLiterals().forEach(lit -> {
                if (graph.getAssignment(lit.getName()) == null) {
                    literalsWithoutAssignment.add(lit);
                }
            });


            // If there is only 1 left, we can force the value, else continue
            if (literalsWithoutAssignment.size() != 1) {
                continue;
            }

            Literal lit = literalsWithoutAssignment.get(0);
            boolean sat = checkClauseSatisfiable(c);
            if (sat) {
                continue;
            }
            boolean assign = lit.isPositive();
            Variable impliedVariable = new Variable(lit.getName(), assign);
            graph.addImplicationNode(impliedVariable, decisionLevel, c);

            // Recursively check until we cannot imply / force any other values
            if (!implicationPropagation(clauses, impliedVariable)) {
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
     * decision level to backtrack to. If unable to analyze, return -1 to indicate that it is unsat
     */
    private int conflictAnalysis() {
        if (conflictedClause == null) {
            throw new NullPointerException("conflictedClause is null");
        }

        Node conflictedNode = graph.getConflictedNode();
        if (conflictedNode == null) {
            throw new NullPointerException("conflictedNode is null");
        }

        int conflictDecisionLevel = conflictedNode.getDecisionLevel();

        Clause learntClause = graph.analyzeConflict(conflictedClause, conflictDecisionLevel);

        System.out.println("Learnt clause: " + learntClause.toString());
        System.out.println("Backtrack to: " + graph.getBacktrackLevel());

        db.insertLearntClause(learntClause);

        return graph.getBacktrackLevel();
    }

    /**
     * Backtracks to the decision level computed by conflict analysis
     */
    private void backtrack(int level) {
        graph.revertToDecisionLevel(level);
        decisionLevel = level;
    }

    private boolean allVariablesAssigned() {
        return graph.allVariablesAssigned(db.getNumberOfLiterals());
    }
}
