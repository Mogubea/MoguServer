package me.mogubea.statistics;

public abstract class DirtyVal<T extends Number> {
	
	boolean dirty;
	T value;
	
	public DirtyVal(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

	/**
	 * Update the {@link #value} stored. If it's different, flag this as {@link #dirty}.
	 * @return this
	 */
	public DirtyVal<T> setValue(T newValue) {
		boolean dirty = !value.equals(newValue);
		value = newValue;
		return setDirty(isDirty() || dirty);
	}

	public DirtyVal<T> setValue(T newValue, boolean dirty) {
		value = newValue;
		return setDirty(dirty);
	}

	public abstract DirtyVal<T> addToValue(T add);

	/**
	 * Get if the stored {@link #value} has been changed recently.
	 * @return {@link #dirty}
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Force {@link #dirty}.
	 * @return this
	 */
	public DirtyVal<T> setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

	@Override
	public String toString() {
		return value.toString();
	}
	
}
