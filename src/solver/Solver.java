package solver;

import db.ClauseDB;

public abstract class Solver implements ISolver {
    ClauseDB db;

    public Solver(ClauseDB db) {
        this.db = db;
    }
}
