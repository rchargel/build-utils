package net.zcarioca.maven;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;

import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;

public class CustomSiteSink extends SiteRendererSink {

    public CustomSiteSink(final RenderingContext renderingContext) {
        super(renderingContext);
    }

    public void style(final String style, final MutableAttributeSet attrs) {
        writeStartTag(Tag.STYLE, attrs);

        write(style);

        writeEndTag(Tag.STYLE);
    }

}
