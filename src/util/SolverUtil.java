package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datastruct.Clause;
import datastruct.Literal;

public class SolverUtil {
    public static Clause performResolution(Clause left, Clause right) {
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
        Set<Literal> literalsSet = new HashSet<>();

        literalsSet.addAll(left.getLiterals());
        literalsSet.addAll(right.getLiterals());

        for (Literal l : literalsSet) {
            if (!variableToLiteralsMap.containsKey(l.getName())) {
                variableToLiteralsMap.put(l.getName(), new ArrayList<>());
            }
            if (!variableToLiteralsMap.get(l.getName()).contains(l)) {
                variableToLiteralsMap.get(l.getName()).add(l);
            }
        }

        Clause resolutionClause = new Clause();

        for (String key : variableToLiteralsMap.keySet()) {
            List<Literal> literals = variableToLiteralsMap.get(key);
            if (literals.size() == 1) {
                resolutionClause.addLiteral(literals.get(0));
            }
        }

        System.out.println(
                String.format("Resolve %s with %s \n --> %s\n",
                        left.toString(),
                        right.toString(),
                        resolutionClause.toString()));

        return resolutionClause;
    }
}
