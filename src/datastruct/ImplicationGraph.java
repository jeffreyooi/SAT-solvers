package datastruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<Clause, Boolean> satisfiedClauses;

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
        if (adjacencyList.containsKey(node)) {
            return;
        }

        adjacencyList.put(node, new ArrayList<>());
        unassignedVariables.remove(v.getName());
        assignedVariables.put(v.getName(), v.getAssignment());
        assignedNodes.put(v.getName(), node);
    }

    public void addImplicationNode(Variable from, Variable to, int decisionLevel) {
        if (!assignedNodes.containsKey(from.getName())) {
            return;
        }
        Node fromNode = assignedNodes.get(from.getName());
        Node node = new Node(to, decisionLevel);
        if (adjacencyList.get(fromNode).contains(node)) {
            return;
        }
        adjacencyList.get(fromNode).add(node);

        unassignedVariables.remove(to.getName());
        assignedVariables.put(to.getName(), to.getAssignment());
        assignedNodes.put(to.getName(), node);
    }

    public void addNode(Node node) {
        if (adjacencyList.containsKey(node)) {
            return;
        }

        adjacencyList.put(node, new ArrayList<>());
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

    public void addEdge(Node source, Node destination) {
        Pair<Node, Node> edge = new Pair<>(source, destination);
        if (edgeList.contains(edge)) {
            return;
        }
        edgeList.add(edge);
        adjacencyList.get(source).add(destination);
    }

    public void removeEdge(Node source, Node destination) {
        Pair<Node, Node> edge = new Pair<>(source, destination);
        if (!edgeList.contains(edge)) {
            return;
        }
        edgeList.remove(edge);
        adjacencyList.get(source).remove(destination);
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

    public boolean hasConflict(Set<Clause> clauses) {
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
                return true;
            }
        }
        return false;
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
