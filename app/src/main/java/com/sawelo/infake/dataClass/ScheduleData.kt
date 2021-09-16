package com.sawelo.infake.dataClass

data class ScheduleData(
    val timerType: Boolean? = true,

    val relativeHour: Int = 0,
    val relativeMinute: Int = 0,
    val relativeSecond: Int = 0,

    val specificHour: Int = 0,
    val specificMinute: Int = 0,
)

