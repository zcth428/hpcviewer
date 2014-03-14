package edu.rice.cs.hpc.common.util;

import java.util.Hashtable;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;

/******************************************************
 * 
 * Managing sleak objects across different windows 
 *
 ******************************************************/
public class SleakManager {

	private static final String VAR_MEMLEAK = "HPCTOOLKIT_MEMLEAK";
	
	static final private Hashtable<Display, Sleak> lists = new Hashtable<Display, Sleak>(1);
	
	
	/************
	 * retrieve the sleak object of this display iff HPCTOOLKIT_MEMLEAK has value "1" or "T"
	 * 
	 * @param display
	 * 
	 * @return sleak object if the environement variable is true, null otherwise
	 ************/
	static public Sleak getSleak(final Display display) {
		
		Sleak sleak = null;

		String mem = System.getenv(VAR_MEMLEAK);
		
		// we activate sleak if the value of variable HPCTOOLKIT_MEMLEAK is not "f" or "0"
		
		if ( mem != null &&  !mem.isEmpty()) {
			boolean memleak = !(mem.equalsIgnoreCase("f") || mem.equals("0"));
			
			if (memleak) {
				// memleak is on. Check if it's already on for this display.
				// we don't want to have two sleaks work on the same display
				
				sleak = lists.get(display);

				if (sleak == null) {
					sleak = new Sleak();
					
					DeviceData data = display.getDeviceData();
					data.tracking = true;
					
					lists.put(display, sleak);
				}
			}
		}
		return sleak;
	}
	
	
	/************
	 * initialize sleak. If sleak is not activated by the environment variable HPCTOOLKIT_MEMLEAK,
	 * it won't be started.
	 * 
	 * @param display
	 ************/
	static public void init(final Display display) {
		Sleak sleak = getSleak( display );
		if (sleak != null) {
			sleak.open();
		}
	}
}
