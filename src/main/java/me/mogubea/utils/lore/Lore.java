package me.mogubea.utils.lore;

import me.mogubea.utils.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Lore {

    protected final List<TextComponent> lore;
    protected final String baseContent;

    public Lore(List<TextComponent> lore) {
        this.lore = lore;
        this.baseContent = "";
    }

    protected Lore(PersistentLoreBuilder builder) {
        if (builder.compact) {
            TextComponent component = Component.empty();
            int size = builder.lore.size();
            for (int x = -1; ++x < size;) {
                component = component.append(builder.lore.get(x));
                if (x + 1 < size)
                    component = component.append(Component.newline());
            }

            for (TextComponent comp : builder.lore)
                component = component.append(Component.text("\n").append(comp));

            lore = List.of(component);
        } else {
            lore = List.copyOf(builder.lore);
        }

        StringBuilder content = new StringBuilder();
        int size = builder.content.length;
        for (int x = -1; ++x < size;) {
            content.append(builder.content[x]);
            if (x + 1 >= size) continue;
            content.append("\n");
        }

        baseContent = content.toString();
    }

    @NotNull
    public List<TextComponent> getLore() {
        return lore;
    }

    @NotNull
    public List<Component> getLoree() {
        return new ArrayList<>(lore);
    }

    @NotNull
    public String getBaseContent() {
        return baseContent;
    }

    /**
     * Replaces the string "{<b>idx</b>}" with the corresponding replacement text from the array (e.g. "{1} will be replaced by replacements[1]").
     */
    @NotNull
    public List<TextComponent> getLore(String... replacements) {
        int rSize = replacements.length;
        if (rSize < 1) return lore;

        List<TextComponent> clone = new ArrayList<>(lore);
        int size = clone.size();
        for (int r = -1; ++r < rSize;) {
            TextReplacementConfig config = TextReplacementConfig.builder().matchLiteral("{"+r+"}").replacement(replacements[r]).build();
            for (int x = -1; ++x < size;)
                clone.set(x, (TextComponent) clone.get(x).replaceText(config));
        }

        return clone;
    }

    /**
     * Replaces the string "{<b>idx</b>}" with the corresponding replacement text from the array (e.g. "{1} will be replaced by replacements[1]").
     */
    @NotNull
    public List<TextComponent> getLore(float... replacements) {
        int rSize = replacements.length;
        if (rSize < 1) return lore;

        List<TextComponent> clone = new ArrayList<>(lore);
        int size = clone.size();
        for (int r = -1; ++r < rSize;) {
            TextReplacementConfig config = TextReplacementConfig.builder().matchLiteral("{"+r+"}").replacement(replacements[r] + "").build();
            for (int x = -1; ++x < size;)
                clone.set(x, (TextComponent) clone.get(x).replaceText(config));
        }

        return clone;
    }

    /**
     * Replaces the string "{<b>idx</b>}" with the corresponding replacement text from the array (e.g. "{1} will be replaced by replacements[1]").
     */
    @NotNull
    public List<TextComponent> getLore(int... replacements) {
        int rSize = replacements.length;
        if (rSize < 1) return lore;

        List<TextComponent> clone = new ArrayList<>(lore);
        int size = clone.size();
        for (int r = -1; ++r < rSize;) {
            TextReplacementConfig config = TextReplacementConfig.builder().matchLiteral("{"+r+"}").replacement(replacements[r] + "").build();
            for (int x = -1; ++x < size;)
                clone.set(x, (TextComponent) clone.get(x).replaceText(config));
        }

        return clone;
    }

    /**
     * To be used when creating lore for actual items.
     */
    public static PersistentLoreBuilder getBuilder(String... content) {
        return new PersistentLoreBuilder(content);
    }

    public static List<TextComponent> fastBuild(boolean format, int wrapLength, String... content) {
        PersistentLoreBuilder builder = new PersistentLoreBuilder(content);
        if (!format) builder.dontFormatColours();
        builder.setLineLimit(wrapLength);

        return builder.build().getLore();
    }

    public static class PersistentLoreBuilder {

        private int maximumLineLength = 34;
        private int currentLineLength;

        private TextColor ifMissing = NamedTextColor.GRAY;
        private boolean formatColours = true;

        private final List<TextComponent> lore = new ArrayList<>();
        private final String[] content;
        private boolean compact = false;

        private TextComponent currentLine = Component.empty();
        private TextComponent currentComponent = Component.empty();

        /**
         * Converts an array of strings into a {@link TextComponent} list designed for permanent items. Converts traditional formatting, for example <b>&a</b> into
         * a {@link TextComponent} with the {@link NamedTextColor#GREEN} colour. But can also create custom colours using <b>&#RRGGBB</b>.
         */
        public PersistentLoreBuilder(@NotNull String[] content) {
            this.content = content;
        }

        /**
         * The final list of {@link TextComponent}'s will only have 1 entry containing every component separated by \n character.
         */
        public PersistentLoreBuilder setCompact() {
            this.maximumLineLength = 500;
            this.compact = true;
            return this;
        }

        public PersistentLoreBuilder dontFormatColours() {
            this.formatColours = false;
            return this;
        }

        /**
         * Sets the maximum amount of characters per line.
         */
        public PersistentLoreBuilder setLineLimit(int characterLimit) {
            this.maximumLineLength = characterLimit;
            return this;
        }

        /**
         * Similar functionality to {@link Component#colorIfAbsent(TextColor)}
         */
        public PersistentLoreBuilder colorIfAbsent(TextColor color) {
            this.ifMissing = color;
            return this;
        }

        private void appendComponent() {
            if (currentComponent == null || currentComponent.content().isEmpty()) return;

            currentLine = currentLine == null ? currentComponent : currentLine.append(currentComponent);
            currentComponent = currentComponent.content(""); // Should carry over styles to the next line
        }

        private void finishLine(boolean addEmpty) {
            appendComponent();

            if (currentLine != null) {
                lore.add(currentLine.colorIfAbsent(ifMissing).decoration(TextDecoration.ITALIC, false));
                currentLine = Component.empty();
            }

            if (addEmpty)
                lore.add(Component.empty());

            currentLineLength = 0;
        }

        public Lore build() {
            int size = content.length;

            for (int x = -1; ++x < size;) {
                String string = content[x];
                if (x > 0)
                    finishLine(false);

                // Finish line, add empty line, continue
                if (string == null || string.isEmpty()) {
                    finishLine(true);
                    continue;
                }

                String[] split = string.split(" ");
                int sSize = split.length;

                // Scan each word for length...
                for (int s = -1; ++s < sSize;) {
                    @NotNull String word = split[s];
                    int length = word.length();

                    // Scan for colours and decoration in the word...
                    if (formatColours) {
                        StringBuilder newWord = new StringBuilder();
                        char[] b = word.toCharArray();
                        int charLength = b.length;
                        for(int i = -1; ++i < charLength;) {
                            if (b[i] == ' ' && currentLineLength <= 0) continue;

                            if (b[i] == '\n') {
                                // Split the word
                                if (newWord.length() > 0) {
                                    if (currentLineLength + length > maximumLineLength) // Current portion of the word is over length limit
                                        finishLine(false);

                                    currentComponent = currentComponent.content(currentComponent.content() + newWord);
                                    newWord = new StringBuilder();
                                }

                                finishLine(false);
                                length = 0;
                            } else if (b[i] == '&' && (i + 1 < charLength)) {
                                String check = "0123456789AaBbCcDdEeFfLlMmNnOoRr#";
                                int idx = check.indexOf(b[i + 1]);

                                if (idx <= -1) {
                                    newWord.append(b[i]);
                                    continue;
                                }

                                // Split the word
                                if (newWord.length() > 0) {
                                    if (currentLineLength + length > maximumLineLength) // Current portion of the word is over length limit
                                        finishLine(false);

                                    currentComponent = currentComponent.content(currentComponent.content() + newWord);
//                                    length += newWord.length();
                                    newWord = new StringBuilder();
                                }

                                // Finish previous component if not empty
                                appendComponent();

                                // Numbers
                                i++;

                                // Do charAt instead of case checking idx for readability's sake
                                switch (check.charAt(idx)) {
                                    case 'L', 'l' -> currentComponent = currentComponent.decoration(TextDecoration.BOLD, true);
                                    case 'M', 'm' -> currentComponent = currentComponent.decoration(TextDecoration.STRIKETHROUGH, true);
                                    case 'N', 'n' -> currentComponent = currentComponent.decoration(TextDecoration.UNDERLINED, true);
                                    case 'O', 'o' -> currentComponent = currentComponent.decoration(TextDecoration.ITALIC, true);
                                    case 'R', 'r' -> currentComponent = Component.text(currentComponent.content()); // Clears all formatting
                                    /*case 'T', 't' -> { TODO: translatable
                                        int remaining = b.length - (i + 1);


                                        appendComponent();
                                    }*/
                                    case '#' -> {
                                        int remaining = Math.min(6, b.length - (i + 1));
                                        StringBuilder colourWord = new StringBuilder();
                                        int counted = 0;

                                        for (int c = -1; ++c < remaining;) {
                                            if ("0123456789AaBbCcDdEeFf".indexOf(b[i + 1 + c]) == -1) break;
                                            colourWord.append(b[i + 1 + c]);
                                            counted++;
                                        }

                                        try {
                                            int colour = Integer.decode("0x" + colourWord);
                                            currentComponent = currentComponent.color(TextColor.color(colour));
                                        } catch (Exception e) {
                                            currentComponent = currentComponent.color(null);
                                        }

                                        length -= counted;
                                        i += counted;
                                    }
                                    default -> currentComponent = Component.text(currentComponent.content(), ChatColor.namedOfChar(b[i])); // Clears previous formatting
                                }

                                length -= 2;
                            } else {
                                newWord.append(b[i]);
                            }
                        }
                        word = newWord.toString();
                    }

                    if (currentLineLength + length > maximumLineLength) // Word is over length limit
                        finishLine(false);

                    if (s < (sSize - 1)) { // Before last word
                        if (currentLineLength + 1 <= maximumLineLength) {
                            currentComponent = currentComponent.content(currentComponent.content() + word + " ");
                            length++;
                        }
                    } else { // Last Word
                        currentComponent = currentComponent.content(currentComponent.content() + word);
                    }

                    currentLineLength += length;
                }
            }

            finishLine(false);
            return new Lore(this);
        }

    }

}
