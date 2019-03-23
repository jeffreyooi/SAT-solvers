package solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datastruct.Clause;

public class Assignment {
    Map<String, Boolean> variableAssignments;

    /**
     * Store all tried assignments
     */
    Set<Map<String, Boolean>> assignmentHistory;

    /**
     * Store assignments at level
     */
    Map<Integer, Map<String, Boolean>> assignmentLevels;

    int level;

    Assignment() {
        variableAssignments = new HashMap<>();
        level = 0;
        assignmentHistory = new HashSet<>();
    }

    public void initialize(List<Clause> clauses) {
        clauses.forEach(c -> c.getLiterals().forEach(l -> {
            if (!variableAssignments.containsKey(l.getName())) {
                variableAssignments.put(l.getName(), false);
            }
        }));
    }

    public void assign() {

    }

    public void backtrack() {

    }

    public Map<String, Boolean> getCurrentAssignment() {
        return variableAssignments;
    }

    public int getLevel() {
        return level;
    }
}
