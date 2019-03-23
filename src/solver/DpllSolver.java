package solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datastruct.Clause;
import datastruct.Literal;

public class DpllSolver implements ISolver {

    List<Clause> clauses;
    int numOfLiterals;

    Map<String, Boolean> assignments;

    public DpllSolver(List<Clause> clauses, int numOfLiterals) {
        this.clauses = clauses;
        this.numOfLiterals = numOfLiterals;
        assignments = new HashMap<>();
    }

    @Override
    public String evaluate() {
        for (Clause c : clauses) {
            for (Literal l : c.getLiterals()) {
                assignments.put(l.getName(), false);
            }
        }

        if (validate()) {
            return "SAT";
        }




        return "UNSAT";
    }

    private boolean validate() {
        for (Clause c : clauses) {
            for (Literal l : c.getLiterals()) {
                if ((l.isPositive() && !assignments.get(l))
                || (!l.isPositive() && assignments.get(l))) {
                    return false;
                }
            }
        }

        return true;
    }
}
