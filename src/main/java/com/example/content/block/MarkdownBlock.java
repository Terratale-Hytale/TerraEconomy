package com.example.content.block;

public sealed interface MarkdownBlock
        permits HeadingBlock, LineBreakBlock, ParagraphBlock, SeparatorBlock {}

