package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;

import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;

/***
 * Base abstract experiment that handle only call path.
 * 
 * No metric is associated in this experiment
 * 
 */
public abstract class BaseExperiment implements IExperiment {

	/** The experiment's configuration. */
	protected ExperimentConfiguration configuration;

	protected RootScope rootScope;

	/** version of the database **/
	protected String version;

	private TraceAttribute attribute;

	/** The file stream (that was originally used for path resolving)*/
	protected InputStream fileExperiment;

	/**
	 * This is either the file that backs the stream or null (could possibly
	 * even be the socket, but not really needed). Used for resolving relative references.
	 */
	protected Object backingObject;

	/***
	 * the root scope of the experiment
	 * 
	 * @param the
	 *            root scope
	 */
	public void setRootScope(Scope rootScope) {
		this.rootScope = (RootScope) rootScope;
	}

	/***
	 * retrieve the root scope
	 */
	public Scope getRootScope() {
		return rootScope;
	}

	/****
	 * retrieve the root scope of caller tree (bottom-up view)
	 * 
	 * @return root scope
	 */
	public RootScope getCallerTreeRoot() {

		if (this.rootScope.getSubscopeCount() == 3) {

			Scope scope = this.rootScope.getSubscope(1);
			if (scope instanceof RootScope)
				return (RootScope) scope;

		}
		return null;
	}

	public TreeNode[] getRootScopeChildren() {
		return this.rootScope.getChildren();
	}

	/****
	 * open a database
	 * 
	 * @param fileExperiment
	 * @param userData
	 * @throws Exception
	 */
	public void open(File fileExperiment, IUserData userData) throws Exception {
		backingObject = fileExperiment;
		open(new FileInputStream(fileExperiment), userData, false,
				fileExperiment.toString());
	}

	/****
	 * open a database, possibly with parsing the metrics as well
	 * 
	 * @param fileExperiment
	 * @param userData
	 * @param withMetric
	 * @throws Exception
	 */
	public void open(InputStream fileExperiment, IUserData userData,
			boolean withMetric, String name) throws Exception {
		// protect ourselves against filename being `foo' with no parent
		// information whatsoever.
		this.fileExperiment = fileExperiment;

		// parsing may throw exceptions
		new ExperimentFileXML().parse(fileExperiment, name, this, withMetric,
				userData);
	}

	public void setVersion(String v) {
		this.version = v;
	}

	public void setTraceAttribute(TraceAttribute _attribute) {
		this.attribute = _attribute;
	}

	public TraceAttribute getTraceAttribute() {
		return this.attribute;
	}

	/*************************************************************************
	 * Returns the name of the experiment.
	 ************************************************************************/

	public String getName() {
		return this.configuration.getName();
	}

	/*************************************************************************
	 * Sets the experiment's configuration.
	 * 
	 * This method is to be called only once, during
	 * <code>Experiment.open</code>.
	 * 
	 ************************************************************************/

	public void setConfiguration(ExperimentConfiguration configuration) {
		this.configuration = configuration;
	}

	public ExperimentConfiguration getConfiguration() {
		return this.configuration;
	}

	/*************************************************************************
	 * Returns the default directory from which to resolve relative paths.
	 * 
	 ************************************************************************/
	// TODO: When we are getting the XML file remotely, what do we do with
	// this??
	// I think it is not called by HPC TraceViewer, so it shouldn't be a
	// problem, but I'm not positive...
	public File getDefaultDirectory() {
		if (backingObject instanceof File) {
			File backingFile = (File) backingObject;
			return backingFile.getAbsoluteFile().getParentFile();

		} else {
			System.err
					.println("Tired to get default directory when none exists.");
			return null;
		}

	}

}
