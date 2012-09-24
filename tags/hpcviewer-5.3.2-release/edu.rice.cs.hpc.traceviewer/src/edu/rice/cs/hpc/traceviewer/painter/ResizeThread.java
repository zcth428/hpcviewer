package edu.rice.cs.hpc.traceviewer.painter;

public class ResizeThread implements Runnable {
	private BufferPaint buffering;
	
	public ResizeThread(BufferPaint _buffering) {
		this.buffering = _buffering;
	}
	
	public void run() {
		buffering.rebuffering();
	}

}
