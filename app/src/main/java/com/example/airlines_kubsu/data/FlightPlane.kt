package com.example.airlines_kubsu.data

import java.time.LocalTime
import java.util.Date
import java.util.UUID

data class FlightPlane(
    val id : UUID = UUID.randomUUID(),
    val deptTime : LocalTime,
    val date: Date = Date(0L),
    var deptCity : String,
    var arrCity : String,
    var plane: Plane,
)
