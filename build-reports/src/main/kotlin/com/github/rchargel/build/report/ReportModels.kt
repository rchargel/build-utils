package com.github.rchargel.build.report

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * An interface to mark report content.
 */
interface ReportContent : Serializable

/**
 * A section of a report. The top level object for any report is a section
 *
 * @property title The report title
 * @property subTitle An optional sub-title
 * @property content A list of sub-sections, tables, text, and images
 * @property anchor Generated from the title, used internally by the report table of contents
 *
 * @constructor Creates a new Section
 */
data class Section(
        val title: String,
        val subTitle: String? = null,
        val content: List<ReportContent>
) : ReportContent {
    val anchor: String
        get() = this.title.replace(Regex("""[^a-zA-Z]+"""), "_")

    companion object {
        /**
         * Builder for the [Section] object
         */
        class Builder internal constructor(
                private var title: String,
                private var subTitle: String? = null,
                private var content: ArrayList<ReportContent> = ArrayList()
        ) {
            /**
             * Adds a [subTitle] to this section
             * @return an instance of this builder
             */
            fun subTitle(subTitle: String) = apply { this.subTitle = subTitle }

            /**
             * Appends [content] to this section
             * @return an instance of this builder
             */
            fun appendContent(content: ReportContent) = apply { this.content.add(content) }

            /**
             * Builds a [Section] instance
             * @return a new [Section] instance
             */
            fun build() = Section(title, subTitle, content.toList())
        }

        /**
         * Uses a [title] to create a new [Builder] instance
         * @return a new instance of [Builder]
         */
        @JvmStatic
        fun builder(title: String) = Builder(title)
    }
}

/**
 * An HTML paragraph, with an optional "bold" paragraph title.
 *
 * @property content The paragraph content
 * @property title An optional bold text "title" for the paragraph
 *
 * @constructor Creates a new instance of [Text]
 */
data class Text(
        val content: String,
        val title: String? = null
) : ReportContent {
    companion object {
        /**
         * Builder for the [Text] object
         */
        class Builder internal constructor(
                private var content: String,
                private var title: String? = null
        ) {
            /**
             * Adds a [title] to the text
             * @return an instance of this builder
             */
            fun title(title: String) = apply { this.title = title }

            /**
             * Builds a [Text] instance.
             * @return a new instance of [Text]
             */
            fun build() = Text(content, title)
        }

        /**
         * Uses the [content] to create a new [Builder] instance.
         * @return an instance of [Builder]
         */
        @JvmStatic
        fun builder(content: String) = Builder(content)
    }
}

/**
 * An HTML Table
 *
 * @property headings A list of table headings
 * @property rows A list of row values which map back to the headings
 * @property renderHeadings A boolean which if false, will hide the table headings
 * @property headingsOnLeft A boolean which if true, will pivot the table so that the headings are on the left
 * @property tableName An optional bold heading for the table
 * @property caption An optional caption for the table
 *
 * @constructor Creates an instance of the table
 */
data class Table(
        val headings: List<String>,
        val rows: List<Map<String, Any?>>,
        val renderHeadings: Boolean = true,
        val headingsOnLeft: Boolean = false,
        val tableName: String? = null,
        val caption: String? = null
) : ReportContent {
    companion object {
        /**
         * Builds instances of [Table]
         */
        class Builder internal constructor(
                private var headings: ArrayList<String> = ArrayList(),
                private var rows: ArrayList<HashMap<String, Any?>> = ArrayList(),
                private var renderHeadings: Boolean = true,
                private var headingsOnLeft: Boolean = false,
                private var tableName: String? = null,
                private var caption: String? = null,
                private var rowIndex: Int = 0
        ) {
            /**
             * Sets the [headings] for the table
             * @return this instance of this builder
             */
            fun headings(headings: List<String>) = apply { this.headings = ArrayList(headings) }

            /**
             * Adds a new [heading] for this table
             * @return this instance of this builder
             */
            fun addHeading(heading: String) = apply { this.headings.add(heading) }

            /**
             * Adds a new [row] for this table
             * @return this instance of this builder
             */
            fun addRow(row: Map<String, Any?>) = apply {
                this.rows.add(HashMap(row))
                this.rowIndex++
            }

            /**
             * Determines the [renderHeadings] behavior
             * @return this instance of this builder
             */
            fun renderHeadings(renderHeadings: Boolean) = apply { this.renderHeadings = renderHeadings }

            /**
             * Deterines the [headingsOnLeft] behavior
             * @return this instance of this builder
             */
            fun headingsOnLeft(headingsOnLeft: Boolean) = apply { this.headingsOnLeft = headingsOnLeft }

            /**
             * Adds a new [value] to the [heading] of the current row
             * @return this instance of this builder
             */
            fun addCellValue(heading: String, value: Any?) = apply {
                while (this.rows.size <= rowIndex)
                    this.rows.add(HashMap())
                this.rows[rowIndex][heading] = value
            }

            /**
             * Ends the current row, and starts a new one
             * @return this instance of this builder
             */
            fun endRow() = apply { this.rowIndex++ }

            /**
             * Adds a [tableName] to this table
             * @return this instance of this builder
             */
            fun tableName(tableName: String) = apply { this.tableName = tableName }

            /**
             * Adds a [caption] to this table
             * @return this instance of this builder
             */
            fun caption(caption: String) = apply { this.caption = caption }

            /**
             * Builds a [Table] instance
             * @return a new instance of [Table]
             */
            fun build() = Table(headings.toList(), rows.toList(), renderHeadings, headingsOnLeft, tableName, caption)
        }

        /**
         * Creates a [Builder] for new tables
         * @return a new instance of [Builder]
         */
        @JvmStatic
        fun builder() = Builder()
    }
}

