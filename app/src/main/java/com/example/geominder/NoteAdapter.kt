package com.example.geominder

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.type.Date
import java.util.Locale

class NoteAdapter(private val groupedNotes: List<Pair<String, List<Note>>>,
                  private val onEditClicked: (Note) -> Unit,
                  private val onDeleteClicked: (Note) -> Unit,
                  private val onPinClicked: (Note) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_NOTE = 1
    }

    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeader: TextView = itemView.findViewById(R.id.dateHeader)
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.noteTitle)
        val noteContent: TextView = itemView.findViewById(R.id.noteContent)
        val noteTime: TextView = itemView.findViewById(R.id.noteTime)
        val notePlace: TextView = itemView.findViewById(R.id.notePlace)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val pinButton: ImageButton = itemView.findViewById(R.id.pinButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.data_header_item, parent, false)
                DateHeaderViewHolder(view)
            }
            TYPE_NOTE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
                NoteViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentIndex = 0

        // Iterate over the groupedNotes and adjust the position to find the correct item
        for (group in groupedNotes) {
            // The first item in each group is the date header
            if (position == currentIndex) {
                val dateHeaderHolder = holder as DateHeaderViewHolder
                dateHeaderHolder.dateHeader.text = formatDate(group.first)
                return
            }
            currentIndex++

            // Now find the note in the current group
            val notesInGroup = group.second
            if (position < currentIndex + notesInGroup.size) {
                val notePosition = position - currentIndex
                val note = notesInGroup[notePosition]
                val noteHolder = holder as NoteViewHolder
                noteHolder.noteTitle.text = note.title
                noteHolder.noteContent.text = note.content
                noteHolder.noteTime.text = note.time
                noteHolder.notePlace.text = note.place

                // Edit button click
                noteHolder.editButton.setOnClickListener {
                    onEditClicked(note)
                }

                // Delete button click
                noteHolder.deleteButton.setOnClickListener {
                    onDeleteClicked(note)
                }

                // Pin button click
                noteHolder.pinButton.setOnClickListener {
                    onPinClicked(note)
                }

                return
            }
            currentIndex += notesInGroup.size
        }
    }

    override fun getItemCount(): Int {
        return groupedNotes.sumOf { it.second.size + 1 } // +1 for the date header
    }

    override fun getItemViewType(position: Int): Int {
        var currentIndex = 0

        // Iterate through groupedNotes and check for the position of the date header
        for (group in groupedNotes) {
            if (position == currentIndex) {
                return TYPE_DATE_HEADER
            }
            currentIndex++

            // Move to the next position for the notes in this group
            val notesInGroup = group.second
            if (position < currentIndex + notesInGroup.size) {
                return TYPE_NOTE
            }
            currentIndex += notesInGroup.size
        }

        throw IllegalArgumentException("Invalid position $position")
    }

    private fun formatDate(dateString: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = sdf.parse(dateString)
            val newFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            newFormat.format(date ?: java.util.Date())
        } catch (e: Exception) {
            dateString
        }
    }
}
