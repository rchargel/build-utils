package com.github.rchargel.build.common

class StringUtils {
    companion object {
        private val METRIC_ABBR = arrayOf("", "K", "M", "G", "T")

        fun normalizeMetricString(value: Long?, units: String): String? {
            if (value == null)
                return null
            var dub = value.toDouble()
            var abbrIndex = 0
            while (dub > 1000 && abbrIndex < METRIC_ABBR.size - 1) {
                abbrIndex++
                dub /= 1000.0
            }
            return "%.1f %s%s".format(dub, METRIC_ABBR[abbrIndex], units)
        }

        @JvmStatic
        fun normalizeMemoryString(bytes: Long?): String? {
            if (bytes == null)
                return null

            var bdub = bytes.toDouble()
            var abbrIndex = 0
            while (bdub > 2048.0 && abbrIndex < METRIC_ABBR.size - 1) {
                abbrIndex++
                bdub /= 1024.0
            }
            return "%.1f %sB".format(bdub, METRIC_ABBR[abbrIndex])
        }
    }
}