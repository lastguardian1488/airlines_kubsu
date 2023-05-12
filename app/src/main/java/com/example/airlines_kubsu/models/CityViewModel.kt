package com.example.airlines_kubsu.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.airlines_kubsu.data.Airline
import com.example.airlines_kubsu.repository.AppRepository
import java.util.UUID

class CityViewModel : ViewModel() {
    val airlines: MutableLiveData<Airline?> = MutableLiveData()

    private var airlineId: UUID? = null

    init {
        AppRepository.get().airlines.observeForever {
            airlines.postValue(it.find { airline -> airline.id == airlineId})
        }
    }

    fun setAirlineId (airlineId : UUID) {
        this.airlineId = airlineId
        airlines.postValue(AppRepository.get().airlines.value?.find { airline -> airline.id == airlineId })
    }
}