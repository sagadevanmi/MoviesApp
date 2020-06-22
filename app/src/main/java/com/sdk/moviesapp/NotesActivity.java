package com.sdk.moviesapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sdk.moviesapp.adapter.NotesAdapter;
import com.sdk.moviesapp.data.NotesDbHelper;
import com.sdk.moviesapp.model.Notes;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {
    RecyclerView recyclerView ;
    FloatingActionButton floatingActionButton;
    List<Notes> notesList;
    Context context;
    NotesDbHelper notesDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        context = this;

        recyclerView = findViewById(R.id.notes_rv);
        floatingActionButton = findViewById(R.id.notes_fabButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notes");
        actionBar.setDisplayHomeAsUpEnabled(true);

        notesList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        notesDbHelper = new NotesDbHelper(context);
        notesList = notesDbHelper.fetch();

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new NotesAdapter(context, notesList));

    }
}
