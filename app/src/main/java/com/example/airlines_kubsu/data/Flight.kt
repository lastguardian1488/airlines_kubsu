package com.example.airlines_kubsu.data

import java.util.*

data class Flight(
    val id : UUID = UUID.randomUUID(),
    var flightWeekDay : Int = -1,
    var departureCity : String = "",
    var arrivalCity : String = "",
    var flightCost : Int = 0,
    var baggageCost : Int = 0,
    var plane: Int = 0
)