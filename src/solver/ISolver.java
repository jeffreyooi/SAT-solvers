package solver;

import java.io.IOException;

public interface ISolver {
    String evaluate();

    int getPickBranchingVariableCount();

    void setResultOutput(String outputPath) throws IOException;

    void setStatisticsOutput(String outputPath) throws IOException;

    void writeResult(String result) throws IOException;

    void logStatistics(String[] data) throws IOException;

    void finalize() throws IOException;

    void reset();
}
