package com.example.content;

import com.example.content.block.MarkdownBlock;
import java.util.List;

public class MarkdownSection {

    private final String title;
    private final List<MarkdownBlock> blocks;

    public MarkdownSection(String title, List<MarkdownBlock> blocks) {
        this.title = title;
        this.blocks = blocks;
    }

    public String title() {
        return title;
    }

    public List<MarkdownBlock> blocks() {
        return blocks;
    }
}
