package me.mogubea.guilds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExperienceSource {

    QUEST("QUEST"),
    TRADE("TRADE"),
    DAILY_QUEST("DAILY"),
    WEEKLY_QUEST("WEEKLY"),
    LOYALTY_BONUS("LOYALTY");

    private final String identifier;

    ExperienceSource(@NotNull String identifier) {
        this.identifier = identifier;
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }

    public static @Nullable ExperienceSource fromIdentifier(@NotNull String s) {
        for (ExperienceSource type : values())
            if (type.getIdentifier().equals(s.toUpperCase()))
                return type;
        return null;
    }

}
