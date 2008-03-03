/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;

/**
 * @author laksono
 *
 */
public class IOUtilities {
    /**
     * Verify if the file exist or not
     * @param scope
     * @return
     */
    static public boolean isFileReadable(Scope scope) {
		SourceFile newFile = ((SourceFile)scope.getSourceFile());
		if((newFile != null && (newFile != SourceFile.NONE)
			|| (newFile.isAvailable()))  ) {
			if (newFile instanceof FileSystemSourceFile) {
				FileSystemSourceFile objFile = (FileSystemSourceFile) newFile;
				return objFile.isAvailable();
			}
		}
		return false;
    }

}
