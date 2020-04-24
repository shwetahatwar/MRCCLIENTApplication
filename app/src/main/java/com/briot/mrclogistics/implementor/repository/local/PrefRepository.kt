package com.briot.mrclogistics.implementor.repository.local

import android.content.Context
import android.graphics.Color

class PrefRepository {
    companion object {
        val singleInstance = PrefRepository();
    }

    private val prefs = HashMap<String, String>()

    fun setKeyValue(key: String, value: String) {
        prefs[key] = value;
    }

    fun getValueOrDefault(key: String, defaultValue: String) : String {
        return prefs[key] ?: defaultValue;
    }

    fun serializePrefs(context: Context) {
        val sharedPref = context.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        val editor = sharedPref.edit();

        prefs.keys.asIterable().forEach {
            editor.putString(it, prefs[it] ?: "")
        }

        editor.commit();
    }

    fun deserializePrefs(context: Context) {
        val sharedPref = context.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return
        sharedPref.all.keys.forEach {
            prefs[it] = sharedPref.getString(it, "").orEmpty()
        }
    }
}

class PrefConstants {


    public val USER_TOKEN = "USERTOKEN"
    public val USER_ID = "USER_ID"
    public val id = "id"
    public val username = "username"
    public val deviceId = "deviceId"
    public val password = "password"
    public val status = "status"

    public val MAX_STALE = 60 * 60 * 3 * 1 // 3 hours
    public val MAX_AGE = 60 * 60 * 2 // 2 hours

    val lightGreenColor = Color.parseColor("#FF9CF780")
    val lightOrangeColor = Color.parseColor("#73FF8800")
    val lightGrayColor = Color.parseColor("#FFF3F3F3")
    val messageBackgroundColor = Color.parseColor("#FFD50000")
}