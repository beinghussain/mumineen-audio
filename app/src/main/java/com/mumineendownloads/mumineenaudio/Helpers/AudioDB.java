package com.mumineendownloads.mumineenaudio.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.mumineendownloads.mumineenaudio.Models.Audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AudioDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "audio_manager";
    private static final String TABLE_AUDIO = "audio";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SIZE = "size";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_AID = "aid";
    private static final String KEY_CAT = "cat";
    private static final String KEY_PDF = "pdf";


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_AUDIO + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_ALBUM + " TEXT," + KEY_SOURCE + " TEXT," + KEY_SIZE + " TEXT," +
                KEY_AID  + " INTEGER UNIQUE,"  +  KEY_CAT + " TEXT" + ", "+ KEY_PDF+" INTEGER)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIO);
        onCreate(db);
    }

    private Context context;
    private ArrayList<Audio.AudioItem> downloaded;

    public AudioDB(Context context){
        super(context, "database.db", null, 1);
        this.context = context;
    }

    public void addAudio(Audio.AudioItem audioItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ID, audioItem.getId());
            values.put(KEY_TITLE, audioItem.getTitle());
            values.put(KEY_ALBUM, audioItem.getAlbum());
            values.put(KEY_SOURCE, audioItem.getSource());
            values.put(KEY_SIZE, audioItem.getSize());
            values.put(KEY_AID, audioItem.getAid());
            values.put(KEY_CAT, audioItem.getCat());
            db.insert(TABLE_AUDIO, null, values);
        }catch (SQLiteConstraintException ignored){

        }
        db.close();
    }

    public Audio.AudioItem getAudio(int aid) {
        try {
            ArrayList<Audio.AudioItem> arrayList = new ArrayList<Audio.AudioItem>();
            String selectQuery = "SELECT  * FROM " + TABLE_AUDIO + " WHERE aid = " + aid;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Audio.AudioItem audioItem = new Audio.AudioItem();
                    audioItem.setId(Integer.parseInt(cursor.getString(0)));
                    audioItem.setTitle(cursor.getString(1));
                    audioItem.setAlbum(cursor.getString(2));
                    audioItem.setSource(cursor.getString(3));
                    audioItem.setSize(Integer.parseInt(cursor.getString(4)));
                    audioItem.setAid(Integer.parseInt(cursor.getString(5)));
                    audioItem.setCat(cursor.getString(6));
                    audioItem.setPdf_id(Integer.parseInt(cursor.getString(7)));
                    arrayList.add(audioItem);
                } while (cursor.moveToNext());
            }
            return arrayList.get(0);
        }catch (IndexOutOfBoundsException ignored){
            return null;
        }
    }

    public ArrayList<Audio.AudioItem> getAllPDFS(String album) {
        List<String> files = Utils.getFiles();
        ArrayList<Audio.AudioItem> arrayList = new ArrayList<Audio.AudioItem>();
        String selectQuery;
        switch (album) {
            case "all":
                selectQuery = "SELECT  * FROM " + TABLE_AUDIO + " ORDER BY title";
                break;
            case "Quran30":
                selectQuery = "SELECT  * FROM " + TABLE_AUDIO + " WHERE album in ('Quran30','QuranSurat') ORDER BY title";
                break;
            default:
                selectQuery = "SELECT  * FROM " + TABLE_AUDIO + " WHERE album = '" + album + "' ORDER BY title";
                break;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Audio.AudioItem audioItem = new Audio.AudioItem();
                audioItem.setId(Integer.parseInt(cursor.getString(0)));
                audioItem.setTitle(cursor.getString(1));
                audioItem.setAlbum(cursor.getString(2));
                audioItem.setSource(cursor.getString(3));
                audioItem.setSize(Integer.parseInt(cursor.getString(4)));
                audioItem.setAid(Integer.parseInt(cursor.getString(5)));
                audioItem.setCat(cursor.getString(6));
                audioItem.setPdf_id(Integer.parseInt(cursor.getString(7)));
                arrayList.add(audioItem);
            } while (cursor.moveToNext());
        }return arrayList;
    }

    public int updatePDF(Audio.AudioItem audioItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        return db.update(TABLE_AUDIO, values, KEY_AID + " = ?",
                new String[] { String.valueOf(audioItem.getAid()) });
    }

    public void deletePDF(Audio.AudioItem pdf) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_AUDIO, KEY_ID + " = ?",
                new String[] { String.valueOf(pdf.getId()) });
        db.close();
    }

    public void updateOrInsert(final Audio.AudioItem pdf) {
        final SQLiteDatabase db = this.getWritableDatabase();
                ContentValues initialValues = new ContentValues();
                initialValues.put(KEY_ID, pdf.getId());
                initialValues.put(KEY_SIZE, pdf.getSize());
                initialValues.put(KEY_SOURCE, pdf.getSource());
                initialValues.put(KEY_ALBUM, pdf.getAlbum());
                initialValues.put(KEY_TITLE, pdf.getTitle());
                initialValues.put(KEY_AID, pdf.getAid());
                initialValues.put(KEY_CAT,pdf.getCat());
                initialValues.put(KEY_PDF, pdf.getPdf_id());
                try {
                    int id = (int) db.insertWithOnConflict(TABLE_AUDIO, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE);
                    if (id == -1) {
                        db.update(TABLE_AUDIO, initialValues, "aid = ?", new String[]{String.valueOf(pdf.getAid())});
                    }
                }catch (IllegalStateException ignored){
                }

    }

    public ArrayList<String> getAlbums(){
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            String selectQuery = "SELECT distinct album FROM " + TABLE_AUDIO;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    arrayList.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }catch (Exception ignored) {
            arrayList.add("Marasiya");
            arrayList.add("Madeh");
            arrayList.add("Noha");
            arrayList.add("Matami Noha");
            arrayList.add("Other");
            arrayList.add("Dua");
            arrayList.add("Quran");
            arrayList.add("Salaam");
            arrayList.add("Qasaid");
            arrayList.add("Naat");
            arrayList.add("Manqabat");
            arrayList.add("Mutafarrat");
        }
        return arrayList;
    }

    public Audio.AudioItem getAudioById(int id) {
        try {
            ArrayList<Audio.AudioItem> arrayList = new ArrayList<Audio.AudioItem>();
            String selectQuery = "SELECT  * FROM " + TABLE_AUDIO + " WHERE id = " + id;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Audio.AudioItem audioItem = new Audio.AudioItem();
                    audioItem.setId(Integer.parseInt(cursor.getString(0)));
                    audioItem.setTitle(cursor.getString(1));
                    audioItem.setAlbum(cursor.getString(2));
                    audioItem.setSource(cursor.getString(3));
                    audioItem.setSize(Integer.parseInt(cursor.getString(4)));
                    audioItem.setAid(Integer.parseInt(cursor.getString(5)));
                    audioItem.setCat(cursor.getString(6));
                    audioItem.setPdf_id(Integer.parseInt(cursor.getString(7)));
                    arrayList.add(audioItem);
                } while (cursor.moveToNext());
            }
            return arrayList.get(0);
        }catch (IndexOutOfBoundsException ignored){
            return null;
        }
    }
}
