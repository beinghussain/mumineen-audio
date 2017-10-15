package com.mumineendownloads.mumineenaudio.Models;

/**
 * Created by hussaindehgamwala on 10/8/17.
 */

public class AlbumModel {
    String title;
    int size;
    int id;

    public AlbumModel(String title, int size, int id) {
        this.title = title;
        this.size = size;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AlbumModel() {
    }
}
