package com.remedius.pphpredictorapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the local SQLite database that stores PPH prediction results.
 * It handles table creation, data insertion, and retrieval of past predictions.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "PPHPredictorApp-DB";

    // Database configuration
    private static final String DATABASE_NAME = "pph_predictions.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_NAME = "predictions";
    public static final String COL_ID = "id";
    public static final String COL_AGE = "age";
    public static final String COL_PARITY = "parity";
    public static final String COL_MODE = "mode";
    public static final String COL_HB = "haemoglobin";
    public static final String COL_PREV_PPH = "previous_pph";
    public static final String COL_PROLONGED = "prolonged_labor";
    public static final String COL_RESULT = "risk_result";

    // SQL query to create the predictions table
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_AGE + " INTEGER, " +
                    COL_PARITY + " INTEGER, " +
                    COL_MODE + " INTEGER, " +
                    COL_HB + " REAL, " +
                    COL_PREV_PPH + " INTEGER, " +
                    COL_PROLONGED + " INTEGER, " +
                    COL_RESULT + " TEXT" +
                    ");";

    /**
     * Constructor for initializing the database helper.
     * @param context Application context
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the predictions table is created.
     */
    @Override

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "Table created: " + TABLE_NAME);
    }

    /**
     * Called when the database version is incremented.
     * Drops the old table and recreates it (destructive upgrade).
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Inserts a new prediction into the database.
     * @param age Patient's age
     * @param parity Number of prior births
     * @param mode Mode of delivery
     * @param hb Haemoglobin level
     * @param prevPPH History of previous PPH (1 or 0)
     * @param prolonged Prolonged labor (1 or 0)
     * @param result Risk classification result (e.g., "High Risk of PPH")
     */
    public void insertPrediction(int age, int parity, int mode, float hb, int prevPPH, int prolonged, String result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Map prediction input values to corresponding column names
        values.put(COL_AGE, age);
        values.put(COL_PARITY, parity);
        values.put(COL_MODE, mode);
        values.put(COL_HB, hb);
        values.put(COL_PREV_PPH, prevPPH);
        values.put(COL_PROLONGED, prolonged);
        values.put(COL_RESULT, result);

        // Insert the record into the database
        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) {
            Log.d(TAG, "Prediction inserted successfully with ID: " + id);
        } else {
            Log.e(TAG, "Failed to insert prediction.");
        }
    }

    /**
     * Retrieves all predictions stored in the database, ordered by newest first.
     * @return A list of PredictionModel objects
     */
    public List<PredictionModel> getAllPredictions() {
        List<PredictionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all prediction records
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

        // Process each row and add to the list
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE));
                int parity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PARITY));
                int mode = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MODE));
                float hb = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_HB));
                int prevPPH = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PREV_PPH));
                int prolonged = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROLONGED));
                String result = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESULT));

                // Create a model object and add it to the list
                list.add(new PredictionModel(id, age, parity, mode, hb, prevPPH, prolonged, result));
            } while (cursor.moveToNext());
        }

        cursor.close(); // Close the cursor to avoid memory leaks
        return list;
    }
}
