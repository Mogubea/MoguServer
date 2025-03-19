package me.mogubea.claims.flags;

public enum MemberLevel {

	EVERYONE, // Non-members
	MEMBER, // Members
	TRUSTED, // Trusted members
	OWNER, // Owners
	NONE; // Absolutely nobody

	private final String niceName;

	MemberLevel() {
		niceName = this.name().charAt(0) + name().substring(1).toLowerCase();
	}

	public boolean is(MemberLevel level) {
		return this.ordinal() >= level.ordinal();
	}

	@Override
	public String toString() {
		return niceName;
	}
	
}
