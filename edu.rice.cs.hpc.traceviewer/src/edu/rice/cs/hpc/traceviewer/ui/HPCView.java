package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

abstract public class HPCView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		setContextMenus();
	}

	
	
	/*************************************************************************
	 * add context menus for the canvas
	 *************************************************************************/
	private void setContextMenus() {
		
		final Action saveImage = new Action("Save image ...") {
			
			public void run() {
				
				FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
				dialog.setText("Save trace view ... ");
				dialog.setFilterExtensions(new String[] {"*.png"});
				String filename = dialog.open();
				if (filename == null) {
					return;
				}
				
				// get image data from the buffer
				ImageData data = getImageData();
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] {data};
				
				// save the data into a file with PNG format
				loader.save(filename, SWT.IMAGE_PNG);
			}
		};
		
		// add menus to the canvas
		MenuManager mnuMgr = new MenuManager();
		Menu menu = mnuMgr.createContextMenu(getMainControl());
		mnuMgr.add(saveImage);

		getMainControl().setMenu(menu);
	}
	
	abstract protected ImageData getImageData();
	abstract protected Control getMainControl();
}
