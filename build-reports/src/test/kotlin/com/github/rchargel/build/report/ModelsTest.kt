package com.github.rchargel.build.report

import com.github.rchargel.build.test.BuildTester
import org.junit.Test

class ModelsTest {

    @Test
    fun sectionTest() = BuildTester(Section::class.java, setOf("anchor")).evaluate()

    @Test
    fun divisionTest() = BuildTester(Division::class.java).evaluate()

    @Test
    fun textTest() = BuildTester(Text::class.java).evaluate()

    @Test
    fun tableTest() {
        BuildTester(Table::class.java).evaluate()
    }

    @Test
    fun imagesTest() {
        BuildTester(Image::class.java, setOf("dataURL")).evaluate()

        assert(Image.EXPANDED_ICON == Image.EXPANDED_ICON)
        assert(Image.EXPANDED_ICON.hashCode() == Image.EXPANDED_ICON.hashCode())
        assert(Image.EXPANDED_ICON.dataURL == Image.EXPANDED_ICON.dataURL)
        assert(Image.EXPANDED_ICON != Image.SUCCESS_ICON)
        assert(Image.EXPANDED_ICON.hashCode() != Image.SUCCESS_ICON.hashCode())
        assert(Image.EXPANDED_ICON.dataURL != Image.SUCCESS_ICON.dataURL)
        assert(Image.EXPANDED_ICON != null)

        val i1 = Image.builder().title("A")
                .data(byteArrayOf(1, 2, 3))
                .contentType("image/jpg")
                .thumbnail(true).build()
        val i2 = Image.builder().title("A")
                .data(byteArrayOf(1, 2, 3))
                .contentType("image/jpg")
                .thumbnail(false).build()
        val i3 = Image.builder().title("A")
                .data(byteArrayOf(1, 2, 4))
                .contentType("image/jpg")
                .thumbnail(true)
                .build()
        val i4 = Image.builder().title("A")
                .data(byteArrayOf(1, 2, 3))
                .contentType("image/jpg")
                .thumbnail(true).build()

        assert(i1 != i2)
        assert(i2 != i3)
        assert(i1 != i3)
        assert(i1 === i1)
        assert(i1 == i4)
        assert(i1 !== i4)
    }
}