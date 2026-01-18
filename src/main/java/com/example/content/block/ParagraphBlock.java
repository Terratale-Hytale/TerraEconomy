package com.example.content.block;

import com.example.content.inline.InlineElement;
import java.util.List;

public record ParagraphBlock(List<InlineElement> inlines)
        implements MarkdownBlock {}
