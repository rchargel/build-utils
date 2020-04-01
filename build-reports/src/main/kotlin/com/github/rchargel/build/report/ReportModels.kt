package com.github.rchargel.build.report

import java.io.Serializable

data class Section(
        val title: String,
        val subTitle: String? = null
) : Serializable