package com.example.airlines_kubsu.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.airlines_kubsu.data.Airline
import com.example.airlines_kubsu.repository.AppRepository

class AirlineViewModel : ViewModel() {
    var airlines: MutableLiveData<List<Airline>> = MutableLiveData()

    init {
        AppRepository.get().airlines.observeForever {
            airlines.postValue(it)
        }
    }
}