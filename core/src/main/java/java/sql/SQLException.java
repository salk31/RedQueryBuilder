package java.sql;

public class SQLException extends Exception {
    public SQLException(String m) {
        super(m);
    }

    public SQLException(String m, Throwable t) {
        super(m, t);
    }


    public int getErrorCode() {
        return 0;
    }
}
