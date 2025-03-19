package me.mogubea.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EmoteManager {

    private final Map<String, Emote> emoteMap;

    public EmoteManager() {
        Map<String, Emote> emotes = new HashMap<>();
        for (Emote emote : Emote.values())
            emote.getNames().forEach((name) -> emotes.put(name, emote));

        emoteMap = Map.copyOf(emotes);
    }

    public @Nullable Emote getEmote(@NotNull String name) {
        name = name.toLowerCase().replace(":", "");
        return emoteMap.get(name);
    }

}
