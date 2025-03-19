package me.mogubea.claims.flags;

import me.mogubea.commands.CommandException;

public class FlagMember extends Flag<MemberLevel> {
	
	public FlagMember(String name, String displayName, MemberLevel def) {
		super(name, displayName, def, false);
	}

	@Override
	public MemberLevel parseInput(String input) throws CommandException {
		String s = input.toUpperCase();
		MemberLevel ml = null;
		try {
			if (!(s.equals("NULL")))
				ml = MemberLevel.valueOf(s);
		} catch (Exception e) {
			throw new CommandException(null, "'"+input+"' is not a valid member level.");
		}
		return ml;
	}
	
	@Override
    public MemberLevel unmarshal(String o) {
        return MemberLevel.valueOf(o);
    }

    @Override
    public String marshal(MemberLevel o) {
        return o.name();
    }

	@Override
	public MemberLevel validateValue(MemberLevel o) {
		return o;
	}
	
}
