package com.handylab.ruto.util

import javax.inject.Inject

interface AppLogger {
    fun d(tag: String, msg: String): Int
    fun w(tag: String, msg: String, t: Throwable?): Int
    fun e(tag: String, msg: String, t: Throwable? = null): Int
}

class LogcatLogger @Inject constructor() : AppLogger {
    override fun d(tag: String, msg: String) =
        android.util.Log.d(tag, msg)

    override fun w(tag: String, msg: String, t: Throwable?) =
        android.util.Log.w(tag, msg)


    override fun e(tag: String, msg: String, t: Throwable?) =
        android.util.Log.e(tag, msg, t)
}