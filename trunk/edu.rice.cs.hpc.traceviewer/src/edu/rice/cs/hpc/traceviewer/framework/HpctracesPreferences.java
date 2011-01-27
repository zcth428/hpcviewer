package edu.rice.cs.hpc.traceviewer.framework;

import org.eclipse.swt.graphics.Rectangle;

public class HpctracesPreferences {
	Rectangle preferredTraceViewportSize;
	
	public HpctracesPreferences(Rectangle rectangle) {
		preferredTraceViewportSize = rectangle;
	}
	public void setPreferredTraceViewportSize(Rectangle s) {
		preferredTraceViewportSize = s;
	}
	public Rectangle getPreferredTraceViewportSize() {
		return preferredTraceViewportSize;
	}
}