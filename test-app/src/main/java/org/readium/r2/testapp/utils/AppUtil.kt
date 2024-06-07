package org.readium.r2.testapp.utils

import java.util.Locale

object AppUtil {

    fun getAppLanguage(): String {
        return Locale.getDefault().language
    }
}