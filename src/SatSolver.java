import static config.Config.Solver.CDCL_Chaff;
import static config.Config.Solver.CDCL_Random;
import static config.Config.Solver.CDCL_TwoClause;

import config.Config;
import db.ClauseDB;
import parser.DimacsParser;
import solver.CDCLSolver;
import solver.ISolver;
import solver.RandomSolver;
import solver.TwoClauseSolver;
import util.FileUtil;

public class SatSolver {

    private static final String USAGE_MSG = "Usage: <Solver type> <CNF file name>";
    private static final String INVALID_TYPE_MSG = "Invalid type passed: %s\nAvailable solvers:\n- CDCL_Chaff\n- CDCL_TwoClause";

    private ClauseDB clauseDb;

    private SatSolver() {
        clauseDb = new ClauseDB();
    }

    private Config.Solver parseSolverType(String strType) {
        if (strType.equals(CDCL_Chaff.toString())) {
            return CDCL_Chaff;
        } else if (strType.equals(CDCL_TwoClause.toString())) {
            return CDCL_TwoClause;
        } else if (strType.equals(Config.Solver.CDCL_Random.toString())) {
            return CDCL_Random;
        }
        return null;
    }

    private ISolver getSolver(Config.Solver solverType) {
        switch (solverType) {
            case CDCL_Chaff:
                return new CDCLSolver(clauseDb);
            case CDCL_TwoClause:
                return new TwoClauseSolver(clauseDb);
            case CDCL_Random:
                return new RandomSolver(clauseDb);
            default:
                return null;
        }
    }

    private void solve(String[] args) {
        Config.Solver solverType = parseSolverType(args[0]);

        if (solverType == null) {
            System.out.println(String.format(INVALID_TYPE_MSG, args[0]));
            return;
        }

        String filePath = args[1];

        String fileContent = FileUtil.getFileString(filePath);

        if (fileContent == null) {
            System.out.println("File does not exist.");
            return;
        }

        DimacsParser dp = new DimacsParser(clauseDb);
        boolean parseSuccessful = dp.parse(fileContent);

        if (!parseSuccessful) {
            System.out.println("cnf file not in the correct format");
            return;
        }

        ISolver solver = getSolver(solverType);
        if (solver == null) {
            return;
        }
        String result = solver.evaluate();
        System.out.println(result);
    }

    public void reset() {
        clauseDb.reset();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(USAGE_MSG);
            return;
        }

        SatSolver solver = new SatSolver();
        solver.solve(args);
    }
}
