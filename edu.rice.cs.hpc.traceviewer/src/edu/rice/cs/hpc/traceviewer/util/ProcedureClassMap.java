package edu.rice.cs.hpc.traceviewer.util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.rice.cs.hpc.common.util.AliasMap;


/***
 * 
 * Class to manage map between a procedure and its class
 * For instance, we want to class all MPI_* into mpi class, 
 * 	the get() method will then return all MPI functions into mpi
 *
 */
public class ProcedureClassMap extends AliasMap {

	static public final String CLASS_IDLE = "idle";
	static private final String FILENAME = "class.map";
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.ProcedureMap#getFilename()
	 */
	public String getFilename() {
		
		IPath path = Platform.getLocation().makeAbsolute();
		return path.append(FILENAME).makeAbsolute().toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.ProcedureMap#initDefault()
	 */
	public void initDefault() {

		this.put("GPU_IDLE", CLASS_IDLE);
		this.put("cudaEventSynchronize", CLASS_IDLE);
		this.put("cudaStreamSynchronize", CLASS_IDLE);
		this.put("cudaDeviceSynchronize", CLASS_IDLE);
		this.put("cudaThreadSynchronize", CLASS_IDLE);
		this.put("cuStreamSynchronize", CLASS_IDLE);
		this.put("cuEventSynchronize", CLASS_IDLE);
		this.put("cuCtxSynchronize", CLASS_IDLE);
	}
	
	public Object[] getEntrySet() {
		return data.entrySet().toArray();
	}
}
