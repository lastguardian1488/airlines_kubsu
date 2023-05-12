package com.example.airlines_kubsu.models

import androidx.lifecycle.ViewModel
import com.example.airlines_kubsu.data.Flight
import com.example.airlines_kubsu.repository.AppRepository
import java.util.*

class CityFlightListViewModel: ViewModel() {
    fun deleteFlight(cityID: UUID, flight: Flight) =
        AppRepository.get().deleteFlight(cityID, flight)
}