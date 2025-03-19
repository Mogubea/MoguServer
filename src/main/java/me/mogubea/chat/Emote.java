package me.mogubea.chat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum Emote {

    MOGU_ANGY("MOGUANGY", '\uF000', "A very angy Mogubea.", "angy", "moguangy", "mogubeangy", "mogubangy", "bangy");

    private final char unicode;
    private final String identifier;
    private final String description;
    private final List<String> names;

    Emote(@NotNull String identifier, char unicode, @NotNull String description, String... names) {
        this.identifier = identifier;
        this.unicode = unicode;
        this.description = description;

        // Force lowercase just in case
        ArrayList<String> list = new ArrayList<>();
        for (String name : names)
            list.add(name.toLowerCase());

        this.names = List.copyOf(list);
    }

    public @NotNull String getIdentifier() { return identifier; }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull List<String> getNames() {
        return names;
    }

    public char getUnicode() {
        return unicode;
    }

}
