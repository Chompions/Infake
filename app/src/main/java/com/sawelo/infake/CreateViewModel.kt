package com.sawelo.infake

import android.content.Context
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.infake.databinding.DialogMenuScheduleBinding
import com.sawelo.infake.databinding.DialogRelativeScheduleBinding
import com.sawelo.infake.function.SharedPrefFunction
import com.sawelo.infake.function.UpdateTextFunction

class CreateViewModel : ViewModel() {
    private val _mainScheduleText = MutableLiveData<String>()
    val mainScheduleText: LiveData<String> = _mainScheduleText

    private val _relativeTimeText = MutableLiveData<String>()
    val relativeTimeText: LiveData<String> = _relativeTimeText

    private val _relativeHourNum = MutableLiveData<Int>()
    val relativeHourNum: LiveData<Int> = _relativeHourNum
    private val _relativeMinuteNum = MutableLiveData<Int>()
    val relativeMinuteNum: LiveData<Int> = _relativeMinuteNum
    private val _relativeSecondNum = MutableLiveData<Int>()
    val relativeSecondNum: LiveData<Int> = _relativeSecondNum

    private val _specificHourNum = MutableLiveData<Int>()
    val specificHourNum: LiveData<Int> = _specificHourNum
    private val _specificMinuteNum = MutableLiveData<Int>()
    val specificMinuteNum: LiveData<Int> = _specificMinuteNum

    private val _isTimerType = MutableLiveData<Boolean>()

    private val _menuArray = MutableLiveData<List<ScheduleData>>()

    init {
        _mainScheduleText.value = "Schedule Call"
        _relativeTimeText.value = "Set timer"

        _relativeHourNum.value = 0
        _relativeMinuteNum.value = 0
        _relativeSecondNum.value = 0

        _specificHourNum.value = 0
        _specificMinuteNum.value = 0

        _isTimerType.value = true

        _menuArray.value = mutableListOf(
            ScheduleData(true, minute = 0),
            ScheduleData(true, minute = 2),
            ScheduleData(true, minute = 5),
            ScheduleData(true, minute = 10)
        )
    }

    // Set text for each button in ScheduleMenuFragment
    fun menuButton(
        context: Context,
        fragment: Fragment,
        binding: DialogMenuScheduleBinding,
    ) {
        _menuArray.value?.forEachIndexed { i, menuData ->
            val button = when (i) {
                0 -> binding.button1
                1 -> binding.button2
                2 -> binding.button3
                else -> binding.button4
            }
            button.setOnClickListener {
                mainSetTime(context, menuData)
                dismissMenuDialog(fragment)
            }
            button.text = updateMainValues(context, menuData, updateSharedPref = false)
        }
    }

    // Function for dismissing ScheduleMenuFragment
    fun dismissMenuDialog(fragment: Fragment) {
        val prevFragment = fragment.parentFragmentManager
            .findFragmentByTag("ScheduleMenuFragment")
        if (prevFragment != null) {
            val prevDialog = prevFragment as DialogFragment
            prevDialog.dismiss()
        }
    }

    // Update sharedPref time settings & run mainUpdateText function
    fun mainSetTime(context: Context, scheduleData: ScheduleData) {
        val hour: Int = scheduleData.hour ?: 0
        val minute: Int = scheduleData.minute ?: 0
        val second: Int = scheduleData.second ?: 0

        if (scheduleData.timerType == true) {
            val sharedPref = SharedPrefFunction(context)
            with(sharedPref.editor) {
                putInt(SharedPrefFunction.RELATIVE_HOUR, hour)
                putInt(SharedPrefFunction.RELATIVE_MINUTE, minute)
                putInt(SharedPrefFunction.RELATIVE_SECOND, second)
                putBoolean(SharedPrefFunction.TIMER_TYPE, true)
                apply()
            }
            updateMainValues(context, scheduleData)
        } else {
            val sharedPref = SharedPrefFunction(context)
            with(sharedPref.editor) {
                putInt(SharedPrefFunction.SPECIFIC_HOUR, hour)
                putInt(SharedPrefFunction.SPECIFIC_MINUTE, minute)
                putBoolean(SharedPrefFunction.TIMER_TYPE, false)
                apply()
            }
            updateMainValues(context, scheduleData)
        }
    }

    // Update relativeTime values & set text for ScheduleRelativeFragment UI view
    fun updateRelativeTime(
        picker: NumberPicker?,
        binding: DialogRelativeScheduleBinding,
        newVal: Int
    ) {
        if (picker != null) {
            when (picker.id) {
                binding.relativeTimeHour.id -> _relativeHourNum.value = newVal
                binding.relativeTimeMinute.id -> _relativeMinuteNum.value = newVal
                binding.relativeTimeSecond.id -> _relativeSecondNum.value = newVal
            }
        }
        val hourPad: String = _relativeHourNum.value.toString().padStart(2, '0')
        val minutePad: String = _relativeMinuteNum.value.toString().padStart(2, '0')
        val secondPad: String = _relativeSecondNum.value.toString().padStart(2, '0')
        if (_relativeHourNum.value == 0 &&
            _relativeMinuteNum.value == 0 &&
            _relativeSecondNum.value == 0
        ) {
            _relativeTimeText.value = "Set timer"
        } else {
            _relativeTimeText.value = "$hourPad:$minutePad:$secondPad"
        }
    }

    // Update specificTime values
    fun updateSpecificTime(hourOfDay: Int, minute: Int) {
        _specificHourNum.value = hourOfDay
        _specificMinuteNum.value = minute
    }

    private fun updateMainValues(
        context: Context,
        scheduleData: ScheduleData,
        updateSharedPref: Boolean = true): String {

        val (mainScheduleText) =
            UpdateTextFunction(context).updateMainText(scheduleData, updateSharedPref)
        _mainScheduleText.value = mainScheduleText
        return mainScheduleText
    }
}