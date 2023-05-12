package com.example.airlines_kubsu.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.airlines_kubsu.R
import com.example.airlines_kubsu.data.Airline
import com.example.airlines_kubsu.data.City
import com.example.airlines_kubsu.data.Flight
import com.example.airlines_kubsu.databinding.FragmentCityBinding
import com.example.airlines_kubsu.models.CityViewModel
import com.example.airlines_kubsu.repository.AppRepository
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*


const val CITY_TAG = "CityFragment"

class CityFragment private constructor(): Fragment() {
    private var _binding: FragmentCityBinding? = null
    private lateinit var viewModel: CityViewModel

    private val binding
        get() = _binding!!

    companion object {
        private lateinit var id: UUID
        private var _city: City? = null

        fun newInstance(id: UUID): CityFragment {
            this.id = id
            return CityFragment()
        }

        val getAirlineId
            get() = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CityViewModel::class.java]
        viewModel.setAirlineId(getAirlineId)
        viewModel.airlines.observe(viewLifecycleOwner) {
            updateUI(it)
            callbacks?.setTitle(it?.name ?: "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // указываем, что фрагмент имеет своё меню
    }

    private var tabPosition: Int = 0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miDelBtn -> {
                val cityToDelete: City? = _city
                if (binding.tabCity.tabCount > 0) {
                    val tabIndexToRemove = tabPosition // Сохраняем текущую позицию вкладки
                    binding.tabCity.removeTabAt(tabIndexToRemove)
                    tabPosition = if (tabIndexToRemove > 0) {
                        // Если удаляемая вкладка не была первой,
                        // то обновляем позицию текущей вкладки
                        tabIndexToRemove - 1
                    } else {
                        // В противном случае, если первую вкладку удаляем,
                        // то обновляем текущую позицию вкладки на 0
                        0
                    }
                }
                AppRepository.get().deleteCity(airlineId = getAirlineId, cityToDelete)
                true
            }             // Обработка нажатия на пункт меню "удалить" из фрагмента
            R.id.miEditBtn -> {
                if (_city != null) {
                    showEditCityDialog()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(airline: Airline?) {
        binding.tabCity.clearOnTabSelectedListeners()
        binding.tabCity.removeAllTabs()
        binding.faBtnNewFlight.visibility = if ((airline?.cities?.size ?: 0) > 0) {
            binding.faBtnNewFlight.setOnClickListener {
                callbacks?.showFlight(airline?.cities!![tabPosition].id, airline?.cities!![tabPosition].name,null)
            }
            View.VISIBLE
        } else
            View.GONE
        for (i in 0 until (airline?.cities?.size ?: 0)) {
            binding.tabCity.addTab(binding.tabCity.newTab().apply {
                text = i.toString()
            })
        }

        val adapter = CityPageAdapter(requireActivity(), airline!!)
        binding.vpCity.adapter = adapter
        TabLayoutMediator(binding.tabCity, binding.vpCity, true, true) { tab, pos ->
            tab.text = airline.cities[pos].name
        }.attach()

        binding.tabCity.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabPosition = tab?.position!!
                _city = airline.cities.getOrNull(tabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabPosition = tab?.position!!
                _city = airline.cities.getOrNull(tabPosition)
            }
        })

        binding.tabCity.selectTab(binding.tabCity.getTabAt(tabPosition)) //выбираем tab, чтобы при переходе из фрагмента мы сразу могли удалить или изменить без реселекта
    }

    private inner class CityPageAdapter(fa: FragmentActivity, private val airline: Airline) :
        FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return airline.cities?.size!!
        }

        override fun createFragment(position: Int): Fragment {
            return CityFlightListFragment(airline.cities?.get(position)!!)
        }
    }

    private fun showEditCityDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.name_input, null)
        builder.setView(dialogView)
        val nameInput = dialogView.findViewById(R.id.etAirlineCity) as EditText
        val yearInput = dialogView.findViewById(R.id.etAirlineCreateYear) as EditText
        builder.setTitle(R.string.new_city)
        nameInput.hint = getString(R.string.hint_enter_city)
        nameInput.setText(_city?.name ?: "")
        yearInput.visibility = View.GONE
        builder.setPositiveButton(R.string.ok) { _, _ ->
            val n = nameInput.text.toString()
            if (n.isNotBlank()) {
                //TODO: переписать, используя модель для вызова
                AppRepository.get().editCity(getAirlineId, _city!!.id, n)
            }
        }
        builder.setNegativeButton(R.string.cansel, null)
        val alert = builder.create()
        alert.show()
    }

    interface Callbacks {
        fun setTitle(_title: String)
        fun showFlight(cityId: UUID, cityName: String, flight: Flight?)
    }

    var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }
}