package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;




public class RemoteFilteredBaseData implements IFilteredData {

	private static final int FILTER = 0x464C5452;
	private final TraceName[] allNames;
	private int[] indexes;
	private final int headerSize;
	private final DataOutputStream server;
	FilterSet filter;
	
	public RemoteFilteredBaseData(TraceName[] names, int _headerSz, DataOutputStream server) {
		allNames = names;
		headerSize = _headerSz;
		this.server = server;
	}
	@Override
	public void setFilter(FilterSet filter) {
		this.filter = filter;
		applyFilter();
	}


	private void applyFilter() {
		ArrayList<Integer> lindexes = new ArrayList<Integer>();

	
		for (int i = 0; i < allNames.length; i++) {
			if (filter.include(allNames[i]))
				lindexes.add(i);
		}

		indexes = new int[lindexes.size()];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = lindexes.get(i);
		}

		try {
			server.writeInt(FILTER);
			ArrayList<Filter> pat = filter.getPatterns();
			server.write(0);
			server.write(filter.isShownMode()? 0 : 1);
			server.writeShort(pat.size());
			for (Filter filter : pat) {
				filter.serializeSelfToStream(server);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Override
	public FilterSet getFilter() {
		return filter;
	}

	@Override
	public int getHeaderSize() {
		return headerSize;
	}

	@Override
	public String[] getListOfRanks() {
		//This is already an O(n) operation so it's okay that we are recomputing the strings.
		String[] list = new String[getNumberOfRanks()];
		for (int i = 0; i < list.length; i++) {
			list[i] = allNames[indexes[i]].toString();
		}
		return list;
	}

	@Override
	public int getNumberOfRanks() {
		return indexes.length;
	}

	@Override
	public void dispose() {
		// Do nothing. The local version disposes the BaseDataFile. The rough
		// equivalent would be to dispose the RemoteDataRetriever, but that is
		// done elsewhere. Plus, because RemoteDataRetriever is in traceviewer,
		// we can't access it here.
	}

	@Override
	public boolean isGoodFilter() {
		return getNumberOfRanks() > 0;
	}

}
