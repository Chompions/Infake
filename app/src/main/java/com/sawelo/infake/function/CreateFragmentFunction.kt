package com.sawelo.infake.function

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.sawelo.infake.dataClass.ScheduleData
import com.sawelo.infake.viewModel.CreateViewModel

class CreateFragmentFunction(context: Context, viewModel: CreateViewModel) {
    private val model = viewModel
    private val sharedPref = SharedPrefFunction(context)

    // Update sharedPref time settings & run mainUpdateText function
    fun mainSetTime(scheduleData: ScheduleData) {
        when (scheduleData.timerType) {
            (true) -> {
                with(sharedPref.editor) {
                    putInt(SharedPrefFunction.RELATIVE_HOUR, scheduleData.relativeHour)
                    putInt(SharedPrefFunction.RELATIVE_MINUTE, scheduleData.relativeMinute)
                    putInt(SharedPrefFunction.RELATIVE_SECOND, scheduleData.relativeSecond)
                    putBoolean(SharedPrefFunction.TIMER_TYPE, true)
                    apply()
                }
                model.updateMainScheduleText(scheduleData)
            }
            (false) -> {
                with(sharedPref.editor) {
                    putInt(SharedPrefFunction.SPECIFIC_HOUR, scheduleData.specificHour)
                    putInt(SharedPrefFunction.SPECIFIC_MINUTE, scheduleData.specificMinute)
                    putBoolean(SharedPrefFunction.TIMER_TYPE, false)
                    apply()
                }
                model.updateMainScheduleText(scheduleData)
            }
        }
    }

    // Function for dismissing dialogs
    fun dismissMenuDialog(fragment: Fragment) {
        val prevFragment = fragment.parentFragmentManager
            .findFragmentByTag("ScheduleMenuFragment")
        if (prevFragment != null) {
            val prevDialog = prevFragment as DialogFragment
            prevDialog.dismiss()
        }
    }
}