package me.mogubea.statistics;

public class DirtyLong extends DirtyVal<Long> {

	public DirtyLong(long value) {
		super(value);
	}

	@Override
	public DirtyVal<Long> addToValue(Long add) {
		return null;
	}

	@Override
	public DirtyLong setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

}
