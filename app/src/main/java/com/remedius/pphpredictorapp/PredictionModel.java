package com.remedius.pphpredictorapp;

/**
 * Model class representing a single prediction record.
 * Each instance holds the input values used for prediction
 * and the resulting classification (e.g., "High Risk of PPH").
 */
public class PredictionModel {

    // Unique ID for the prediction record (from SQLite DB)
    public int id;

    // Patient's age (in years)
    public int age;

    // parity
    public int parity;

    // Mode of delivery (vaginal = 0, cesarean = 1)
    public int mode;

    // Haemoglobin level (g/dL)
    public float haemoglobin;

    // Whether patient had a previous history of PPH (1 = yes, 0 = no)
    public int prevPPH;

    // Whether labor was prolonged (1 = yes, 0 = no)
    public int prolongedLabor;

    // Final prediction result ("High Risk of PPH" or "Low Risk of PPH")
    public String result;

    /**
     * Constructor to initialize all fields.
     *
     * @param id              Unique record ID
     * @param age             Patient's age
     * @param parity          Number of previous births
     * @param mode            Mode of delivery
     * @param haemoglobin     Haemoglobin level
     * @param prevPPH         Previous PPH history
     * @param prolongedLabor  Whether labor was prolonged
     * @param result          Prediction result
     */
    public PredictionModel(int id, int age, int parity, int mode, float haemoglobin, int prevPPH, int prolongedLabor, String result) {
        this.id = id;
        this.age = age;
        this.parity = parity;
        this.mode = mode;
        this.haemoglobin = haemoglobin;
        this.prevPPH = prevPPH;
        this.prolongedLabor = prolongedLabor;
        this.result = result;
    }
}
