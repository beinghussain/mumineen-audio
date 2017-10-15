package com.mumineendownloads.mumineenaudio.Models;

import java.util.ArrayList;

/**
 * Created by hussaindehgamwala on 10/8/17.
 */


public class SectionDataModel {

        private String headerTitle;
        private ArrayList<Audio.AudioItem> allItemsInSection;
        private ArrayList<AlbumModel> allAlbumsSection;


        public SectionDataModel() {

        }


        public SectionDataModel(String headerTitle, ArrayList<Audio.AudioItem> allItemsInSection) {
            this.headerTitle = headerTitle;
            this.allItemsInSection = allItemsInSection;
        }

        public SectionDataModel(String headerTitle, ArrayList<Audio.AudioItem> audioItems, ArrayList<AlbumModel> allAlbumsSection) {
            this.headerTitle = headerTitle;
            this.allAlbumsSection = allAlbumsSection;
            this.allItemsInSection = audioItems;
        }

        public String getHeaderTitle() {
            return headerTitle;
        }

        public void setHeaderTitle(String headerTitle) {
            this.headerTitle = headerTitle;
        }

        public ArrayList<Audio.AudioItem> getAllItemsInSection() {
            return allItemsInSection;
        }

        public void setAllItemsInSection(ArrayList<Audio.AudioItem> allItemsInSection) {
            this.allItemsInSection = allItemsInSection;
        }


        public ArrayList<AlbumModel> getAllAlbumsSection() {
            return allAlbumsSection;
        }

        public void setAllAlbumsSection(ArrayList<AlbumModel> allAlbumsSection) {
            this.allAlbumsSection = allAlbumsSection;
        }
}
