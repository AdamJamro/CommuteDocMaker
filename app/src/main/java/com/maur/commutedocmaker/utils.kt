@file:Suppress("UNCHECKED_CAST")

package com.maur.commutedocmaker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.trimmedLength
import java.io.Serializable

@Keep
fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T?
{
    @Suppress("DEPRECATION")
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
               activity.intent.getSerializableExtra(name, clazz)
           else
               activity.intent.getSerializableExtra(name) as? T?
}

@Keep
fun <T : Serializable?> getSerializable(intent: Intent?, name: String, clazz: Class<T>): T?
{
    @Suppress("DEPRECATION")
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
               intent?.getSerializableExtra(name, clazz)
           else
               intent?.getSerializableExtra(name) as? T?
}

const val FILENAME_MAX_LENGTH = 45
fun isFileNameValid(name: String): Boolean {
    if (name.trimmedLength() == 0) return false
    if (name.length > FILENAME_MAX_LENGTH) return false
    return true
}

@Composable
/* inline */ fun stringResource(@StringRes id: Int): String {
    return LocalContext.current.getString(id)
}

/* inline */ fun stringRes(context: Context, @StringRes id: Int, vararg formatArgs: Any): String {
    return context.getString(id, *formatArgs)
}