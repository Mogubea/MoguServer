package me.mogubea.claims.flags;

import me.mogubea.commands.CommandException;

public class FlagBoolean extends Flag<Boolean> {

	public FlagBoolean(String name, String displayName, boolean def, boolean inherit) {
		super(name, displayName, def, inherit);
	}

	@Override
	public Boolean parseInput(String input) throws CommandException {
		String s = input.toLowerCase();
		if (s.equals("deny") || s.equals("false"))
			return false;
		if (s.equals("allow") || s.equals("true"))
			return true;
		if (s.equals("none") || s.equals("null"))
			return null;
		
		throw new CommandException(null, "'"+input+"' is not a valid boolean value.");
	}
	
	@Override
    public Boolean unmarshal(String o) {
		if (o == null) return null;
		return o.equalsIgnoreCase("true");
	}

    @Override
    public String marshal(Boolean o) {
		if (o == null) return null;
		return o ? "true" : "false";
	}

	@Override
	public Boolean validateValue(Boolean o) {
		return o;
	}
	
}
