package com.example.samue.login;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "P2PDB";
    private static final String TABLE_NAME = "friends";
    private static final String COL1 = "id";
    private static final String COL2 = "name";

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, 1);
        //context.deleteDatabase(DB_NAME); //para borrar la base de datos si hace falta
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 + " TEXT);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTable);
        onCreate(db);
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

    public boolean addData(String item){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);

        Log.d(TAG, "addData: Adding " + item + " to " +  TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String q = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(q, null);
        return data;
    }

}
