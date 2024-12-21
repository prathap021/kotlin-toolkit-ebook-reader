package org.readium.r2.testapp.about

import android.content.Context
import android.content.SharedPreferences


class LanguagePreference(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)

    fun setLanguage(language: String) {
        sharedPref.edit().putString("language", language).apply()
    }

    fun getLanguage(): String {
        return sharedPref.getString("language", "en") ?: "en"
    }
}
//
//
//
//object LanguageManager {
//    private const val LANGUAGE_PREF_KEY = "selected_language"
//
//    fun setLocale(context: Context, languageCode: String) {
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//
//        val config = context.resources.configuration
//        config.setLocale(locale)
//
//        context.resources.updateConfiguration(config, context.resources.displayMetrics)
//
//        // Save language to SharedPreferences
//        val sharedPref = context.getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)
//        sharedPref.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply()
//    }
//
//    fun getCurrentLanguage(context: Context): String {
//        val sharedPref = context.getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)
//        return sharedPref.getString(LANGUAGE_PREF_KEY, Locale.getDefault().language) ?: "en"
//    }
//}
