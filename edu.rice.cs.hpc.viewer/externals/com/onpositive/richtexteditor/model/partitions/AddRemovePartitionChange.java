/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtexteditor.model.partitions;

/**
 * @author kor
 * Change class incapsulating adding/removing partition command
 */
public class AddRemovePartitionChange extends AbstractPartitionChange{

	boolean add;
	int offset;
	
	/**
	 * Basic constructor
	 * @param offset Change offset
	 * @param partition partition to add/remove
	 * @param add true if we should add partition, false - if remove
	 */
	public AddRemovePartitionChange(int offset,BasePartition partition,boolean add) {
		super(partition);
		if (!add){
			if (partition.index ==-1){
				throw new IllegalArgumentException();
			}
		}
		this.offset=offset;
		this.add=add;
	}

	

	//@Override
	protected void apply(PartitionDelta delta) {
		PartitionStorage storage=delta.getStorage();
		if (add){
			storage.insertPartition(offset, partition);
			delta.added(partition);
		}
		else{			
			if (partition.index==-1){
				BasePartition removePartition = storage.removePartition(offset);
				delta.removed(removePartition);
			}
			else{
				storage.removePartition(partition);
				delta.removed(partition);
			}
		}
		delta.getUndoChange().add(new AddRemovePartitionChange(offset,partition,!add));
	}

}
