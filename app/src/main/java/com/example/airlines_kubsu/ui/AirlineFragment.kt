package com.example.airlines_kubsu.ui

import android.content.ClipData.Item
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airlines_kubsu.R
import com.example.airlines_kubsu.databinding.FragmentAirlineBinding
import com.example.airlines_kubsu.data.Airline
import com.example.airlines_kubsu.models.AirlineViewModel
import com.example.airlines_kubsu.repository.AppRepository
import java.util.*

const val AIRLINE_TAG = "AirlineFragment"
const val AIRLINE_TITLE = "Авиакомпании"

class AirlineFragment : Fragment() {
    private lateinit var viewModel: AirlineViewModel
    private var _binding: FragmentAirlineBinding? = null
    private var lastItemView: View? = null
    private val binding
        get() = _binding!!

    private var adapter: AirlineListAdapter = AirlineListAdapter(emptyList())

    companion object {
        fun newInstance() = AirlineFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAirlineBinding.inflate(inflater, container, false)
        binding.rwAirline.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AirlineViewModel::class.java]
        viewModel.airlines.observe(viewLifecycleOwner) {
            adapter = AirlineListAdapter(it)
            binding.rwAirline.adapter = adapter
        }
        callbacks?.setTitle(AIRLINE_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // указываем, что фрагмент имеет своё меню
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.miEditBtn).isVisible = false
        menu.findItem(R.id.miDelBtn).isVisible = false
    }

    private inner class AirlineHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        lateinit var airline : Airline

        fun bind(airline: Airline) {
            this.airline = airline
            itemView.findViewById<TextView>(R.id.tvAirlineElement).text = airline.name
            itemView.findViewById<ConstraintLayout>(R.id.airlineButtons).visibility = View.GONE
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val cl = itemView.findViewById<ConstraintLayout>(R.id.airlineButtons)
            cl.visibility = View.VISIBLE
            lastItemView?.findViewById<ConstraintLayout>(R.id.airlineButtons)?.visibility = View.GONE
            lastItemView = if (lastItemView == itemView) null else itemView
            if (cl.visibility == View.VISIBLE) {
                itemView.findViewById<ImageButton>(R.id.airlineMoreBtn).setOnClickListener {
                    callbacks?.showAirline(airline.id)
                }
                itemView.findViewById<ImageButton>(R.id.airlineDelBtn).setOnClickListener {
                    showDeleteDialog(airline)
                }
                itemView.findViewById<ImageButton>(R.id.airlineEditBtn).setOnClickListener {
                    showEditDialog(airline.id, airline.name, airline.year)
                }
            }
        }
    }

    private fun showEditDialog(airlineId: UUID, name: String = "", year: String = "") {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.name_input, null)
        builder.setView(dialogView)
        val nameInput = dialogView.findViewById(R.id.etAirlineCity) as EditText
        val yearInput = dialogView.findViewById(R.id.etAirlineCreateYear) as EditText
        builder.setTitle(R.string.new_airline)
        nameInput.hint = getString(R.string.hint_enter_airline_name)
        yearInput.hint = getString(R.string.hint_enter_airline_year)
        nameInput.setText(name)
        yearInput.setText(year)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            var n = nameInput.text.toString()
            var y = yearInput.text.toString()
            //TODO: переписать, используя модель для вызова
            if (n.isNotBlank() and y.isNotBlank())
                AppRepository.get().editAirline(airlineId, name = n, year = y)
        }
        builder.setNegativeButton(R.string.cansel, null)
        val alert = builder.create()
        alert.show()
    }

    private fun showDeleteDialog(airline: Airline) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setMessage("Удалить авиакомпанию ${airline.name} из списка?")
        builder.setTitle("Подтверждение")
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            //TODO вызов через модель
            AppRepository.get().deleteAirline(airline)
        }
        builder.show()
    }

    private inner class AirlineListAdapter(private val items: List<Airline>) : RecyclerView.Adapter<AirlineHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirlineHolder {
            val view = layoutInflater.inflate(R.layout.element_airline_list, parent, false)
            return AirlineHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: AirlineHolder, position: Int) {
            holder.bind(items[position])
        }
    }

    interface Callbacks {
        fun setTitle(_title: String)
        fun showAirline(id : UUID)
    }

    var callbacks : Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        callbacks = null
        super.onDetach()
    }
}