package com.osquare.support.utils.sharedPreference

import android.content.Context
import android.content.SharedPreferences
import com.vishal.callblocker.R
import com.vishal.callblocker.util.CoreApp
import java.util.*

class SharedPrefsHelper private constructor(context: Context?) {

    private val sharedPreferences: SharedPreferences

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    init {
        instance = this
        sharedPreferences = context?.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)!!
    }

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            editor.remove(key).apply()
        }
    }

    fun deleteAll() {
        editor.clear().apply()
    }

    fun save(key: String, value: Any?) {
        val editor = editor
        if (value is Boolean) {
            editor.putBoolean(key, (value as Boolean?)!!)
        } else if (value is Int) {
            editor.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor.putLong(key, (value as Long?)!!)
        } else if (value is String) {
            editor.putString(key, value as String?)
        } else if (value is Enum<*>) {
            editor.putString(key, value.toString())
        } else if (value != null) {
            throw RuntimeException("Attempting to save non-supported preference")
        }

        editor.apply()
    }

    operator fun <T> get(key: String): T {
        return sharedPreferences.all[key] as T
    }

    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T
        return returnValue ?: defValue
    }

    fun has(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getSet(key: String, set: Set<String>): Set<String>? {
        return sharedPreferences.getStringSet(key, set)
    }

    fun saveSet(key: String, set: TreeSet<String>) {
        editor.putStringSet(key, set).commit()
    }

    companion object {
        private val SHARED_PREFS_NAME =  CoreApp.instance?.getString(R.string.app_name)

        private var instance: SharedPrefsHelper? = null

        @Synchronized
        fun getInstance(context: Context?): SharedPrefsHelper? {
            if (instance == null) {
                instance = SharedPrefsHelper(context)
            }

            return instance
        }
    }
}
