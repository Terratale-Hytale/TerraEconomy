package terratale.content.block;

import terratale.content.inline.InlineElement;
import java.util.List;

public record ParagraphBlock(List<InlineElement> inlines)
        implements MarkdownBlock {}
