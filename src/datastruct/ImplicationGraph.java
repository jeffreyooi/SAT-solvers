package datastruct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import config.Config;
import util.SolverUtil;

public class ImplicationGraph {
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
        edgeMap = new HashMap<>();
        unassignedVariables = new HashSet<>();
        assignedVariables = new HashMap<>();
        assignedNodes = new HashMap<>();
        backtrackLevel = -1;
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
        if (assignedNodes.containsKey(node.getVariable().getName())) {
            return;
        }

        Variable v = node.getVariable();
        unassignedVariables.remove(v.getName());
        assignedVariables.put(v.getName(), v.getAssignment());
        assignedNodes.put(v.getName(), node);
    }

    public void revertToDecisionLevel(int decisionLevel) {

        List<Node> nodesToRemove = new ArrayList<>();
        for (String k : assignedNodes.keySet()) {
            Node n = assignedNodes.get(k);
            if (n.getDecisionLevel() > decisionLevel) {
                nodesToRemove.add(n);
            }
        }

        for (Node remove : nodesToRemove) {
            removeEdges(remove);
            assignedNodes.remove(remove.getVariable().getName());
            assignedVariables.remove(remove.getVariable().getName());
            unassignedVariables.add(remove.getVariable().getName());
        }
    }

    private void removeEdges(Node n) {
        List<Pair<Node, Node>> edgesToRemove = new ArrayList<>();
        for (Pair<Node, Node> edge : edgeMap.keySet()) {
            if (edge.getFirst().equals(n) || edge.getSecond().equals(n)) {
                edgesToRemove.add(edge);
            }
        }
        for (Pair<Node, Node> remove : edgesToRemove) {
            edgeMap.remove(remove);
        }
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
                if (Config.logging == Config.Logging.DEBUG) {
                    System.out.println(
                            String.format("Resolve %s with %s \n",
                                    learntClause.toString(), clause.toString()));
                }
                learntClause = SolverUtil.performResolution(learntClause, clause);
                if (Config.logging == Config.Logging.DEBUG) {
                    System.out.println(String.format("--> %s\n", learntClause.toString()));
                }
                analyzedClauses.add(clause);
            }
            clausesToAnalyze.clear();

            // Check if we are at an implication point where we can stop
            // i.e. only left with 1 node with decisionLevel
            int count = 0;
            List<Integer> decisionLevels = new ArrayList<>();
            for (Literal l : learntClause.getLiterals()) {
                Node n = assignedNodes.get(l.getName());
                if (n.getDecisionLevel() == decisionLevel) {
                    ++count;
                } else {
                    decisionLevels.add(n.getDecisionLevel());
                }
            }

            if (count == 1) {
                --backtrackLevel;
                for (int level : decisionLevels) {
                    if (level >= decisionLevel) {
                        continue;
                    }
                    backtrackLevel = Math.max(level, backtrackLevel);
                }
                break;
            }

            nodesList.sort((Node one, Node two) -> Integer.compare(one.getDecisionLevel(), two.getDecisionLevel()) * -1);
        }

        return learntClause;
    }

    private boolean hasUnassignedVariable() {
        return !unassignedVariables.isEmpty();
    }

    public Variable getNextUnassignedVariable(boolean random) {
        if (!hasUnassignedVariable()) {
            return null;
        }

        String[] variableNames = new String[unassignedVariables.size()];
        unassignedVariables.toArray(variableNames);
        int index = 0;
        if (random) {
            Random rand = new Random();
            index = rand.nextInt(variableNames.length);
        }
        return new Variable(variableNames[index], true);
    }

    public Variable getNextUnassignedVariable(Map<String, Integer> literalCountMap) {
        List<Pair<Integer, String>> unassignedLiteralCountList = new ArrayList<>();

        List<String> unassignedMoreThanTwoClauseLiterals = new ArrayList<>();
        for (String unassigned : unassignedVariables) {
            if (!literalCountMap.containsKey(unassigned)) {
                unassignedMoreThanTwoClauseLiterals.add(unassigned);
                continue;
            }
            Pair<Integer, String> pair = new Pair<>(literalCountMap.get(unassigned), unassigned);
            unassignedLiteralCountList.add(pair);
        }

        unassignedLiteralCountList.sort(
                (Pair<Integer, String> first, Pair<Integer, String> second)
                        -> first.getFirst().compareTo(second.getFirst()) * -1);

        String selected;
        if (!unassignedLiteralCountList.isEmpty()) {
            selected = unassignedLiteralCountList.get(0).getSecond();
        } else {
            selected = unassignedMoreThanTwoClauseLiterals.get(0);
        }
        return new Variable(selected, true);
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

    public String edgesToString() {
        StringBuilder sb = new StringBuilder();

        for (Pair<Node, Node> key : edgeMap.keySet()) {
            sb.append(key.getFirst()).append(" -> ").append(key.getSecond());
            sb.append(": ").append(edgeMap.get(key).toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
