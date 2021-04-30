package com.example.relatarproblemas.Notes.Recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.ListFragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.relatarproblemas.Notes.ListNotesFragmentDirections
import com.example.relatarproblemas.Notes.Model.Note
import com.example.relatarproblemas.R
import kotlinx.android.synthetic.main.note_custom_row.view.*

class ListAdapter:RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var noteList = emptyList<Note>()

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.note_custom_row, parent, false))
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = noteList[position]
        holder.itemView.id_textView.text = currentItem.id.toString()
        holder.itemView.title_textView.text = currentItem.title

        holder.itemView.row_layout.setOnClickListener{
            val action = ListNotesFragmentDirections.actionListNotesFragmentToUpdateNotesFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(note: List<Note>){
        this.noteList = note
        notifyDataSetChanged()
    }
}