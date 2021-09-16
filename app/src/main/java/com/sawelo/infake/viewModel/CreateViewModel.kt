package com.sawelo.infake.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.infake.`object`.StaticObject
import com.sawelo.infake.`object`.UpdateTextObject
import com.sawelo.infake.dataClass.ScheduleData

class CreateViewModel : ViewModel() {
    private val _mainScheduleText = MutableLiveData<String>()
    val mainScheduleText: LiveData<String> = _mainScheduleText

    private val _relativeTimeText = MutableLiveData<String>()
    val relativeTimeText: LiveData<String> = _relativeTimeText

    private val _relativeHourNum: MutableLiveData<Int> = MutableLiveData()
    val relativeHourNum: LiveData<Int> = _relativeHourNum
    private val _relativeMinuteNum: MutableLiveData<Int> = MutableLiveData()
    val relativeMinuteNum: LiveData<Int> = _relativeMinuteNum
    private val _relativeSecondNum: MutableLiveData<Int> = MutableLiveData()
    val relativeSecondNum: LiveData<Int> = _relativeSecondNum

    private val _specificHourNum: MutableLiveData<Int> = MutableLiveData()
    val specificHourNum: LiveData<Int> = _specificHourNum
    private val _specificMinuteNum: MutableLiveData<Int> = MutableLiveData()
    val specificMinuteNum: LiveData<Int> = _specificMinuteNum

    private val _isTimerType = MutableLiveData<Boolean>()

    init {
        _mainScheduleText.value = "Schedule Call"
        _relativeTimeText.value = "Set timer"

        _relativeHourNum.value = 0
        _relativeMinuteNum.value = 0
        _relativeSecondNum.value = 0

        _specificHourNum.value = 0
        _specificMinuteNum.value = 0

        _isTimerType.value = true
    }

    fun updateMainScheduleText(scheduleData: ScheduleData){
        val (mainScheduleText) = UpdateTextObject.updateMainText(scheduleData)
        _mainScheduleText.value = mainScheduleText
    }

    // Update relativeTime values & set text for ScheduleRelativeFragment UI view
    fun updateRelativeTime(
        time: Pair<String, Int>
    ) {
        when (time.first) {
            (StaticObject.TimeEnum.HOUR.name) -> _relativeHourNum.value = time.second
            (StaticObject.TimeEnum.MINUTE.name) -> _relativeMinuteNum.value = time.second
            (StaticObject.TimeEnum.SECOND.name) -> _relativeSecondNum.value = time.second
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
}