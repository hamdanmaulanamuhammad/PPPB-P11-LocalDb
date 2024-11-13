package com.example.pppbp11_localdb.ui

import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pppbp11_localdb.R
import com.example.pppbp11_localdb.database.Note
import com.example.pppbp11_localdb.database.NoteDao
import com.example.pppbp11_localdb.database.NoteRoomDatabase
import com.example.pppbp11_localdb.databinding.ActivityMainBinding
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mNotesDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNotesDao = db!!.noteDao()!!

        with(binding){
            btnAdd.setOnClickListener(View.OnClickListener {
                insert(
                    Note(
                        title = edtTitle.text.toString(),
                        description = edtDesc.text.toString(),
                        date = edtDate.text.toString()
                    )
                )
                setEmptyField()
            })
            btnUpdate.setOnClickListener {
                update(
                    Note(
                        id = updateId,
                        title = edtTitle.getText().toString(),
                        description = edtDesc.getText().toString(),
                        date = edtDate.getText().toString()
                    )
                )
                updateId = 0
                setEmptyField()
            }
            listView.setOnItemClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
            }
            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { adapterView, _, i, _ ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    fun insert(note: Note){
        executorService.execute{
            mNotesDao.insert(note)
        }
    }
    private fun delete(note: Note) {
        executorService.execute {
            mNotesDao.delete(note)
        }
    }
    private fun update(note: Note) {
        executorService.execute {
            mNotesDao.update(note)
        }
    }

    fun getAllNotes(){
        mNotesDao.allNotes.observe(this){
                notes ->
            val adapter : ArrayAdapter<Note> =
                ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1, notes)

            binding.listView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    fun setEmptyField(){
        with(binding){
            edtDesc.setText("")
            edtTitle.setText("")
            edtTitle.setText("")
        }
    }
}