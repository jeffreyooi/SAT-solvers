package config;

public class Config {
    public enum Solver {
        CDCL_Chaff,
        CDCL_TwoClause,
        CDCL_NClause,
        CDCL_Random,
        CDCL_VSDIS,
    }

    public enum Logging {
        NONE,
        VERBOSE,
        DEBUG,
    }

    public static Solver solver = Solver.CDCL_Chaff;
    public static Logging logging = Logging.VERBOSE;
}
