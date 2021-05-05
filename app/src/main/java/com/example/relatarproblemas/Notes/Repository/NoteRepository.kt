package com.example.relatarproblemas.Notes.Repository

import androidx.lifecycle.LiveData
import com.example.relatarproblemas.Notes.Data.NoteDao
import com.example.relatarproblemas.Notes.Model.Note

class NoteRepository(private val noteDao: NoteDao) {

    val readAllData: LiveData<List<Note>> = noteDao.readAllData()

    suspend fun addNote(note: Note){
        noteDao.addNote(note)
    }

    suspend fun updateNote(note: Note){
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note){
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotes(){
        noteDao.deleteAllNotes()
    }


}