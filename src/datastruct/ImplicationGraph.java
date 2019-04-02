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
    private List<Pair<Node, Node>> edgeList;

    private Set<String> unassignedVariables;
    private Map<String, Boolean> assignedVariables;
    private Map<String, Node> assignedNodes;

    private Node conflictedNode;

    public ImplicationGraph() {
        adjacencyList = new HashMap<>();
        edgeList = new ArrayList<>();
        unassignedVariables = new HashSet<>();
        assignedVariables = new HashMap<>();
        assignedNodes = new HashMap<>();
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
                addEdge(antecedentNode, impliedNode);
            }
        });
    }

    public void setConflictedNode(Variable var, int decisionLevel) {
        conflictedNode = new Node(var, decisionLevel);
    }

    public Node getConflictedNode() {
        return conflictedNode;
    }

    public void removeConflictedNode() {
        conflictedNode = null;
    }

    public void addNode(Node node) {
        if (adjacencyList.containsKey(node)) {
            return;
        }

        Variable v = node.getVariable();
        adjacencyList.put(node, new ArrayList<>());
        unassignedVariables.remove(v.getName());
        assignedVariables.put(v.getName(), v.getAssignment());
        assignedNodes.put(v.getName(), node);
    }

    public void removeNode(Node node) {
        if (!adjacencyList.containsKey(node)) {
            return;
        }

        // If node is a source or destination of an edge, remove
        for (Pair<Node, Node> edge : edgeList) {
            if (edge.getFirst() == node || edge.getSecond() == node) {
                edgeList.remove(edge);
            }
        }

        adjacencyList.remove(node);
    }

    public void addEdge(Variable from, Variable to) {
        Node fromNode = assignedNodes.get(from.getName());
        Node toNode = assignedNodes.get(to.getName());

        addEdge(fromNode, toNode);
    }

    private void addEdge(Node from, Node to) {
        Pair<Node, Node> edge = new Pair<>(from, to);
        if (edgeList.contains(edge)) {
            return;
        }
        edgeList.add(edge);
        if (!adjacencyList.get(from).contains(to)) {
            adjacencyList.get(from).add(to);
        }
    }

    public void removeEdge(Variable from, Variable to) {
        Node fromNode = assignedNodes.get(from.getName());
        Node toNode = assignedNodes.get(to.getName());

        Pair<Node, Node> edge = new Pair<>(fromNode, toNode);
        if (!edgeList.contains(edge)) {
            return;
        }
        edgeList.remove(edge);
        adjacencyList.get(fromNode).remove(toNode);
    }

    public boolean hasUnassignedVariable() {
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
