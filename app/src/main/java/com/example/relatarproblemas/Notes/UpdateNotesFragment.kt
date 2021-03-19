package com.example.relatarproblemas.Notes

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.relatarproblemas.Notes.Model.Note
import com.example.relatarproblemas.Notes.ViewModel.NoteViewModel
import com.example.relatarproblemas.R
import kotlinx.android.synthetic.main.fragment_update_notes.*
import kotlinx.android.synthetic.main.fragment_update_notes.view.*

class UpdateNotesFragment : Fragment() {

    private val args by navArgs<UpdateNotesFragmentArgs>()

    private lateinit var mNoteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update_notes, container, false)

        mNoteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        view.update_note_title_editText.setText(args.currentNote.title)
        view.update_note_description_editText.setText(args.currentNote.content)

        view.update_save_button.setOnClickListener {
            updateItem()
        }

        

        //Add menu
        setHasOptionsMenu(true)

        return view
    }

    private fun updateItem() {
        val title = update_note_title_editText.text.toString()
        val description = update_note_description_editText.text.toString()

        if (inputCheck(title, description)){
            //Create User Object
            val updatedNote = Note(args.currentNote.id, title, description, args.currentNote.date)
            //Update Current User
            mNoteViewModel.updateNote(updatedNote)
            Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()
            //Navigate Back
            findNavController().popBackStack()
        }
        else{
            Toast.makeText(requireContext(), "Please fill out all fields!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputCheck(title : String, description : String): Boolean {
        return !(TextUtils.isEmpty(title) || TextUtils.isEmpty(description))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_delete){
            deleteNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNote() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_,_ ->
            mNoteViewModel.deleteNote(args.currentNote)
            Toast.makeText(requireContext(), "Successfully removed ${args.currentNote.title}", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        builder.setNegativeButton("No"){ _, _ -> }
        builder.setTitle("Delete ${args.currentNote.title}?")
        builder.setMessage("Are you sure you want to delete ${args.currentNote.title}?")
        builder.create().show()
    }



}