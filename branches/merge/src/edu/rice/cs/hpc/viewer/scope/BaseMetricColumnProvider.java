package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.viewer.util.Utilities;

public abstract class BaseMetricColumnProvider extends ColumnLabelProvider {
	
	public Font getFont(Object element) {
		return Utilities.fontMetric;
	}

}
