package com.github.rchargel.build.report

import java.text.MessageFormat
import java.util.*

class Messages internal constructor(private val resourceBundle: ResourceBundle) {
    fun text(key: String) = resourceBundle.getString(key).orEmpty()
    fun text(key: String, vararg args: String) = MessageFormat.format(text(key), *args).orEmpty()

    companion object {
        @JvmStatic
        fun loadMessages(bundleName: String, locale: Locale) = Messages(ResourceBundle.getBundle(bundleName, locale))
    }
}