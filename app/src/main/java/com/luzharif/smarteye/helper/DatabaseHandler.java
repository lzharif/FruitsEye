package com.luzharif.smarteye.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.luzharif.smarteye.model.Shots;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuZharif on 23/04/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shotsdb";
    private static final int DATABASE_VERSION = 1;

    public static final String SHOTS_TABLE = "shots";

    public static final String ID_COLUMN = "id";
    public static final String SHOTS_NAME = "name_shot";
    public static final String SHOTS_FRUIT = "name_fruit";
    public static final String SHOTS_QUALITY = "fruit_quality";
    public static final String SHOTS_IMAGE = "image_fruit";

    public static final String CREATE_SHOTS_TABLE = "CREATE TABLE "
            + SHOTS_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY, "
            + SHOTS_NAME + " TEXT, " + SHOTS_FRUIT + " TEXT, " + SHOTS_QUALITY + " INT, "
            + SHOTS_IMAGE + " TEXT)";

//    private static DatabaseHandler instance;
//
//    public static synchronized DatabaseHandler getHelper (Context context) {
//        if (instance == null)
//            instance = new DatabaseHandler(context);
//        return instance;
//    }

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

//    @Override
//    public void onOpen(SQLiteDatabase db) {
//        super.onOpen(db);
//        if (!db.isReadOnly()) {
//            // Enable foreign key constraints
//            db.execSQL("PRAGMA foreign_keys=ON;");
//        }
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHOTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOTS_TABLE);

        // Create tables again
        onCreate(db);
    }

    // Adding new Shot
    public void addShot(Shots shots) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SHOTS_NAME, shots.getNameShot());
        values.put(SHOTS_FRUIT, shots.getNameFruit());
        values.put(SHOTS_QUALITY, shots.getFruitQuality());
        values.put(SHOTS_IMAGE, shots.getImageFruit());

        // Inserting Row
        db.insert(SHOTS_TABLE, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Shots
    public List<Shots> getAllShots() {
        List<Shots> contactList = new ArrayList<Shots>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + SHOTS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Shots shots = new Shots();
                shots.setID(Integer.parseInt(cursor.getString(0)));
                shots.setNameShot(cursor.getString(1));
                shots.setNameFruit(cursor.getString(2));
                shots.setFruitQuality(Integer.parseInt(cursor.getString(3)));
                shots.setImageFruit(cursor.getString(4));
                // Adding contact to list
                contactList.add(shots);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Updating single shots
    public int updateShots(Shots shots) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SHOTS_NAME, shots.getNameShot());
        values.put(SHOTS_FRUIT, shots.getNameFruit());
        values.put(SHOTS_QUALITY, shots.getFruitQuality());
        values.put(SHOTS_IMAGE, shots.getImageFruit());

        // updating row
        return db.update(SHOTS_TABLE, values, ID_COLUMN + " = ?",
                new String[]{String.valueOf(shots.getID())});
    }

    // Deleting single shots
    public void deleteShots(Shots shots) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SHOTS_TABLE, ID_COLUMN + " = ?",
                new String[]{String.valueOf(shots.getID())});
        db.close();
    }

    // Getting fruits Count
    public int getShotsCount() {
        int count;
        String countQuery = "SELECT  * FROM " + SHOTS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting shots Sum
    public int getShotsSum() {
        int sum = 0;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + SHOTS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int shot = Integer.parseInt(cursor.getString(1));
            sum = sum + shot;
        }
        return sum;
    }

    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SHOTS_TABLE);

        // Create tables again
        onCreate(db);
    }
}
