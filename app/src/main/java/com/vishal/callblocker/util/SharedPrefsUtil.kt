package com.osquare.support.utils.sharedPreference

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("StaticFieldLeak")
object SharedPrefsUtil {

    private var sPrefs: SharedPreferences? = null
    private var context: Context? = null
        private set

    fun setNumber(number: String?,context: Context?) {
        SharedPrefsHelper.getInstance(context)?.save(AppPrefStrings.NUMBER, number)
    }

    fun getNumber(context: Context?): String? {
        return SharedPrefsHelper.getInstance(context)?.get(AppPrefStrings.NUMBER, "")
    }

    fun init(newContext: Context) {
        context = newContext
    }


    interface AppPrefStrings {
        companion object {
            val NUMBER = "number"
        }
    }
}