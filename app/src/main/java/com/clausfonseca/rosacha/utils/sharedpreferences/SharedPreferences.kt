package com.clausfonseca.rosacha.utils.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE

private const val SHARED_PREFERENCES_KEY = "rosa.cha"
private const val VISIBILITY_HOME_FRAGMENT = "visibility.home.fragment"

private fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)
}

fun isHomeFragmentVisible(context: Context): Boolean {
    return getSharedPreferences(context).getBoolean(VISIBILITY_HOME_FRAGMENT, true)
}

fun setHomeFragmentVisible(context: Context, homeFragmentVisibility: Boolean) {
    getSharedPreferences(context).edit().apply {
        putBoolean(VISIBILITY_HOME_FRAGMENT, homeFragmentVisibility)
        apply()
    }
}


