package com.example.carrentalapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.R
import com.example.carrentalapp.model.Car

class SearchResultAdapter(
    private var cars: List<Car>,
    private val onCarClick: (Car) -> Unit,
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.searchResultName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val car = cars[position]
        holder.name.text = car.name
        holder.itemView.setOnClickListener { onCarClick(car) }
    }

    override fun getItemCount() = cars.size

    fun update(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
