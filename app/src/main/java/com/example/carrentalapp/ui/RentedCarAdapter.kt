package com.example.carrentalapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carrentalapp.R
import com.example.carrentalapp.model.Rental

class RentedCarAdapter(
    private var rentals: List<Rental>,
) : RecyclerView.Adapter<RentedCarAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.rentedImage)
        val name: TextView = view.findViewById(R.id.rentedName)
        val period: TextView = view.findViewById(R.id.rentedPeriod)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rented, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rental = rentals[position]
        holder.image.setImageResource(rental.car.imageResId)
        holder.name.text = rental.car.name
        holder.period.text = holder.itemView.context.resources.getQuantityString(R.plurals.rental_days, rental.days, rental.days)
    }

    override fun getItemCount() = rentals.size

    fun update(newRentals: List<Rental>) {
        rentals = newRentals
        notifyDataSetChanged()
    }
}
