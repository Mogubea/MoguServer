package me.mogubea.claims.flags;

public class FlagColour extends FlagInt {

	public FlagColour(String name, String displayName, int def) {
		super(name, displayName, def);
	}

	public FlagColour(String name, String displayName, int def, boolean inheritFromWorld) {
		super(name, displayName, def, inheritFromWorld);
	}
	
}
