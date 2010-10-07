package edu.rice.cs.hpc.viewer.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.Preferences ;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class UserInputHistory {
    protected static final String HISTORY_NAME_BASE = "history."; //$NON-NLS-1$
    protected final static String ENCODING = "UTF-8";
    
    protected String name;
    protected int depth;
    protected List<String> history;


    public UserInputHistory(String name) {
        this(name, 50);
    }

    public UserInputHistory(String name, int depth) {
        this.name = name;
        this.depth = depth;
        
        this.loadHistoryLines();
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getDepth() {
        return this.depth;
    }
    
    public String []getHistory() {
        return (String [])this.history.toArray(new String[this.history.size()]);
    }
    
    public void addLine(String line) {
        if (line == null || line.trim().length() == 0) {
            return;
        }
    	this.history.remove(line);
        this.history.add(0, line);
        if (this.history.size() > this.depth) {
            this.history.remove(this.history.size() - 1);
        }
        this.saveHistoryLines();
    }
    
    public void clear() {
        this.history.clear();
        this.saveHistoryLines();
    }

    
    static public Preferences getPreference() {
    	IPreferencesService service = Platform.getPreferencesService();
    	if (service != null) {
        	IEclipsePreferences pref = service.getRootNode();
        	return pref;
    	}
    	return null;
    }
    
    protected void loadHistoryLines() {
        this.history = new ArrayList<String>();
        String historyData = getPreference().get(UserInputHistory.HISTORY_NAME_BASE + this.name, "");
        if (historyData != null && historyData.length() > 0) {
            String []historyArray = historyData.split(";"); //$NON-NLS-1$
            for (int i = 0; i < historyArray.length; i++) {
            	try {
            		historyArray[i] = new String(historyArray[i].getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
                } catch (UnsupportedEncodingException e) {
                	historyArray[i] = new String(historyArray[i].getBytes());
                }
            }
            this.history.addAll(Arrays.asList(historyArray));
        }
    }
    
    protected void saveHistoryLines() {
        String result = ""; //$NON-NLS-1$
        for (Iterator<String> it = this.history.iterator(); it.hasNext(); ) {
            String str = (String)it.next();
            try {
				str = new String(str.getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
			} catch (UnsupportedEncodingException e) {
				str = new String(str.getBytes());
			}
            result += result.length() == 0 ? str : (";" + str); //$NON-NLS-1$
        }
        getPreference().put(UserInputHistory.HISTORY_NAME_BASE + this.name, result);
    }

}
