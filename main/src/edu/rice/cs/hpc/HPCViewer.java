package edu.rice.cs.hpc;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class HPCViewer implements IApplication {

	private String[] checkArguments(IApplicationContext context) {
		String[] args = (String[])context.getArguments().get("application.args");
		if(args != null) {
			System.out.print("Arguments: ");
			for(int i=0;i<args.length;i++) {
				System.out.print(" "+args[i]);
			}
			System.out.println();
		}
		java.util.Map<String, String> map = context.getArguments();
		java.util.Collection<String> col = map.values();
		for(java.util.Iterator<String> i=col.iterator();i.hasNext();) {
			Object o = i.next();
			String s;
			if(o.getClass().isArray()) {
				Object []oo = (Object []) o;
				if(oo != null && oo.length>0 && oo[0] instanceof String) {
					for(int k=0;k<oo.length;k++) {
						System.out.println("\t"+oo[k]);
					}
				}
		} else {
				s = (String) o;
				System.out.println("\t"+s);
			}
		}
		System.out.println("hpcviewer: "+context.getBrandingApplication()+"\n"+context.getBrandingId()+
				"\n"+context.getBrandingName()+"\n");
		return args;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		String []args = this.checkArguments(context);
		try {		
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor(args));
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
