package com.androideasyapps.phoenix.dao;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChangesBean extends AbstractBean implements Serializable,
		PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	protected Map<String, Object> changes = new HashMap<String, Object>();

	public ChangesBean() {
		super();	
	}

	public Map<String, Object> getChanges() {
		return changes;
	}

	public void setChanged(boolean val) {
		if (val == false) {
			changes.clear();
		}
		super.setChanged(val);
	}

	@Override
	protected void handleChange(String prop, Object oldValue, Object newValue) {
		changes.put(prop, newValue);
		super.handleChange(prop, oldValue, newValue);
	}

	/**
	 * NOTE: This is called when are are listening for changes on OTHER objects,
	 * not our object.
	 * 
	 * @param evt
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		setChanged(true);
	}
}
