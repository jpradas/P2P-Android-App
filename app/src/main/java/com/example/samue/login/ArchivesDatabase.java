package com.example.samue.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ArchivesDatabase extends SQLiteOpenHelper{

    private static final String TAG = "ArchivesDatabase";
    private static final String DB_NAME = "Archives";
    private static final String TABLE_NAME = "SharedArchives";
    private static final String COL1= "name";
    private static final String COL2 = "path";



    public ArchivesDatabase(Context context){
            super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL1 + " TEXT, " + COL2 + " TEXT);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTable);
        onCreate(db);
    }

    public boolean addData(String name, String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, name);
        contentValues.put(COL2, path);

        Log.d(TAG, "addData: Adding " + name + " with path " + path + "to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public boolean removeData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[] {name};

        long result = db.delete(TABLE_NAME,  "name=?", args);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getData(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] campos = new String[] {COL1, COL2};
        String[] args = new String[] {name};

        Cursor data = db.query(TABLE_NAME, campos, "name=?", args, null, null, null);
        return data;
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(q, null);
        return data;
    }

    public boolean exists(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        //String q = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1 + "=" + name;
        String[] campos = new String[] {COL1, COL2};
        String[] args = new String[] {name};


        Cursor c = db.query(TABLE_NAME, campos, "name=?", args, null, null, null);

        if(c.getCount() != 0){
            return true;
        }else{
            return false;
        }
    }
}

