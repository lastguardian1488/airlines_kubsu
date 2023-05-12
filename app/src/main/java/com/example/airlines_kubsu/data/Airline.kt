package com.example.airlines_kubsu.data

import java.util.*

data class Airline(
    val id : UUID = UUID.randomUUID(),
    var name : String="",
    var year : String="") {
    var cities: List<City> = emptyList()
}
