package com.example.airlines_kubsu.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.airlines_kubsu.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

const val SHARED_PREFERENCES_AIRLINES = "AirlinesPrefs"
const val SHARED_PREFERENCES_NAME = "AppAirlinesKubsuPrefs"

class AppRepository private constructor() {
    var airlines : MutableLiveData<List<Airline>> = MutableLiveData()

    companion object {
        private var INSTANCE: AppRepository? = null

        fun newInstance() {
            if (INSTANCE == null) {
                INSTANCE = AppRepository()
            }
        }

        fun get(): AppRepository {
            return INSTANCE ?: throw IllegalAccessException("Репозиторий не инициализирован")
        }
    }

    private val alphabet = ('A'..'Z').joinToString("")

    private fun getRowLetter(row: Int): String {
        var result = ""
        var num = row
        while (num > 0) {
            val remainder = (num - 1) % 26
            result = alphabet[remainder] + result
            num = (num - 1) / 26
        }
        return result
    }

    fun addSeats(plane: Plane) {
        for (row in 1..plane.amRows) {
            for (col in 1..plane.amCols) {
                val rowToLetter = getRowLetter(row)
                val seat = Seat(name = "$rowToLetter$col", position = plane.seats.size+1)
                plane.seats += seat
            }
        }
    }

    //TODO доделать сохранение
    fun saveData(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonAirlines = Gson().toJson(airlines.value)
        sharedPreferences.edit().putString(SHARED_PREFERENCES_AIRLINES, jsonAirlines).apply()
    }

    fun loadData(context: Context) {
        val sharedPreferences =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonAirlines = sharedPreferences.getString(SHARED_PREFERENCES_AIRLINES, null)
        if (jsonAirlines != null) {
            val listType = object : TypeToken<List<Airline>>() {}.type
            val faculties = Gson().fromJson<List<Airline>>(jsonAirlines, listType)
            airlines.value = faculties
        } else {
            airlines.value = arrayListOf()
        }
    }

    fun newAirline(name: String, year: String) {
        val airline = Airline(name = name, year = year)
        val list: ArrayList<Airline> =
            if (airlines.value != null) {
                airlines.value as ArrayList<Airline>
            } else
                ArrayList()
        list.add(airline)
        airlines.postValue(list)
    }

    fun editAirline(airlineId: UUID, name: String, year: String) {
        val list = airlines.value?.toMutableList() ?: return
        val airline = list.find { it.id == airlineId } ?: return
        airline.name = name
        airline.year = year
        airlines.postValue(list)
    }

    fun deleteAirline(airline: Airline) {
        val list = airlines.value?.toMutableList() ?: return
        list.remove(airline)
        airlines.postValue(list)
    }

    fun newCity(airlineId: UUID, name: String) {
        val a = airlines.value ?: return
        val airline = a.find { it.id == airlineId} ?: return
        val city = City(name = name)
        val list: ArrayList<City> = if (airline.cities.isEmpty())
            ArrayList()
        else
            airline.cities as ArrayList<City>
        list.add(city)
        airline.cities = list
        airlines.postValue(a)
    }

    fun editCity(airlineId: UUID, cityId: UUID, name: String) {
        val list = airlines.value?.toMutableList() ?: return
        val airline = list.find { it.id == airlineId } ?: return
        var newCity = airline.cities.find { it.id == cityId } ?: return
        newCity.name = name
        newCity.flights.forEach{ it.departureCity = name}
        newCity.flightPlanes.forEach { it.deptCity = name }
        airlines.postValue(list)
    }

    fun deleteCity(airlineId: UUID, city: City?) {
        val a = airlines.value ?: return
        val airline = a.find { it.id == airlineId }  ?: return
        if (airline!!.cities.isEmpty()) return
        val list = airline.cities as ArrayList<City>
        list.remove(city)
        airline.cities = list
        airlines.postValue(a)
    }

    fun newFlight(cityId: UUID, flight: Flight) {
        val a = airlines.value ?: return
        val airline = a.find { it.cities.find { it.id == cityId } != null } ?: return
        val city = airline.cities.find { it.id == cityId }
        val list: ArrayList<Flight> = if (city!!.flights.isEmpty())
            ArrayList()
        else
            city.flights as ArrayList<Flight>
        list.add(flight)
        city.flights = list
        airlines.postValue(a)
    }

    fun deleteFlight(cityId: UUID, flight: Flight) {
        val a = airlines.value ?: return
        val airline = a.find { it.cities.find { it.id == cityId } != null } ?: return
        val city = airline.cities.find { it.id == cityId }
        if (city!!.flights.isEmpty()) return
        val list = city.flights as ArrayList<Flight>
        list.remove(flight)
        city.flights = list
        airlines.postValue(a)
    }

    fun editFlight() {
        //TODO: implement
    }
}