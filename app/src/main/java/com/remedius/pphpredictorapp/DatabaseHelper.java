package com.remedius.pphpredictorapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "PPHPredictorApp-DB";
    private static final String DATABASE_NAME = "pph_predictions.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "predictions";
    public static final String COL_ID = "id";
    public static final String COL_AGE = "age";
    public static final String COL_PARITY = "parity";
    public static final String COL_MODE = "mode";
    public static final String COL_HB = "haemoglobin";
    public static final String COL_PREV_PPH = "previous_pph";
    public static final String COL_PROLONGED = "prolonged_labor";
    public static final String COL_RESULT = "risk_result";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "✅ Table created: " + TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertPrediction(int age, int parity, int mode, float hb, int prevPPH, int prolonged, String result) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AGE, age);
        values.put(COL_PARITY, parity);
        values.put(COL_MODE, mode);
        values.put(COL_HB, hb);
        values.put(COL_PREV_PPH, prevPPH);
        values.put(COL_PROLONGED, prolonged);
        values.put(COL_RESULT, result);

        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) {
            Log.d(TAG, "✅ Prediction inserted successfully with ID: " + id);
        } else {
            Log.e(TAG, "❌ Failed to insert prediction.");
        }
    }

    public List<PredictionModel> getAllPredictions() {
        List<PredictionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

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

                list.add(new PredictionModel(id, age, parity, mode, hb, prevPPH, prolonged, result));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}
