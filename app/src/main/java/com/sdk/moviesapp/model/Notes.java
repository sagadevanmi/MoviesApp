package com.sdk.moviesapp.model;

public class Notes {

    public int id;
    public String title;
    public String note;
    public String posterPath;

    public Notes() {
    }

    public Notes(int id, String title, String note, String posterPath) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
