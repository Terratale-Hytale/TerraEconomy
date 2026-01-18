package terratale.content.block;

public sealed interface MarkdownBlock
        permits HeadingBlock, LineBreakBlock, ParagraphBlock, SeparatorBlock {}

