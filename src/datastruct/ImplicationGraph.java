package datastruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.SolverUtil;

public class ImplicationGraph {
    /**
     * Map nodes that links to other nodes.
     */
    private Map<Node, List<Node>> adjacencyList;

    /**
     * List of edges. Each element is a pair of source to destination node.
     */
    private Map<Pair<Node, Node>, Clause> edgeMap;

    private Set<String> unassignedVariables;
    private Map<String, Boolean> assignedVariables;
    private Map<String, Node> assignedNodes;

    private Node conflictedNode;

    private int backtrackLevel;

    public ImplicationGraph() {
        adjacencyList = new HashMap<>();
        edgeMap = new HashMap<>();
        unassignedVariables = new HashSet<>();
        assignedVariables = new HashMap<>();
        assignedNodes = new HashMap<>();
        backtrackLevel = -1;
    }

    private ImplicationGraph(ImplicationGraph other) {
        adjacencyList = new HashMap<>(other.adjacencyList);
        edgeMap = new HashMap<>(other.edgeMap);
        unassignedVariables = new HashSet<>(other.unassignedVariables);
        assignedVariables = new HashMap<>(other.assignedVariables);
        assignedNodes = new HashMap<>(other.assignedNodes);
        conflictedNode = other.conflictedNode != null ? new Node(other.conflictedNode) : null;
        backtrackLevel = other.backtrackLevel;
    }

    public ImplicationGraph copy() {
        return new ImplicationGraph(this);
    }

    public void initialize(Set<Clause> clauses) {
        clauses.forEach(c -> c.getLiterals().forEach(l -> unassignedVariables.add(l.getName())));
    }

    public void addDecisionNode(Variable v, int decisionLevel) {
        Node node = new Node(v, decisionLevel);
        addNode(node);
    }

    public void addImplicationNode(Variable impliedVariable, int decisionLevel, Clause antecedent) {
        Node impliedNode = new Node(impliedVariable, decisionLevel);
        adjacencyList.put(impliedNode, new ArrayList<>());
        unassignedVariables.remove(impliedVariable.getName());
        assignedVariables.put(impliedVariable.getName(), impliedVariable.getAssignment());
        assignedNodes.put(impliedVariable.getName(), impliedNode);

        Set<Literal> literals = antecedent.getLiterals();
        // Link all assignments of literals in the clause to the implied node
        literals.stream().filter(l -> !l.getName().equals(impliedVariable.getName())).forEach(l -> {
            Node antecedentNode = assignedNodes.get(l.getName());
            if (antecedentNode != null) {
                addEdge(antecedentNode, impliedNode, antecedent);
            }
        });
    }

    public void setConflictedNode(Variable var, int decisionLevel) {
        conflictedNode = new Node(var, decisionLevel);
    }

    public Node getConflictedNode() {
        return conflictedNode;
    }

    private void addNode(Node node) {
        if (adjacencyList.containsKey(node)) {
            return;
        }

        Variable v = node.getVariable();
        adjacencyList.put(node, new ArrayList<>());
        unassignedVariables.remove(v.getName());
        assignedVariables.put(v.getName(), v.getAssignment());
        assignedNodes.put(v.getName(), node);
    }

    public void addEdge(Variable from, Variable to, Clause clause) {
        Node fromNode = assignedNodes.get(from.getName());
        Node toNode = assignedNodes.get(to.getName());

        addEdge(fromNode, toNode, clause);
    }

    public int getBacktrackLevel() {
        return backtrackLevel;
    }

    private void addEdge(Node from, Node to, Clause clause) {
        Pair<Node, Node> edge = new Pair<>(from, to);
        if (edgeMap.containsKey(edge)) {
            return;
        }
        edgeMap.put(edge, clause);
        if (!adjacencyList.get(from).contains(to)) {
            adjacencyList.get(from).add(to);
        }
    }

    public Clause analyzeConflict(Clause conflictedClause, int decisionLevel) {
        Set<Clause> analyzedClauses = new HashSet<>();

        Clause learntClause = new Clause(conflictedClause);

        Set<Clause> clausesToAnalyze = new HashSet<>();
        Set<String> visitedVariables = new HashSet<>();

        List<Node> nodesList = new ArrayList<>();

        backtrackLevel = decisionLevel;

        nodesList.add(conflictedNode);
        while (!nodesList.isEmpty()) {
            Node nodeToLookAt = nodesList.get(0);
            nodesList.remove(nodeToLookAt);

            for (Pair<Node, Node> key : edgeMap.keySet()) {
                Clause clause = edgeMap.get(key);
                if (nodeToLookAt.getDecisionLevel() == decisionLevel
                        && key.getSecond().equals(nodeToLookAt)
                        && !analyzedClauses.contains(clause)) {
                    clausesToAnalyze.add(clause);
                }
            }

            visitedVariables.add(nodeToLookAt.getVariable().getName());

            if (clausesToAnalyze.isEmpty()) {
                backtrackLevel = nodeToLookAt.getDecisionLevel();
            }

            for (Clause clause : clausesToAnalyze) {
                for (Literal l : clause.getLiterals()) {
                    if (!visitedVariables.contains(l.getName())) {
                        nodesList.add(assignedNodes.get(l.getName()));
                    }
                }
                learntClause = SolverUtil.performResolution(learntClause, clause);
                analyzedClauses.add(clause);
            }
            clausesToAnalyze.clear();
            nodesList.sort((Node one, Node two) -> Integer.compare(one.getDecisionLevel(), two.getDecisionLevel()) * -1);
        }

        return learntClause;
    }

    private boolean hasUnassignedVariable() {
        return !unassignedVariables.isEmpty();
    }

    public Variable getNextUnassignedVariable(boolean assignment) {
        if (!hasUnassignedVariable()) {
            return null;
        }

        String[] variableNames = new String[unassignedVariables.size()];
        unassignedVariables.toArray(variableNames);
        return new Variable(variableNames[0], assignment);
    }

    public Clause getConflictedClause(Set<Clause> clauses) {
        for (Clause c : clauses) {
            boolean conflicted = true;

            for (Literal l : c.getLiterals()) {
                Boolean assignment = assignedVariables.get(l.getName());
                // If no assignment yet, or the assignment satisfies the literal, then it is not conflicted
                if (assignment == null || l.isSatisfied(assignment)) {
                    conflicted = false;
                    break;
                }
            }
            if (conflicted) {
                return c;
            }
        }
        return null;
    }

    public HashMap<String, Boolean> getAssignmentForClause(Clause c) {
        HashMap<String, Boolean> result = new HashMap<>();
        for (Literal l : c.getLiterals()) {
            Boolean assignment = assignedVariables.get(l.getName());
            if (assignment == null) {
                continue;
            }
            result.put(l.getName(), assignment);
        }
        return result;
    }

    public Boolean getAssignment(String variableName) {
        if (!assignedVariables.containsKey(variableName)) {
            return null;
        }
        return assignedVariables.get(variableName);
    }

    public boolean allVariablesAssigned(int numOfVariables) {
        return assignedVariables.size() == numOfVariables;
    }

    public String assignmentsToString() {
        StringBuilder sb = new StringBuilder();

        for (String key : assignedVariables.keySet()) {
            sb.append(key);
            sb.append(" ");
            sb.append(assignedVariables.get(key).toString());
            sb.append("\n");
        }

        return sb.toString();
    }
}
