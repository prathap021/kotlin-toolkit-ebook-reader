package org.readium.r2.testapp.utils
import android.content.Context
import android.content.SharedPreferences

class SpHelper (context: Context){
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun putBool(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
    fun getBool(key: String, defaultValue: Boolean): Boolean {
       return sharedPreferences.getBoolean(key,false)
    }
}