package com.example.relatarproblemas.Notes

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
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

        if((activity as NotesActivity).supportActionBar != null){
            val actionBar = (activity as NotesActivity).supportActionBar
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)

        }


        return view
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_delete -> {
            deleteAllNotes()
            true
        }
        android.R.id.home -> {
            activity?.finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun deleteAllNotes() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            mNoteViewModel.deleteAllNotes()
            Toast.makeText(requireContext(), getString(R.string.successfully_removed_all), Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(getString(R.string.no)){ _, _ -> }
        builder.setTitle(getString(R.string.delete_everything))
        builder.setMessage(getString(R.string.delete_everything_confirmation))
        builder.create().show()
    }
}