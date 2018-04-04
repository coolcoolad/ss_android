package com.github.shadowsocks.utils

import android.content.Context

/**
 * Created by yangjie on 2017/4/24.
 */

object SharedPrefUtils {
    private val sharedPreferenceName = "app"
    val token = "token"
    val userId = "userId"
    val username = "username"
    val sessionId = "sessionId"

    fun getValFromSP(context: Context, key: String): String {
        val sp = context.getSharedPreferences(sharedPreferenceName, 0)
        return sp.getString(key, "")
    }

    fun putValToSP(context: Context, key: String, `val`: String) {
        val sp = context.getSharedPreferences(sharedPreferenceName, 0)
        val editor = sp.edit()
        editor.putString(key, `val`)
        editor.apply()
    }
}