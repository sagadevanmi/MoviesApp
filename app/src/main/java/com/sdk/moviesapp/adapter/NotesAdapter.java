package com.sdk.moviesapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sdk.moviesapp.MyNoteActivity;
import com.sdk.moviesapp.R;
import com.sdk.moviesapp.model.Notes;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder>{

    Context context;
    public List<Notes> notesList;

    public NotesAdapter (Context context, List<Notes> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public NotesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.notes_card, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(notesList.get(position).getTitle());

        Glide.with(context).load(notesList.get(position).getPosterPath())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.thumbnail);
    }


    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail;
        public MyViewHolder(@NonNull View view) {
            super(view);
            title = view.findViewById(R.id.title);
            thumbnail = view.findViewById(R.id.imageView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Notes clickedData = notesList.get(pos);
                        Toast.makeText(view.getContext(), "You clicked" + clickedData.getTitle(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, MyNoteActivity.class);
                        intent.putExtra("title", notesList.get(pos).getTitle());
                        intent.putExtra("poster_path", notesList.get(pos).getPosterPath());
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
