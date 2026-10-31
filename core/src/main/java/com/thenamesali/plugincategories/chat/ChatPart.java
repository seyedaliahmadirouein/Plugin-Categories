package com.thenamesali.plugincategories.chat;

public final class ChatPart {

    private final String text;
    private final ColorSpec color;
    private final boolean bold;
    private final boolean italic;
    private final boolean underlined;
    private final boolean strikethrough;
    private final boolean obfuscated;
    private String clickCommand;
    private String hoverText;

    public ChatPart(String text, ColorSpec color) {
        this(text, color, false, false, false, false, false);
    }

    public ChatPart(String text, ColorSpec color, boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean obfuscated) {
        this.text = text == null ? "" : text;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    public ChatPart click(String command) {
        this.clickCommand = command;
        return this;
    }

    public ChatPart hover(String text) {
        this.hoverText = text;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public ColorSpec getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public boolean isUnderlined() {
        return this.underlined;
    }

    public boolean isStrikethrough() {
        return this.strikethrough;
    }

    public boolean isObfuscated() {
        return this.obfuscated;
    }

    public String getClickCommand() {
        return this.clickCommand;
    }

    public String getHoverText() {
        return this.hoverText;
    }
}