/**
 * A HTML Image element. Uses data-urls rather than needing to include attachments.
 *
 * @property contentType The MIME type of the image
 * @property title The alt/title of the image for readability
 * @property data The binary data of the image
 * @property dataURL A generated value which is included the report, rather than an attachment
 *
 * @constructor Creates an instance of Image
 */
data class Image(
        val contentType: String = "image/png",
        val title: String? = null,
        val data: ByteArray
) : ReportContent {
    val dataURL: String
        get() = "data:$contentType;base64,${base64Encode(data)}"

    companion object {
        /**
         * Builds instances of [Image]
         */
        class Builder internal constructor(
                private var contentType: String = "image/png",
                private var title: String? = null,
                private var data: ByteArray? = null
        ) {
            /**
             * Adds a [contentType] to the image, defaults to 'image/png'
             * @return this instance of this builder
             */
            fun contentType(contentType: String) = apply { this.contentType = contentType }

            /**
             * Adds a [title] to the image
             * @return this instance of this builder
             */
            fun title(title: String) = apply { this.title = title }

            /**
             * Sets the binary [data] for this image
             * @return this instance of this builder
             */
            fun data(data: ByteArray) = apply { this.data = data }

            /**
             * Sets the binary [data] for this image using a Base64 encoded string
             * @return this instance of this builder
             */
            fun base64Data(data: String) = apply { this.data = base64Decode(data) }

            /**
             * Builds a new [Image]
             * @return an instance of [Image]
             */
            fun build() = Image(contentType, title, data ?: throw NullPointerException("No image data"))
        }

        /**
         * Creates a builder for an image
         * @return an instance of [Builder]
         */
        @JvmStatic
        fun builder() = Builder()

        /** A collapsed image */
        @JvmField
        val COLLAPSED_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("R0lGODlhBwAHAIAAAAAAAAAAACH5BAEKAAEALAAAAAAHAAcAAAIMRI5gGLrnnmOKNRsKADs=")
                .title("collapsed")
                .build()

        /** A expanded image */
        @JvmField
        val EXPANDED_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("R0lGODlhBwAHAIAAAAAAAAAAACH5BAEKAAEALAAAAAAHAAcAAAILhA+BGWoNGZy0zgIAOw==")
                .title("expanded")
                .build()

        /** An external link icon */
        @JvmField
        val EXTERNAL_ICON = Image.builder()
                .base64Data("""iVBORw0KGgoAAAANSUhEUgAAAAsAAAAJCAMAAADTuiYfAAAABGdBTUEAAK/INwWK6QAAABl0RVh0
                            U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAAMUExURXV1df///5kAAP///9HwUBsAAAAE
                            dFJOU////wBAKqn0AAAAUElEQVR42mJgBgMmJiABEEAMUDYjkAMQQAxgwMTECOQABBADA1gUBJgA
                            AgjEhjKZAQIIygarBwggCJuJAWQOQABB1DODjQMIIAYoALEBAgwAKocAvRXcdAcAAAAASUVORK5C
                            YII=""")
                .title("external")
                .build()

        /** An error icon */
        @JvmField
        val ERROR_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("""R0lGODlhDwAPAPcAAAAAAP///40LEbIOFbEOFasOFaMNFHEJDXgKD3YKD20XG7g7QJJHSmY0NunS
                            018HDMEPGLcOFp0MFJwME5sME4IKEGkIDVsHDL4PGL0PGLkPGLcPGKsOFqYNFZgME5cME5YME5MM
                            E4oLEogLEX4KEH0KEHMJD3EJDnAJDm4JDlgHC1YHDEwGCp4NFJ0NFYULEYMLEYELEXYKEGwJDr4R
                            G34LEcIUH64SHMIWIrEiLKI5P4I2O3s2Oo5kZuWztvDg4cIXJL8ZJ78aKalPV82xs97Q0cUdLcUf
                            L6scKsciNMAiM9hzffTW2fHV2PDW2frw8fvy88clOMcmOsgoPcgqQMgrQcMqP8k1SL09TcBNXPnx
                            8sQrQskuRskvR8kwScsyTcszT8gyTsc8U/TY3cs1Usw2U8w3Vd6ClMw6Wcs6Wc08XMw7W809Xs0/
                            Yc1AYs9CZfPv8EggMUsiNEghM0YgMUkiNGcxTG05WHJAYnhIbj4lOUouRq9wq7Fyrk4zT7J2tLN4
                            t7V7u0AtRUw2Uk84VjAbOEczTzEcOmJHbTMdPTQePjcgQzUfQWdPeD8yTUU3VG1XhnxkmX1nnY51
                            spqEwV5Wg/jw8Ojg4Pbw8P39/fv7+/r6+vT09P///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAJ0ALAAAAAAPAA8A
                            AAjPADsJHEiwIEFKgQD96cNnksFOkt60WWMGjJcqjQpGcsMGTZkvXahISYJoICQ1ac6MEbPlCpMl
                            QoD46VTJDJkwUAI4wdIkwJMbGzIsyuOFi5UsWgIotaQDRIcBjPBMiaIEyRClARgcqEChgKI7R4wE
                            yeEAaxEFJDwYSGQHhw0aPgJg6gEnAJESIVwcMgQBg4YFP3awaHCJhwwBHwp12hOBQIsKFi6oWPHg
                            BAw9AwlxmDACgQkUKWYkEFRwkIQQIka8iFGDtMFHcurMiUPHEcGAADs=""")
                .title("error")
                .build()

        /** An info icon */
        @JvmField
        val INFO_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("""R0lGODlhDwAPAOYAAAAAAP////39/vn6/Pj5+9rg63KDntHZ5tXc6Nfe6dbd6AknU0ddfV15olZw
                            lk9nikxjhUpggUVZeENXdElefkZaeWWBqmiErGeDq2qFrWyHrm2Ir3CKsFhsi3SNsnaPtGR5mHeQ
                            tHyUt36VuHeNrYCXuXWKqW6BnoSau4OZuoWbvHqOrIqfv3eJpIugv42hwJCkwoWXspmsx5irxoKS
                            qZKiuoaVq6Gyy6a2zqm50K280qy70Z+swLG/1LC+07XC1rjF2LzI2sHM3cPO3sLN3bK8y8nT4sbQ
                            38vU4s3W5MzV49Pb59La5sjP2ent8+Pn7fX3+vT2+fP1+EBUcEFVcXmOq32OppWpxZ6wyae0xbnG
                            2MDJ1eDm7t/l7eHn7+Xq8eTp8PDz9+/y9t3k7dzj7Nvi6+fs8ubr8ezw9evv9Oru89jf5+Lo7/H0
                            9/b4+vv8/fr7/P7+/v///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5
                            BAEAAHIALAAAAAAPAA8AAAe7gHKCg4SFgmBmZ2BsXGQFCkxHg15xcQJvcANuUW1iai6HAgKZTR1P
                            YmhOZ6ByXG8DBFA2C1tpZ4wlgmQEm21FNGtfXmVMHoIJUFJhaAYLWWMISEIYggfLamZWCzxKREA+
                            DYJG2GBcLQs1Wj05Nw6CRGBdCkknCzHsMzAPgkBLSUNBQCyoIgMGixQRBOkA+GPHChMkWKAQ8UGC
                            IBg/dODAcuWFihEhOGigMujChgwWHkCgUGHCFAaGYhIKBAA7""")
                .title("info")
                .build()

        /** A success icon */
        @JvmField
        val SUCCESS_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("""R0lGODlhDwAPAPcAAAAAAP///4SZuqa2zqe3zrzI2sfR4L/I1WN+oqGxx9/l7XeQrH+ZtKa4y5eu
                            v52zw6m9y1Nxgh0qL4KhrRolKCM4PRsnKiEzNx0sLyhDSCdBRiY+Qh4vMh0qLClGSRciIVBya5y8
                            tKLAuGqVhoSsnHSci6LFskZjT4OtjzdHO8XWyJ3Io6zWqbfbtLjfsjJCLrLhpbPiprfjq7jkrLjk
                            rbrlr7zlsY/CgLbjqbLgorLgo5zOiKzemKvcmanZl6vbl12VQp3OhaLUi6nbkZ3NhK3Vl12UPqHU
                            hOfx4Yi7ZpPHco2/bYi2atDmwvj79mKcN0p3KlWHMFeKMpXRadTkyG2qO0FnJHKzP2+sPWWeOGae
                            OGKbN1qOMlmKMVOCLkp0KVF5MYnLV4W9WXWkUWWLRmOIRZe5e5u9gXuTaJ2tkM7Xx+7z6oTKR3a3
                            QHW1P2ulOmieN2CSM0pyKH/DRV6RM1F8LFN+LURoJX28REVpJovOUbLFotbgzc/XyOPr3EltJlF4
                            K090KU1vKOrv5UxsJklnJUdjJPX38v7+/vv7+/f39////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                            AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAIsALAAAAAAPAA8A
                            AAi7ABcJHEiwoEEIIR4YLFigRQ0WBBYOFGFDRg4SEhc1cEEDxo8dCxYqMDEjhg8iTlQcMOgAhw4e
                            QZgEoBKhoIEVPYYcWbImgBkgCQhOEFKkSZIxAQbFwQJi4IAbSpAE8MMnwB46b4wIEDhiipgzhwKI
                            LcNly5MTixiE0YMHDpk+AdR8icKlCxgEKNjMcaPFi5U0aOTUsQMo0IsSba5UySIFSp47fwQRKmQo
                            xSIPGTRsqHCBAwYJHSxQ+LAoIAA7""")
                .title("success")
                .build()

        /** A warning icon */
        @JvmField
        var WARNING_ICON = Image.builder()
                .contentType("image/gif")
                .base64Data("""R0lGODlhDwAPAOYAAAAAAP///7icp7qhq2JgYXdyfZOOpJiguqGov6yyxn6OsISSsmp8oKOvx2Zz
                            ioSZuqe3zrfE18TP38vU4qO0zNzj7FtNQI9LFIM/CuBrEq5UDqNODZRHDEokBvl3FfFzFO9yFOpw
                            FNtpEtdmEtZmEtRlEs5jEc1iEcdfEb9bELxaELBUD6lRDqRPDppKDZBFDI9EDI9FDIM/C2kyCedu
                            FORtFMNdEblYELdXELVWEJVIDYlBDIA9C2gyCV0tCMtiFPl6Hfl7Hux6JqtaHb9pJtdpGfl/KMtq
                            JvmDMfmDMvqGN8xtLUQyJcxwNPqKQvqOSdh8RMx1QPqUWfqYYeaNXMx9UteIW6Z2W/qdbUk6Ms2D
                            YPuhdvukf/ung9mRcz0uKCkbFvuqiysdGCodGPutkeeghS4hHNOXgTwsJumkjTUmIToqJbKHeSoc
                            GIp2cOusnCocGSweGy0dGqiHgpp+esefnv///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5
                            BAEAAHYALAAAAAAPAA8AAAedgHaCg4SFhgkCCIaFEW9hZRSLgwNkXVtzknYNaQGdVAqLFXVenQFQ
                            VxOGB1xapU1JDoUSZ1hVpUdBRBCEBlNSUaU/HzQEgxRWT05LpSkZI0MPgm5KSEZFMD0YNyc2KhZ2
                            C0JAHh8lpSs4GiwXDGwgITUiOaUcGy46L0x0IiQmKC0zOviQEWOHDB5Z7BT4gmaNGjNj4MSRI6YN
                            GDuBAAA7""")
                .title("warning")
                .build()

        /** A new window link icon */
        @JvmField
        val NEW_WINDOW_ICON = Image.builder()
                .base64Data("""iVBORw0KGgoAAAANSUhEUgAAAAsAAAAJCAMAAADTuiYfAAAABGdBTUEAAK/INwWK6QAAABl0RVh0
                            U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAAMUExURXV1dZkAAP///////4A4ydkAAAAE
                            dFJOU////wBAKqn0AAAARklEQVR42mJgZmZmhABmgABiALGZQICRGSCAGIAAxgYIIAYGhDhAACGz
                            AQIImQ0QQGA21ByAAAKxgQBMAgQQAxSA2AABBgAmCwCxtMIFXgAAAABJRU5ErkJggg==""")
                .title("new window")
                .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (contentType != other.contentType) return false
        if (title != other.title) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentType.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        return result
    }
}

internal inline fun base64Encode(data: ByteArray) =
        Base64.getEncoder().encodeToString(data).replace(Regex("""\s+"""), "")!!

internal inline fun base64Decode(data: String) =
        Base64.getDecoder().decode(data.replace(Regex("""\s+"""), ""))!!