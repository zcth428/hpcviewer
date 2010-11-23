package edu.rice.cs.hpc.viewer.help;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.FileDialog;

import com.onpositive.richtexteditor.model.LayerManager;;

public class InBrowser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Shell shell = new Shell(SWT.SHELL_TRIM);
		GridLayout layout = new GridLayout(1, true);
		shell.setLayout(layout);
		final SimpleViewerHTML richTextViewer = new SimpleViewerHTML(shell, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		gridData.minimumWidth = 200;
		gridData.minimumHeight = 400;
		richTextViewer.getConfiguration().setCreateToolbar(false);
		richTextViewer.setEditable(false);
		richTextViewer.getControl().setLayoutData(gridData);		
		shell.setLayoutData(gridData);
		
		FileDialog objFileDlg = new FileDialog(shell);
		String sFile = objFileDlg.open();
		
		LayerManager objManager = richTextViewer.getLayerManager();
		objManager.openHTMLFile(sFile);
		
		Display display = shell.getDisplay();
		shell.pack();
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();		

	}

}
