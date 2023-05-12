package com.example.airlines_kubsu.ui

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import com.example.airlines_kubsu.R
import com.example.airlines_kubsu.databinding.FragmentFlightBinding
import com.example.airlines_kubsu.data.Flight
import com.example.airlines_kubsu.models.FlightViewModel
import com.example.airlines_kubsu.repository.AppRepository
import java.util.*

const val FLIGHT_TAG="FlightFragment"

class FlightFragment : Fragment() {
    private var _binding: FragmentFlightBinding? = null
    private lateinit var viewModel: FlightViewModel

    private val binding
        get() = _binding!!

    private val daysOfWeek = arrayOf("Воскресенье","Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
    private val planeTypes = arrayOf("superjet S", "superjet M", "superjet L")

    companion object {
        lateinit var cityId: UUID
        lateinit var cityName: String
        var flight: Flight? = null
        fun newInstance(cityId: UUID, cityName: String, flight: Flight?): FlightFragment {
            this.cityId = cityId
            this.cityName = cityName
            this.flight = flight
            return FlightFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFlightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FlightViewModel::class.java]

        val adapterDayOfWeek = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, daysOfWeek)
        binding.spinFlightDayOfWeek.adapter = adapterDayOfWeek
        val adapterPlane = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, planeTypes)
        binding.spinSelectPlane.adapter = adapterPlane

        binding.tvFlightDepCityValue.text = cityName

        if (flight!= null) {
            binding.etFlightArrCity.setText(flight!!.arrivalCity)
            binding.etFlightCost.setText(flight!!.flightCost.toString())
            binding.etBaggageCost.setText(flight!!.baggageCost.toString())
            binding.spinFlightDayOfWeek.setSelection(flight!!.flightWeekDay)
            binding.spinSelectPlane.setSelection(flight!!.plane)
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showCommitDialog()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun showCommitDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setCancelable(true)
        builder.setMessage("Сохранить изменения?")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(R.string.ok) { _, _ ->
            var p = true

            binding.etFlightArrCity.text.toString().ifBlank {
                p = false
                binding.etFlightArrCity.error= "Укажите значение"
            }
            if (!binding.etFlightCost.text.toString().matches(Regex("^\\d+$"))) {
                p = false
                binding.etFlightCost.error= "Поле должно содержать только цифры и не быть пустым"
            }
            if (!binding.etBaggageCost.text.toString().matches(Regex("^\\d+$"))) {
                p = false
                binding.etBaggageCost.error= "Поле должно содержать только цифры и не быть пустым"
            }
            if (p) {
                if (flight == null) {
                    flight = Flight()
                    flight?.apply {
                        departureCity = cityName
                        arrivalCity = binding.etFlightArrCity.text.toString()
                        flightCost = binding.etFlightCost.text.toString().toInt()
                        baggageCost = binding.etBaggageCost.text.toString().toInt()
                        flightWeekDay = binding.spinFlightDayOfWeek.selectedItemPosition
                        plane = binding.spinSelectPlane.selectedItemPosition
                    }
                    viewModel.newFlight(cityId!!, flight!!)
                } else {
                    flight?.apply {
                        departureCity = cityName
                        arrivalCity = binding.etFlightArrCity.text.toString()
                        flightCost = binding.etFlightCost.text.toString().toInt()
                        baggageCost = binding.etBaggageCost.text.toString().toInt()
                        flightWeekDay = binding.spinFlightDayOfWeek.selectedItemPosition
                        plane = binding.spinSelectPlane.selectedItemPosition
                    }
                }
                backPressedCallback.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        builder.setNegativeButton(R.string.cansel) { _, _ ->
            backPressedCallback.isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        var alert = builder.create()
        alert.show()
    }
}