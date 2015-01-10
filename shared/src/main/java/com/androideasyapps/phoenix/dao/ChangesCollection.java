package com.androideasyapps.phoenix.dao;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChangesCollection<E extends AbstractBean> extends AbstractBean
		implements Collection<E>, Serializable, PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	protected Collection<E> managedCollection;

	protected Set<E> additions = new HashSet<E>();
	protected Set<E> deletions = new HashSet<E>();
	protected Set<E> updates = new HashSet<E>();

	public ChangesCollection() {
		this(new ArrayList<E>());
	}

	public ChangesCollection(Collection<E> collection) {
		this.managedCollection = collection;
	}

	public int size() {
		return managedCollection.size();
	}

	public boolean isEmpty() {
		return managedCollection.isEmpty();
	}

	public boolean contains(Object o) {
		return managedCollection.contains(o);
	}

	public Iterator<E> iterator() {
		return managedCollection.iterator();
	}

	public Object[] toArray() {
		return managedCollection.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return managedCollection.toArray(a);
	}

	public boolean add(E e) {
		if (managedCollection.add(e)) {
			additions.add(e);
			// we are changed
			setChanged(true);

			// register ourself for listeners on this object
			e.addPropertyChangeListener(this);

			// let other parent objects know we have changed
			pcs.firePropertyChange("add", null, e);
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		if (managedCollection.remove(o)) {
			setChanged(true);
			deletions.add((E) o);
			((E) o).removePropertyChangeListener(this);
			pcs.firePropertyChange("remove", null, o);
			return true;
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		return managedCollection.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		for (E o : c) {
			add(o);
		}
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		for (Object o : c) {
			remove(o);
		}
		return true;
	}

	public boolean retainAll(Collection<?> c) {
		// TODO: what to do here
		return managedCollection.retainAll(c);
	}

	public void clear() {
		managedCollection.clear();
	}

	public boolean equals(Object o) {
		return managedCollection.equals(o);
	}

	public int hashCode() {
		return managedCollection.hashCode();
	}

	/**
	 * This is called by child objects in our collection to let us know they
	 * have changed. We'll in turn let any of our listeners know via the
	 * "update" property change listener
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		E source = (E) evt.getSource();
		updates.add(source);
		// we are not specfic about "what" was updated, just that we were
		pcs.fireIndexedPropertyChange("update", -1, null, source);
	}

	@Override
	public void setChanged(boolean val) {
		super.setChanged(val);
		if (val == false) {
			// tell all child objects to reset their changed states
			for (E e : this) {
				if (e.isChanged()) {
					e.setChanged(false);
				}
			}
			// reset the state sets
			additions.clear();
			updates.clear();
			deletions.clear();
		}
	}

	public Set<E> getAdditions() {
		return additions;
	}

	public Set<E> getDeletions() {
		return deletions;
	}

	public Set<E> getUpdates() {
		return updates;
	}

	public void swapCollection(Collection<E> coll) {
		setChanged(false);
		this.managedCollection = coll;
	}
}
