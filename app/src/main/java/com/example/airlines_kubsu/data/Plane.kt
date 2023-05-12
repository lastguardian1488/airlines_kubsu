package com.example.airlines_kubsu.data

import java.util.UUID

data class Plane(
    val id : UUID = UUID.randomUUID(),
    var name : String = "",
    var amRows : Int = 0,
    var amCols : Int = 0) {
    var seats : List<Seat> = emptyList()
}

