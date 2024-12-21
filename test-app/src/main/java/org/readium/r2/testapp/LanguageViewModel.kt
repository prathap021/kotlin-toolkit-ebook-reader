package org.readium.r2.testapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LanguageViewModel : ViewModel() {
    private val _language = MutableLiveData<String>()
    val language: LiveData<String> get() = _language

    fun setLanguage(languageCode: String) {
        _language.value = languageCode
    }
}