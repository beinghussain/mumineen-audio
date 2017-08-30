package com.mumineendownloads.mumineenaudio.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Audio {

    private ArrayList<AudioItem> audios;

    public ArrayList<AudioItem> getAudios() {
        return audios;
    }

    public void setAudios(ArrayList<AudioItem> audios) {
        this.audios = audios;
    }

    public static class AudioItem implements Serializable {

        private int id;
        private String title;
        private String album;
        private String source;
        private int size;
        private int aid;
        private int pdf_id;
        private String cat;

        public String getCat() {
            return cat;
        }

        public void setCat(String cat) {
            this.cat = cat;
        }

        public AudioItem() {

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

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getAid() {
            return aid;
        }

        public void setAid(int aid) {
            this.aid = aid;
        }

        public int getPdf_id() {
            return pdf_id;
        }

        public void setPdf_id(int pdf_id) {
            this.pdf_id = pdf_id;
        }
    }
}
