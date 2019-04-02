package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datastruct.Clause;
import datastruct.ImplicationGraph;
import datastruct.Literal;

public class SolverUtil {
    public static List<Literal> getLiteralsWithoutAssignments(Clause c, ImplicationGraph g) {
        List<Literal> subset = new ArrayList<>();

        c.getLiterals().forEach(l -> {
            if (g.getAssignment(l.getName()) == null) {
                subset.add(l);
            }
        });

        return subset;
    }

    private Clause performResolution(Clause left, Clause right) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        Map<String, List<Literal>> variableToLiteralsMap = new HashMap<>();

        for (Literal l : left.getLiterals()) {
            if (!variableToLiteralsMap.containsKey(l.getName())) {
                variableToLiteralsMap.put(l.getName(), new ArrayList<>());
            }
            variableToLiteralsMap.get(l.getName()).add(l);
        }

        for (Literal l : right.getLiterals()) {
            if (!variableToLiteralsMap.containsKey(l.getName())) {
                variableToLiteralsMap.put(l.getName(), new ArrayList<>());
            }
            variableToLiteralsMap.get(l.getName()).add(l);
        }

        Clause resolutionClause = new Clause();

        for (String key : variableToLiteralsMap.keySet()) {
            List<Literal> literals = variableToLiteralsMap.get(key);
            if (literals.size() == 1) {
                resolutionClause.addLiteral(literals.get(0));
            }
        }
        return resolutionClause;
    }
}
