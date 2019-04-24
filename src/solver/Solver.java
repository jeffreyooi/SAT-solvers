package solver;

import java.io.IOException;
import java.util.Set;

import callback.CSVLogger;
import callback.ResultOutput;
import datastruct.Clause;
import datastruct.Variable;
import db.ClauseDB;

abstract class Solver implements ISolver {
    static final String UNSAT = "UNSAT";

    private int pickBranchingVariableCount;

    private ResultOutput resultOutput;
    private CSVLogger logger;

    public void reset() {
        pickBranchingVariableCount = 0;
    }

    public void setResultOutput(String outputPath) throws IOException {
        resultOutput = new ResultOutput(outputPath);
        resultOutput.initialize();
    }

    public void setStatisticsOutput(String outputPath) throws IOException {
        logger = new CSVLogger(outputPath, ",");
        logger.initialize();
    }

    public void writeResult(String result) throws IOException {
        if (resultOutput == null) {
            return;
        }
        resultOutput.write(result);
    }

    public void logStatistics(String[] data) throws IOException {
        if (logger == null) {
            return;
        }
        logger.logNext(data);
    }

    public void finalize() throws IOException {
        if (resultOutput != null) {
            resultOutput.close();
        }

        if (logger != null) {
            logger.close();
        }
    }

    ClauseDB db;

    Solver(ClauseDB db) {
        this.db = db;
        pickBranchingVariableCount = 0;
    }

    abstract boolean unitPropagation(Set<Clause> clauses);

    Variable pickBranchingVariable() {
        pickBranchingVariableCount += 1;
        return null;
    }

    abstract int conflictAnalysis();

    abstract void backtrack(int level);

    public int getPickBranchingVariableCount() {
        return pickBranchingVariableCount;
    }
}
