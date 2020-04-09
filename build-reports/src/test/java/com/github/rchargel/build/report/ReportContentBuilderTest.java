package com.github.rchargel.build.report;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReportContentBuilderTest {
    @Test
    public void canAccessBuildersFromJava() {
        final Map<String, Object> row = new HashMap<>();
        row.put("Heading 1", 123);
        row.put("Heading 2", "Value");
        row.put("Heading 3", null);

        final Section section = Section.builder("Title")
                .subTitle("First heading")
                .appendContent(Section.builder("Sub Title 1")
                        .appendContent(Text.builder("Paragraph").build())
                        .appendContent(Text.builder("Paragraph").title("Paragraph Title").build())
                        .build())
                .appendContent(Section.builder("Sub Title 2")
                        .build())
                .appendContent(Section.builder("Sub Title 3")
                        .appendContent(Image.builder()
                                .data(new byte[0])
                                .contentType("image/jpeg")
                                .title("Image 1")
                                .thumbnail(true)
                                .build())
                        .appendContent(Image.builder()
                                .data(new byte[0])
                                .build())
                        .appendContent(Image.COLLAPSED_ICON)
                        .build())
                .appendContent(Section.builder("Sub Title 4")
                        .appendContent(Table.builder()
                                .renderHeadings(false)
                                .headingsOnLeft(true)
                                .headings(Arrays.asList("Heading 1", "Heading 2"))
                                .addHeading("Heading 3")
                                .addRow(row)
                                .addCellValue("Heading 1", 321)
                                .addCellValue("Heading 2", null)
                                .addCellValue("Heading 3", 432.0)
                                .endRow()
                                .addCellValue("Heading 2", "Final row")
                                .tableName("Table 1.1")
                                .caption("A table")
                                .build())
                        .build())
                .build();

        assertEquals("Title", section.getTitle());
        assertEquals("First heading", section.getSubTitle());
        assertEquals(4, section.getContent().size());

        // test subsection 1
        Section subSection = (Section) section.getContent().get(0);
        assertNotNull(subSection);
        assertEquals("Sub Title 1", subSection.getTitle());
        assertNull(subSection.getSubTitle());
        assertEquals(2, subSection.getContent().size());
        assertEquals(new Text("Paragraph", null), subSection.getContent().get(0));
        assertEquals(new Text("Paragraph", "Paragraph Title"), subSection.getContent().get(1));

        // test subsection 2
        subSection = (Section) section.getContent().get(1);
        assertNotNull(subSection);
        assertEquals("Sub Title 2", subSection.getTitle());
        assertNull(subSection.getSubTitle());
        assertEquals(0, subSection.getContent().size());

        // test subsection 3
        subSection = (Section) section.getContent().get(2);
        assertNotNull(subSection);
        assertEquals("Sub Title 3", subSection.getTitle());
        assertNull(subSection.getSubTitle());
        assertEquals(3, subSection.getContent().size());
        assertEquals("image/jpeg", ((Image) subSection.getContent().get(0)).getContentType());
        assertEquals("Image 1", ((Image) subSection.getContent().get(0)).getTitle());
        assertArrayEquals(new byte[0], ((Image) subSection.getContent().get(0)).getData());
        assertTrue(((Image) subSection.getContent().get(0)).getThumbnail());
        assertEquals("image/png", ((Image) subSection.getContent().get(1)).getContentType());
        assertNull(((Image) subSection.getContent().get(1)).getTitle());
        assertArrayEquals(new byte[0], ((Image) subSection.getContent().get(1)).getData());
        assertEquals("collapsed", ((Image) subSection.getContent().get(2)).getTitle());

        // test subsection 4
        subSection = (Section) section.getContent().get(3);
        assertNotNull(subSection);
        assertEquals("Sub Title 4", subSection.getTitle());
        assertNull(subSection.getSubTitle());
        assertEquals(1, subSection.getContent().size());

        final Table table = (Table) subSection.getContent().get(0);
        assertEquals(Arrays.asList("Heading 1", "Heading 2", "Heading 3"), table.getHeadings());
        assertEquals("Table 1.1", table.getTableName());
        assertEquals("A table", table.getCaption());

        final List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("Heading 1", 123);
        map.put("Heading 2", "Value");
        map.put("Heading 3", null);
        content.add(map);

        map = new HashMap<>();
        map.put("Heading 1", 321);
        map.put("Heading 2", null);
        map.put("Heading 3", 432.0);
        content.add(map);

        map = new HashMap<>();
        map.put("Heading 2", "Final row");
        content.add(map);

        assertEquals(content, table.getRows());
        assertFalse(table.getRenderHeadings());
        assertTrue(table.getHeadingsOnLeft());
    }
}
