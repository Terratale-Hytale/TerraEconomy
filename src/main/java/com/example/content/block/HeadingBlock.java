package com.example.content.block;

public non-sealed class HeadingBlock implements MarkdownBlock {

    private final int level; // 2, 3, 4
    private final String text;

    public HeadingBlock(int level, String text) {
        this.level = level;
        this.text = text;
    }

    public int level() {
        return level;
    }

    public String text() {
        return text;
    }
}
