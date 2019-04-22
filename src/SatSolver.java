import static config.Config.Solver.CDCL_Chaff;
import static config.Config.Solver.CDCL_NClause;
import static config.Config.Solver.CDCL_Random;
import static config.Config.Solver.CDCL_TwoClause;
import static config.Config.Solver.CDCL_VSDIS;

import java.io.IOException;

import config.Config;
import db.ClauseDB;
import parser.DimacsParser;
import solver.CDCLSolver;
import solver.ISolver;
import solver.NClauseSolver;
import solver.RandomSolver;
import solver.TwoClauseSolver;
import solver.VSDISSolver;
import util.FileUtil;
import util.SolverUtil;

public class SatSolver {

    private static final String USAGE_MSG
            = "Usage: <Solver type> <CNF file name> <Number of iterations> [Logging] [Statistic log output] "
            + "[Result output]";
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
        } else if (strType.equals(CDCL_NClause.toString())) {
            return CDCL_NClause;
        } else if (strType.equals(CDCL_Random.toString())) {
            return CDCL_Random;
        } else if (strType.equals(CDCL_VSDIS.toString())) {
            return CDCL_VSDIS;
        }
        return null;
    }

    private ISolver getSolver(Config.Solver solverType) {
        switch (solverType) {
            case CDCL_Chaff:
                return new CDCLSolver(clauseDb);
            case CDCL_TwoClause:
                return new TwoClauseSolver(clauseDb);
            case CDCL_NClause:
                return new NClauseSolver(clauseDb);
            case CDCL_Random:
                return new RandomSolver(clauseDb);
            case CDCL_VSDIS:
                return new VSDISSolver(clauseDb);
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
        int numberOfIterations =  Integer.parseInt(args[2]);

        try {
            if (args.length >= 4) {
                solver.setStatisticsOutput(args[3]);
                solver.logStatistics(new String[]{"Iteration", "Time (s)"});
            }

            if (args.length >= 5) {
                solver.setResultOutput(args[4]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < numberOfIterations; ++i) {
            long time = System.nanoTime();
            String result = solver.evaluate();
            long totalTime = System.nanoTime() - time;
            if (Config.logging == Config.Logging.VERBOSE) {
                System.out.println("Total time: " + SolverUtil.millisecToString(totalTime));
                System.out.println("Pick branching variable count: " + solver.getPickBranchingVariableCount());
                System.out.println(result);
            }
            try {
                String[] stat = new String[]{String.valueOf(i + 1), SolverUtil.millisecToString(totalTime)};
                solver.logStatistics(stat);
                solver.writeResult(String.format("%d\n", i + 1));
                solver.writeResult(result);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            solver.reset();
        }

        try {
            solver.finalize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println(USAGE_MSG);
            return;
        }

        SatSolver solver = new SatSolver();
        solver.solve(args);
    }
}
