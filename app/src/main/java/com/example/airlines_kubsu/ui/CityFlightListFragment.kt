package com.example.airlines_kubsu.ui

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airlines_kubsu.R
import com.example.airlines_kubsu.data.*
import com.example.airlines_kubsu.databinding.FragmentCityFlightListBinding
import com.example.airlines_kubsu.models.CityFlightListViewModel
import com.example.airlines_kubsu.repository.AppRepository
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CityFlightListFragment(private val city: City) : Fragment() {
    private lateinit var viewModel: CityFlightListViewModel
    private var _binding: FragmentCityFlightListBinding? = null
    private var lastItemView: View? = null
    private val binding get() = _binding!!

    private val flightsTime = arrayOf("10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
    private val daysOfWeek = arrayOf("Воскресенье","Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
    private val planeTypes = arrayOf("superjet S", "superjet M", "superjet L")

    //TODO возможно придется перенести flights в репозиторий и хранить больше параметров в классе FlightPlane

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCityFlightListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCityFlightList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        viewModel = ViewModelProvider(this)[CityFlightListViewModel::class.java]
        binding.rvCityFlightList.adapter = CityFlightListAdapter(city.flights)
    }

    private inner class CityHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        lateinit var flight: Flight

        fun bind(flight: Flight) {
            this.flight = flight
            val s = "${flight.departureCity}-${flight.arrivalCity}"
            itemView.findViewById<TextView>(R.id.tvElement).text = s
            itemView.findViewById<ConstraintLayout>(R.id.flightButtons).visibility = View.GONE
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val cl = itemView.findViewById<ConstraintLayout>(R.id.flightButtons)
            cl.visibility = View.VISIBLE
            lastItemView?.findViewById<ConstraintLayout>(R.id.flightButtons)?.visibility = View.GONE
            lastItemView = if (lastItemView == itemView) null else itemView
            if (cl.visibility == View.VISIBLE) {
                itemView.findViewById<ImageButton>(R.id.flightDelBtn).setOnClickListener {
                    commitDeleteDialog(flight)
                }
                itemView.findViewById<ImageButton>(R.id.flightEditBtn).setOnClickListener {
                    callbacks?.showFlight(city.id, city.name, flight)
                }
                itemView.findViewById<ImageButton>(R.id.ticketBuy).setOnClickListener {
                    buyTicketDialog(flight)
                }
            }
        }
    }

    private fun commitDeleteDialog(flight: Flight) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setMessage("Удалить рейс ${flight.departureCity}-${flight.arrivalCity} из списка?")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            viewModel.deleteFlight(city.id, flight)
        }
        builder.show()
    }

    // диалог для покупки билетов
    private fun buyTicketDialog(flight: Flight) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setTitle("Купить билет")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.buy_ticket, null)
        builder.setView(dialogView)
        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendarView)
        val deptTimeSpinner = dialogView.findViewById<Spinner>(R.id.spinFlightDepTime)
        val arrTimeVal = dialogView.findViewById<TextView>(R.id.tvFlightArrTimeValue)
        val selectSeatBtn = dialogView.findViewById<Button>(R.id.selectSeatBtn)
        val tvFlightInfo = dialogView.findViewById<TextView>(R.id.tvFlightInfoValue)

        tvFlightInfo.text = "${flight.departureCity}-${flight.arrivalCity} день вылета : ${daysOfWeek[flight.flightWeekDay]} самолет: ${planeTypes[flight.plane]}"

        deptTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, flightsTime)

        deptTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                var selectedItem = parent.getItemAtPosition(position).toString()
                val time = LocalTime.parse(selectedItem).plusHours(2)
                selectedItem = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                arrTimeVal.text = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val minDate = Calendar.getInstance()
        minDate.set(Calendar.DAY_OF_WEEK, flight.flightWeekDay + 1)

        if (minDate.before(Calendar.getInstance())) {
            minDate.add(Calendar.WEEK_OF_YEAR, 1)
        }

        calendarView.minDate = minDate.timeInMillis
        // устанавливаем максимальную дату (год вперед)
        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, 1)
        calendarView.maxDate = maxDate.timeInMillis

        val selectedDate = Calendar.getInstance()
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (selectedDate.get(Calendar.DAY_OF_WEEK) != flight.flightWeekDay + 1) {
                selectedDate.set(Calendar.DAY_OF_WEEK, flight.flightWeekDay + 1)
                calendarView.date = selectedDate.timeInMillis
            }
        }

        selectSeatBtn.setOnClickListener {
            val planeType = planeTypes[flight.plane]

            val deptTime = LocalTime.parse(deptTimeSpinner.selectedItem.toString())

            var flightPlane = city.flightPlanes.find {
                it.deptCity == flight.departureCity &&
                it.arrCity == flight.arrivalCity &&
                it.deptTime == deptTime &&
                it.date == selectedDate.time &&
                it.plane.name == planeType
            }
            if (flightPlane == null) {
                val selectedPlaneTypeIndex = planeTypes.indexOf(planeType)
                val planeTypeRows = arrayOf(4, 5, 6)[selectedPlaneTypeIndex]
                val planeTypeCols = 5
                var plane = Plane(
                    name = planeType,
                    amCols = planeTypeCols,
                    amRows = planeTypeRows
                )
                AppRepository.get().addSeats(plane)
                flightPlane = FlightPlane(
                    deptCity = flight.departureCity,
                    arrCity = flight.arrivalCity,
                    deptTime = deptTime,
                    date = selectedDate.time,
                    plane = plane
                )
                city.flightPlanes += flightPlane
            }
            selectSeatDialog(flightPlane)
        }
        builder.setPositiveButton(R.string.commit) { _, _ ->

        }
        // ..
        builder.setNegativeButton(R.string.cansel, null)
        val alert = builder.create()
        alert.show()
    }

    private fun selectSeatDialog(flightPlane: FlightPlane) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setTitle("Забронировать место")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.select_seat, null)
        builder.setView(dialogView)
        //наполнить
        val seats = flightPlane.plane.seats
        val adapter = SeatAdapter(seats, flightPlane.plane!!.amCols)
        val gridView = dialogView.findViewById<GridView>(R.id.gvSeats)
        gridView.numColumns = flightPlane.plane.amCols
        gridView.adapter = adapter

        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (adapter.getSelectedSeat(dialogView) != -1){
                city.flightPlanes.find { it.id == flightPlane.id }!!.plane.seats.find { it.position == adapter.getSelectedSeat(dialogView)+1 }!!.isBooked = true
            }
        }
        //
        builder.setNegativeButton(R.string.cansel, null)
        val alert = builder.create()
        alert.show()
    }

    class SeatAdapter(private var seats: List<Seat>, private val cols: Int) : BaseAdapter() {

        private var selectedSeatPosition = -1 // индекс выбранного места

        override fun getCount(): Int = seats.size

        override fun getItem(position: Int): Any = seats[position]

        override fun getItemId(position: Int): Long = position.toLong()

        private fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val button = convertView as? Button ?: Button(parent?.context)
            val row = position / cols // индекс строки
            val col = position % cols // индекс колонки
            val seat = seats[row * cols + col] // выберите место на основе индексов строки и колонки
            button.text = seat.name
            button.isEnabled = !seat.isBooked
            button.setBackgroundColor(
                ContextCompat.getColor(
                    parent?.context!!,
                    if (seat.isBooked) R.color.red else R.color.grey
                )
            )
            button.setOnClickListener {
                if (!seat.isBooked) {
                    // установить цвет выбранной кнопки на синий
                    button.setBackgroundColor(
                        ContextCompat.getColor(
                            parent?.context!!,
                            R.color.blue
                        )
                    )
                    if (selectedSeatPosition != -1) {
                        parent.getChildAt(selectedSeatPosition).setBackgroundColor(
                            ContextCompat.getColor(
                                parent?.context!!,
                                R.color.grey
                            )
                        )
                    }
                    selectedSeatPosition = position
                } else {
                    Toast.makeText(parent?.context, "Место занято", Toast.LENGTH_LONG).show()
                }
            }
            val params = AbsListView.LayoutParams(
                35.dpToPx(),
                35.dpToPx()
            )
            button.layoutParams = params
            return button
        }

        fun getSelectedSeat(parent: View): Int {
            return selectedSeatPosition
        }
    }

    private inner class CityFlightListAdapter(private val items: List<Flight>) :
        RecyclerView.Adapter<CityFlightListFragment.CityHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): CityHolder {
                    val view =
                        layoutInflater.inflate(
                            R.layout.fragment_city_flight_list_element,
                            parent,
                            false
                        )
                    return CityHolder(view)
                }

                override fun getItemCount(): Int = items.size

                override fun onBindViewHolder(holder: CityHolder, position: Int) {
                    holder.bind(items[position])
                }
            }

    interface Callbacks {
        fun showFlight(cityId: UUID, cityName : String, flight: Flight?)
    }

    var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        callbacks = null
        super.onDetach()
    }
}