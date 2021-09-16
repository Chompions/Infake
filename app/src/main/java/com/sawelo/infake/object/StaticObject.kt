package com.sawelo.infake.`object`

import com.sawelo.infake.dataClass.FlutterScreenData
import com.sawelo.infake.dataClass.ScheduleData

object StaticObject {

    enum class TimeEnum() {
        HOUR,
        MINUTE,
        SECOND
    }

    /**
     * This array is used in ScheduleMenuFragment to construct list of schedule menus
     */
    val menuArray = mutableListOf(
        ScheduleData(true, relativeMinute = 0),
        ScheduleData(true, relativeMinute = 2),
        ScheduleData(true, relativeMinute = 5),
        ScheduleData(true, relativeMinute = 10)
    )

    private val firstWhatsAppRoute = FlutterScreenData(
        drawableName = "first_whatsapp",
        incomingRouteName = "/FirstWhatsAppIncomingCall",
        ongoingRouteName = "/FirstWhatsAppOngoingCall"
    )

    private val secondWhatsAppRoute = FlutterScreenData(
        drawableName = "second_whatsapp",
        incomingRouteName = "/SecondWhatsAppIncomingCall",
        ongoingRouteName = "/SecondWhatsAppOngoingCall"
    )

    val screenRouteList = listOf(
        firstWhatsAppRoute, secondWhatsAppRoute
    )

}