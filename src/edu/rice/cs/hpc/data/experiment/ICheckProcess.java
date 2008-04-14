/**
 * 
 */
package edu.rice.cs.hpc.data.experiment;

/**
 * @author laksono
 *
 */
public interface ICheckProcess {
	
	/**
	 * The previous process has been done, and ready to go for the
	 * next task 
	 * @param str
	 */
	public void advance(String str);
}
