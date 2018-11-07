package org.icddrb.mamonimncsp;

/**
 * Created by MCHD on 3/28/2018.
 */

public class MyMapping {

    String variablename;
    String clause;
    String removalvalue;

    public String getVariablename() {
        return variablename;
    }

    public void setVariablename(String variablename) {
        this.variablename = variablename;
    }

    public String getClause() {
        return clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
    }

    public String getRemovalvalue() {
        return removalvalue;
    }

    public void setRemovalvalue(String removalvalue) {
        this.removalvalue = removalvalue;
    }

    public MyMapping(String variablename, String clause, String removalvalue) {
        this.variablename = variablename;
        this.clause = clause;

        this.removalvalue = removalvalue;
    }
}
