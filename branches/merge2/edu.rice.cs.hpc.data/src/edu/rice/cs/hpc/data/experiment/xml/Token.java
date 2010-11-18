package edu.rice.cs.hpc.data.experiment.xml;

import java.util.Map;
import java.util.HashMap;

public class Token {
    // to add a new token, declare a new public static final int here
    // and add an appropriate `put' invocation in the static constructor,
    // which you may find below.  a benefit of doing it this way is that
    // if the element names happen to change, we can simply add aliases
    // for them here.  it also looks a lot cleaner with a Map than
    // a big chain of if-then-else's
    public static final int INVALID_ELEMENT_NAME = 0;

    public static final int HPCVIEWER = 1;
    
    public static final int CONFIG = 2;
    public static final int TITLE = 3;
    public static final int PATH = 4;
    public static final int REPLACE = 5;
    public static final int METRICS = 6;
    public static final int METRIC = 7;

    public static final int SCOPETREE = 10;
    public static final int PGM = 11;
    public static final int F   = 12;
    public static final int P   = 13;
    public static final int A   = 14;
    public static final int S   = 15;
    public static final int M   = 16;
    public static final int LM  = 17;
    public static final int L   = 18;
    public static final int G   = 19;

    // token types for callgraph files
    public static final int CSPROFILE       = 20;
    public static final int CSPROFILEHDR    = 21;
    public static final int TARGET          = 22;
    public static final int CSPROFILEPARAMS = 23;
    public static final int CSPROFILETREE   = 24;
    public static final int CALLSITE        = 25;
    public static final int PROCEDURE_FRAME = 26;
    public static final int STATEMENT       = 27;

    private static Map tokenmap;

    static {
        tokenmap = new HashMap();

        tokenmap.put("M", new Integer(M));
	tokenmap.put("G", new Integer(G));
        tokenmap.put("S", new Integer(S));
        tokenmap.put("LN", new Integer(S)); // S = LN (deprecated)
        tokenmap.put("L", new Integer(L));
        tokenmap.put("A", new Integer(A));
        tokenmap.put("P", new Integer(P));
        tokenmap.put("F", new Integer(F));
        tokenmap.put("LM", new Integer(LM));
        tokenmap.put("PGM", new Integer(PGM));
        tokenmap.put("TITLE", new Integer(TITLE));
        tokenmap.put("PATH", new Integer(PATH));
        tokenmap.put("METRIC", new Integer(METRIC));
        tokenmap.put("HPCVIEWER", new Integer(HPCVIEWER));
        tokenmap.put("CONFIG", new Integer(CONFIG));
        tokenmap.put("REPLACE", new Integer(REPLACE));
        tokenmap.put("METRICS", new Integer(METRICS));
        tokenmap.put("SCOPETREE", new Integer(SCOPETREE));
        tokenmap.put("CSPROFILE", new Integer(CSPROFILE));
        tokenmap.put("CSPROFILEHDR", new Integer(CSPROFILEHDR));
        tokenmap.put("TARGET", new Integer(TARGET));
        tokenmap.put("CSPROFILEPARAMS", new Integer(CSPROFILEPARAMS));
        tokenmap.put("CSPROFILETREE", new Integer(CSPROFILETREE));
        tokenmap.put("CALLSITE", new Integer(CALLSITE));
        tokenmap.put("PROCEDURE_FRAME", new Integer(PROCEDURE_FRAME));
        tokenmap.put("STATEMENT", new Integer(STATEMENT));
    }

    public static int map(String element) {
        Object o = tokenmap.get(element);

        if(o == null) {
            return Token.INVALID_ELEMENT_NAME;
        }
        else {
            return ((Integer)o).intValue();
        }
    }
}
