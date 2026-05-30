package com.example.carrentalapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.R
import com.example.carrentalapp.model.Car

class FavouriteAdapter(
    private var cars: List<Car>,
    private val onCarClick: (Car) -> Unit,
) : RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.favImage)
        val name: TextView = view.findViewById(R.id.favName)
        val price: TextView = view.findViewById(R.id.favPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favourite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val car = cars[position]
        holder.image.setImageResource(car.imageResId)
        holder.name.text = car.name
        holder.price.text = holder.itemView.context.getString(R.string.favourite_price, car.dailyCost.toString())
        holder.itemView.setOnClickListener { onCarClick(car) }
    }

    override fun getItemCount() = cars.size

    fun update(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
