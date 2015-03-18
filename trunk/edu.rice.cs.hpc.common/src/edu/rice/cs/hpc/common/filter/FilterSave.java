package edu.rice.cs.hpc.common.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class FilterSave extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FilterMap map = FilterMap.getInstance();
		map.save();
		return null;
	}

}
