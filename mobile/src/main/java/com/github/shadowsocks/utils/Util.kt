package com.github.shadowsocks.utils

import java.util.*

/**
 * Created by yajie on 4/2/2018.
 */
object Util {

    fun isOverdue(dateTime: Date): Boolean
    {
        val end = dateTime.time
        val now = Date().time
        return now >= end
    }

}