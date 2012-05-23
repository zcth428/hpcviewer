package edu.rice.cs.hpc.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import edu.rice.cs.hpc.data.util.IUserData;


/***
 * 
 * Mapping a name to another alias
 * This is useful when we want to change a name of a procedure X to Y (for display only) 
 *
 */
public abstract class AliasMap implements IUserData {
	
	protected HashMap<String, String> data;
	
	
	/***
	 * read map or create file if it doesn't exist
	 */
	public AliasMap() {

		final String filename = getFilename();
		File file = new File( filename );
		
		if (file.canRead()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
				Object o = in.readObject();
				if (o instanceof HashMap) {
					data = (HashMap<String, String>) o;
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (!file.exists()) {
			// file doesn't exist, but we can create
			data = new HashMap<String, String>();
			
			// init data
			initDefault();
			
			save();
		}
	}
		
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.util.IUserData#get(java.lang.String)
	 */
	public String get(String key) {
		
		if (data != null) {
			return data.get(key);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.util.IUserData#put(java.lang.String, java.lang.String)
	 */
	public void put(String key, String val) {
		
		if (data == null) {
			data = new HashMap<String, String>();
		}
		data.put(key, val);
		
		save();
	}

	private void save() {
		
		final String filename = getFilename();
		final File file = new File(filename);
		
		{
			try {
				ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream(file.getAbsoluteFile()) );
				out.writeObject(data);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

	abstract public String getFilename();
	abstract public void initDefault();

}
