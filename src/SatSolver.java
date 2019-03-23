import db.ClauseDB;
import parser.DimacsParser;
import solver.CDCLSolver;
import util.FileUtil;

public class SatSolver {

    private ClauseDB clauseDb;

    private SatSolver() {
        clauseDb = new ClauseDB();
    }

    private void solve() {
        String fileContent = FileUtil.getFileString("C:\\Users\\Jeffrey\\Desktop\\Projects\\CS4244\\cnf\\cnf-2.cnf");

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

        CDCLSolver cdclSolver = new CDCLSolver(clauseDb);
        String result = cdclSolver.evaluate();
        System.out.println(result);
    }

    public static void main(String[] args) {
       SatSolver solver = new SatSolver();
       solver.solve();
    }
}
