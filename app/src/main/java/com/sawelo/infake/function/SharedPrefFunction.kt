package com.sawelo.infake.function

import android.content.Context
import android.content.SharedPreferences
import com.sawelo.infake.ContactData

class SharedPrefFunction(context: Context) {
    companion object {
        const val ACTIVE_NAME = "ACTIVE_NAME"
        const val ACTIVE_NUMBER = "ACTIVE_NUMBER"
        const val ACTIVE_ROUTE = "ACTIVE_ROUTE"
        const val ACTIVE_HOUR = "ACTIVE_HOUR"
        const val ACTIVE_MINUTE = "ACTIVE_MINUTE"

    }
    private val contactData: ContactData = ContactData()
    private val sharedPref: SharedPreferences = context.getSharedPreferences(
            "PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)

    val editor: SharedPreferences.Editor = sharedPref.edit()

    // Retrieve data from sharedPref
    val activeName= sharedPref.getString(ACTIVE_NAME, contactData.name) ?: contactData.name
    val activeNumber = sharedPref.getString(ACTIVE_NUMBER, contactData.number) ?: contactData.number
    val activeRoute = sharedPref.getString(ACTIVE_ROUTE, contactData.route) ?: contactData.route

    val activeHour = sharedPref.getInt(ACTIVE_HOUR, 0)
    val activeMinute = sharedPref.getInt(ACTIVE_MINUTE, 0)

    // Check if string is blank as in -> ""
    private fun checkBlank(input: String, default: String): String {
        return if (input.isBlank()) default else input
    }

    // Public function to put new contactData in sharedPref
    fun putStringData(newData: ContactData) {
        with (editor) {
            putString(ACTIVE_NAME, checkBlank(newData.name, contactData.name))
            putString(ACTIVE_NUMBER, checkBlank(newData.number, contactData.number))
            putString(ACTIVE_ROUTE, checkBlank(newData.route, contactData.route))
            apply()
        }
    }
}