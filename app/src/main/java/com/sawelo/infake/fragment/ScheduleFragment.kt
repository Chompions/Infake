package com.sawelo.infake.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.sawelo.infake.CreateViewModel
import com.sawelo.infake.function.SharedPrefFunction
import java.util.*

class ScheduleFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private val model: CreateViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        // Put time into sharedPref
        val sharedPref = SharedPrefFunction(requireContext())
        with(sharedPref.editor) {
            putInt(SharedPrefFunction.ACTIVE_HOUR, hourOfDay)
            putInt(SharedPrefFunction.ACTIVE_MINUTE, minute)
            apply()
        }

        // Update viewModel LiveData
        model.updateScheduleText(hourOfDay, minute)
    }
}