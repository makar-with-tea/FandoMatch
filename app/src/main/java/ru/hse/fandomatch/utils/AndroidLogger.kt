package ru.hse.fandomatch.utils

import android.util.Log
import ru.hse.fandomatch.BuildConfig
import ru.hse.fandomatch.domain.logging.Logger

class AndroidLogger() : Logger {

    override fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.w(tag, message, throwable)
        else Log.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.e(tag, message, throwable)
        else Log.e(tag, message)
    }
}
