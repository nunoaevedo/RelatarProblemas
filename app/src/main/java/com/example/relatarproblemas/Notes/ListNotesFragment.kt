package com.example.relatarproblemas.Notes

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.relatarproblemas.Notes.ViewModel.NoteViewModel
import com.example.relatarproblemas.Notes.Recycler.ListAdapter
import com.example.relatarproblemas.R
import kotlinx.android.synthetic.main.fragment_list_notes.view.*


class ListNotesFragment : Fragment() {

    private lateinit var mNoteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list_notes, container, false)

        //RecyclerView
        val adapter = ListAdapter()
        val recyclerView = view.listNotesRecycler
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //NoteViewModel
        mNoteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        mNoteViewModel.readAllData.observe(viewLifecycleOwner, Observer { note ->
            adapter.setData(note)
        })

        view.floatingActionButton.setOnClickListener{
            findNavController().navigate(R.id.action_listNotesFragment_to_addNotesFragment)
        }

        //Add menu
        setHasOptionsMenu(true)


        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delete){
            deleteAllNotes()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllNotes() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_ ->
            mNoteViewModel.deleteAllNotes()
            Toast.makeText(requireContext(), "Successfully removed all notes", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No"){ _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to delete everything?")
        builder.create().show()
    }
}