//package com.example.geominder
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//
//
//class UserAdapter(private val users: List<String>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
//        return UserViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//        holder.bind(users[position])
//    }
//
//    override fun getItemCount(): Int = users.size
//    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val userNameTextView: TextView = itemView.findViewById(R.id.userName)
//
//        fun bind(user: String) {
//            userNameTextView.text = user
//        }
//    }
//}
