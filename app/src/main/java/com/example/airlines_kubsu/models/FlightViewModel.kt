package com.example.airlines_kubsu.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.airlines_kubsu.data.Airline
import com.example.airlines_kubsu.data.Flight
import com.example.airlines_kubsu.repository.AppRepository
import java.util.*

class FlightViewModel : ViewModel() {
    fun newFlight(cityId: UUID, flight: Flight) {
        AppRepository.get().newFlight(cityId, flight)
    }
}