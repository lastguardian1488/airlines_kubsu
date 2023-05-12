package com.example.airlines_kubsu.data

import java.util.*

data class City(
    val id : UUID = UUID.randomUUID(),
    var name : String="") {
    var flights: List<Flight> = emptyList()
    var flightPlanes: List<FlightPlane> = emptyList()
}