package com.example.airlines_kubsu

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import com.example.airlines_kubsu.data.Flight
import com.example.airlines_kubsu.repository.AppRepository
import com.example.airlines_kubsu.ui.*
import java.util.*

class MainActivity : AppCompatActivity(), AirlineFragment.Callbacks,CityFragment.Callbacks, CityFlightListFragment.Callbacks {
    private var miNewAirline: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRepository.get().loadData(this)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, AirlineFragment.newInstance(), AIRLINE_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else
                    finish()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        AppRepository.get().saveData(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        miNewAirline = menu?.findItem(R.id.miNewAirlineCity)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miNewAirlineCity -> {
                val myFragment = supportFragmentManager.findFragmentByTag(CITY_TAG)
                if (myFragment == null) {
                    showInputDialog(0);
                } else
                    showInputDialog(1)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInputDialog(index: Int = -1) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.name_input, null)
        builder.setView(dialogView)
        val nameInput = dialogView.findViewById(R.id.etAirlineCity) as EditText
        val yearInput = dialogView.findViewById(R.id.etAirlineCreateYear) as EditText
        when (index) {
            //Авиакомпания
            0 -> {
                builder.setTitle(R.string.new_airline)
                nameInput.hint = getString(R.string.hint_enter_airline_name)
                yearInput.hint = getString(R.string.hint_enter_airline_year)
                builder.setPositiveButton(R.string.ok) { _, _ ->
                    var n = nameInput.text.toString()
                    var y = yearInput.text.toString()
                    if (n.isNotBlank() and y.isNotBlank()) {
                        //TODO: переписать, используя модель для вызова
                        AppRepository.get().newAirline(n, y)
                    }
                }
            }
            //Город
            1 -> {
                builder.setTitle(R.string.new_city)
                nameInput.hint = getString(R.string.hint_enter_city)
                yearInput.visibility = View.GONE
                builder.setPositiveButton(R.string.ok) { _, _ ->
                    val n = nameInput.text.toString()
                    if (n.isNotBlank()) {
                        //TODO: переписать, используя модель для вызова
                        AppRepository.get().newCity(CityFragment.getAirlineId, n)
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.cansel, null)
        val alert = builder.create()
        alert.show()
    }

    override fun setTitle(_title: String) {
        title = _title
    }

    //переписали так что вместо двух параметров cityId, flight теперь принимает третий, используется в фрагментах city и cityflightlist
    override fun showFlight(cityId: UUID, cityName : String, flight: Flight?) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, FlightFragment.newInstance(cityId, cityName,flight), FLIGHT_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(null)
            .commit()
    }

    override fun showAirline(id: UUID) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, CityFragment.newInstance(id), CITY_TAG)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .addToBackStack(null)
            .commit()
    }
}

