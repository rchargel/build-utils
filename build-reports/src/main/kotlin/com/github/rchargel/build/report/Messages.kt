package com.github.rchargel.build.report

import org.apache.commons.lang3.StringUtils
import java.text.MessageFormat
import java.util.*

class Messages internal constructor(private val resourceBundle: ResourceBundle) {
    fun text(key: String): String = try {
        resourceBundle.getString(key)
    } catch (e: MissingResourceException) {
        StringUtils.EMPTY
    }

    fun text(key: String, vararg args: String): String = MessageFormat.format(text(key), *args)

    companion object {
        @JvmStatic
        fun loadMessages(bundleName: String, locale: Locale) = Messages(ResourceBundle.getBundle(bundleName, locale))
    }
}