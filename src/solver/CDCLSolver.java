package solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.Config;
import datastruct.Clause;
import datastruct.ImplicationGraph;
import datastruct.Literal;
import datastruct.Variable;
import db.ClauseDB;

public class CDCLSolver extends Solver {

    /**
     * Implication graph that stores implications and assignments of the CNF
     */
    ImplicationGraph graph;

    /**
     * Current decision level of the solver.
     */
    private int decisionLevel;

    /**
     * Decision level at which conflict happens.
     */
    private int conflictedDecisionLevel;
    /**
     * Assigned variable which causes conflict.
     */
    private Variable conflictedVariable;
    /**
     * Clause that has conflict due to conflicted variable.
     */
    private Clause conflictedClause;

    public CDCLSolver(ClauseDB db) {
        super(db);
        initialize();
    }

    /**
     * Initialize CDCL solver
     */
    private void initialize() {
        graph = new ImplicationGraph();
        graph.initialize(db.getAllClauses());
        conflictedDecisionLevel = -1;
        decisionLevel = 0;
    }

    /**
     * Solve the CNF.
     * @return result of variable assignments in string
     */
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
        }

        return graph.assignmentsToString();
    }

    /**
     * Perform unit propagation, which checks if there are variables that can be assigned / implied due
     * to several reasons such as:
     * 1. It is a unit clause
     * 2. There is only 1 unassigned variable in the clause
     * @param clauses clauses in CNF
     * @return true if there are no conflicts after propagation, false otherwise
     */
    boolean unitPropagation(Set<Clause> clauses) {
        Set<Clause> unitClauses = new HashSet<>();
        Set<Clause> nonUnitClauses = new HashSet<>();
        clauses.forEach(clause -> {
            if (clause.getLiterals().size() == 1) {
                unitClauses.add(clause);
            } else {
                nonUnitClauses.add(clause);
            }
        });

        for (Clause c : unitClauses) {
            Map<String, Boolean> assignment = graph.getAssignmentForClause(c);
            if (!assignment.isEmpty()) {
                continue;
            }
            Variable v = c.getImpliedVariable(assignment);
            if (v == null) {
                continue;
            }
            graph.addDecisionNode(v, decisionLevel);
        }

        // For every clause, choose a variable, assign, then check if we can imply / force assignment
        // on other literals in clauses
        for (Clause c : nonUnitClauses) {
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

    /**
     * Force to satisfy a clause. Only used for learnt clause.
     * @param clause learnt clause
     * @return true
     */
    private boolean forceSatisfyClause(Clause clause) {
        if (Config.logging == Config.Logging.VERBOSE) {
            System.out.println(String.format("Forcing clause %s to be true", clause.toString()));
        }
        Map<String, Boolean> assignment = graph.getAssignmentForClause(clause);

        List<Literal> unassignedLiterals = new ArrayList<>();
        for (Literal l : clause.getLiterals()) {
            if (!assignment.containsKey(l.getName())) {
                unassignedLiterals.add(l);
            }
        }
        if (unassignedLiterals.size() != 1) {
            return true;
        }
        Literal literalToImply = unassignedLiterals.get(0);
        Variable v = new Variable(literalToImply.getName(), false);
        assignment.put(literalToImply.getName(), false);
        if (!clause.isSatisfied(assignment)) {
            v.setAssignment(true);
        }
        graph.addImplicationNode(v, decisionLevel, clause);
        if (Config.logging != Config.Logging.NONE) {
            for (String k : assignment.keySet()) {
                System.out.println(String.format("%s: %s", k, assignment.get(k) ? "true" : "false"));
            }
            System.out.println();
        }

        return true;
    }

    /**
     * Check if clause is still satisfiable.
     * @param clause clause to verify
     * @return true if clause is satisfiable, false otherwise
     */
    private boolean checkClauseSatisfiable(Clause clause) {
        boolean sat = false;
        for (Literal l : clause.getLiterals()) {
            Boolean assignment = graph.getAssignment(l.getName());
            if (assignment == null) {
                continue;
            }

            if (l.isSatisfied(assignment)) {
                sat = true;
                break;
            }
        }
        return sat;
    }

    /**
     * Perform implication propagation, which is to continuously imply / force variables to be a certain
     * assignment if there is only one unassigned variable in a clause. If there exists a conflict,
     * {@code conflictedClause}, {@code conflictedDecisionLevel} and {@code conflictedVariable} will be
     * set.
     * @param clauses clauses in CNF
     * @param decision last decision made
     * @return true if variables can be implied with no conflicts, false otherwise
     */
    private boolean implicationPropagation(Set<Clause> clauses, Variable decision) {
        Clause conflicted = graph.getConflictedClause(clauses);
        if (conflicted != null) {
            conflictedClause = conflicted;
            conflictedDecisionLevel = decisionLevel;
            conflictedVariable = decision;
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

//            List<Literal> literalsWithoutAssignment = new ArrayList<>();
//
//            c.getLiterals().forEach(lit -> {
//                if (graph.getAssignment(lit.getName()) == null) {
//                    literalsWithoutAssignment.add(lit);
//                }
//            });
//
//
//            // If there is only 1 left, we can force the value, else continue
//            if (literalsWithoutAssignment.size() != 1) {
//                continue;
//            }
//
//            Literal lit = literalsWithoutAssignment.get(0);
//            boolean sat = checkClauseSatisfiable(c);
//            if (sat) {
//                continue;
//            }
//            boolean assign = lit.isPositive();
//            Variable impliedVariable = new Variable(lit.getName(), assign);
            Map<String, Boolean> clauseAssignment = graph.getAssignmentForClause(c);
            Variable impliedVariable = c.getImpliedVariable(clauseAssignment);
            if (impliedVariable == null) {
                continue;
            }
            graph.addImplicationNode(impliedVariable, decisionLevel, c);

            // Recursively check until we cannot imply / force any other values
            if (!implicationPropagation(clauses, impliedVariable)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Picks an unassigned variable.
     * @return unassigned variable
     */
    Variable pickBranchingVariable() {
        super.pickBranchingVariable();
        return graph.getNextUnassignedVariable(false);
    }

    /**
     * Analyze most recent conflict and learning a new clause from the conflict and returns the
     * decision level to backtrack to. If unable to analyze, return -1 to indicate that it is
     * unsatisfiable.
     * @return level to backtrack to
     */
    int conflictAnalysis() {
        if (conflictedClause == null) {
            throw new NullPointerException("conflictedClause is null");
        }

        Clause learntClause = graph.analyzeConflict(conflictedClause, conflictedVariable, conflictedDecisionLevel);

        if (Config.logging != Config.Logging.NONE) {
            System.out.println("Learnt clause: " + learntClause.toString());
            System.out.println("Backtrack to: " + graph.getBacktrackLevel());
        }

        db.insertLearntClause(learntClause);

        return graph.getBacktrackLevel();
    }

    /**
     * Backtracks to the decision level.
     * @param level decision level to backtrack to
     */
    void backtrack(int level) {
        graph.revertToDecisionLevel(level);
        decisionLevel = level;
    }

    /**
     * Check if all variables are assigned.
     * @return true if all variables are assigned, false otherwise
     */
    private boolean allVariablesAssigned() {
        return graph.allVariablesAssigned(db.getNumberOfLiterals());
    }

    public void reset() {
        graph.reset();
        graph.initialize(db.getAllClauses());
        decisionLevel = 0;
        conflictedDecisionLevel = -1;
        conflictedVariable = null;
        conflictedClause = null;
    }
}
