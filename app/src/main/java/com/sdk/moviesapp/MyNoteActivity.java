package com.sdk.moviesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sdk.moviesapp.data.NotesDbHelper;
import com.sdk.moviesapp.model.Notes;

public class MyNoteActivity extends AppCompatActivity {

    EditText etNotes, etTitle;
    Toolbar toolbar;
    Button btnSave;
    Context activity;
    NotesDbHelper notesDbHelper;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_note);
        activity = getApplicationContext();
        Intent intent = getIntent();
        title = intent.getStringExtra("title");

        etNotes = findViewById(R.id.notes_et);
        etTitle = findViewById(R.id.title_et);
        etTitle.setText(title);
        btnSave = findViewById(R.id.btnSave);
        toolbar = findViewById(R.id.Notestoolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etNotes.getText().toString();
                saveNote(text);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveNote(etNotes.getText().toString());
    }

    private void saveNote(String text) {
        if (!text.equals("")) {
            notesDbHelper = new NotesDbHelper(activity);
            String poster = getIntent().getStringExtra("poster_path");

            Notes note = new Notes();
            note.setNote(text);
            note.setPosterPath(poster);
            note.setTitle(title);

            notesDbHelper.addNote(note);

        } else {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
        }
    }


}
