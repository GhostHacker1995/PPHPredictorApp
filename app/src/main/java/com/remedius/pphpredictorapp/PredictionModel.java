package com.remedius.pphpredictorapp;

public class PredictionModel {
    public int id, age, parity, mode, prevPPH, prolongedLabor;
    public float haemoglobin;
    public String result;

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
