package com.androideasyapps.phoenix.dao;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class AbstractBean implements Serializable {
	private static final long serialVersionUID = 1L;

	protected boolean changed = false;
	protected transient PropertyChangeSupport pcs;

	public AbstractBean() {
		pcs = new PropertyChangeSupport(this);		
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String field,
			PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(field, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String field,
			PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	protected void onChanged(String prop, Object oldValue, Object newValue) {
		if (oldValue == null && newValue == null)
			return;
		if ((oldValue == null && newValue != null)
				|| (newValue == null && oldValue != null)
				|| !oldValue.equals(newValue)) {
			handleChange(prop, oldValue, newValue);
		}
	}

	protected void handleChange(String prop, Object oldValue, Object newValue) {
		changed = true;
		pcs.firePropertyChange(prop, oldValue, newValue);
	}

	public void setChanged(boolean val) {
		this.changed = val;
	}

	public boolean isChanged() {
		return changed;
	}
	
	/**
	 * provides a default implemenation for readObject so that we can recreate the transient fields.
	 * 
	 * @param ois
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		pcs = new PropertyChangeSupport(this);
	}
}
