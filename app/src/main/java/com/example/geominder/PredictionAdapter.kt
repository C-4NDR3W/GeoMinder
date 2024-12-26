import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geominder.R
import com.example.geominder.models.Prediction


class PlaceAdapter(private val placeList: MutableList<Prediction>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    // Define the ViewHolder class to hold references to the views for each item
    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.placeName)
        val placeAddress: TextView = itemView.findViewById(R.id.placeAddress)
    }

    // Inflate the item layout and return a new PlaceViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_suggestion_1line, parent, false)
        return PlaceViewHolder(view)
    }

    // Bind the data (Place) to the ViewHolder
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address

        Log.d("PredictionView", "ukuran : ${placeList.size}")
        holder.itemView.setOnClickListener {
            // Handle item click here
        }
    }

    // Return the total count of items in the list
    override fun getItemCount(): Int {
        return placeList.size
    }
}