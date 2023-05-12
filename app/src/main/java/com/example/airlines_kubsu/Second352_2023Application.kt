package com.example.airlines_kubsu

import android.app.Application
import android.content.Context
import com.example.airlines_kubsu.repository.AppRepository

class Second352_2023Application: Application() {
    override  fun onCreate(){
        super.onCreate()
        AppRepository.newInstance()
    }
    init{
        instance = this
    }
    companion object{
        private var instance: Second352_2023Application? = null
        fun applicationContext(): Context{
            return instance!!.applicationContext
        }
    }
}