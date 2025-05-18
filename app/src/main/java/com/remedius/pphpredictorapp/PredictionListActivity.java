package com.remedius.pphpredictorapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;

public class PredictionListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseHelper dbHelper;
    PredictionAdapter adapter;

    List<PredictionModel> predictionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        predictionList = dbHelper.getAllPredictions();

        if (predictionList.isEmpty()) {
            Toast.makeText(this, "No predictions found.", Toast.LENGTH_SHORT).show();
        }

        adapter = new PredictionAdapter(predictionList);
        recyclerView.setAdapter(adapter);

        // Export + Back buttons
        Button btnExportCSV = findViewById(R.id.btnExportCSV);
        Button btnExportPDF = findViewById(R.id.btnExportPDF);
        Button btnBack = findViewById(R.id.btnBack);

        btnExportCSV.setOnClickListener(v -> exportToCSV(predictionList));
        btnExportPDF.setOnClickListener(v -> exportToPDF(predictionList));
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(PredictionListActivity.this, MainActivity.class));
            finish();
        });
    }

    private void exportToCSV(List<PredictionModel> predictions) {
        try {
            File dir = new File(getExternalFilesDir(null), "exports");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "predictions.csv");

            FileWriter writer = new FileWriter(file);
            writer.append("ID,Age,Parity,Mode,Haemoglobin,PreviousPPH,ProlongedLabor,Result\n");

            for (PredictionModel p : predictions) {
                writer.append(String.format(Locale.getDefault(),
                        "%d,%d,%d,%d,%.1f,%d,%d,%s\n",
                        p.id, p.age, p.parity, p.mode, p.haemoglobin, p.prevPPH, p.prolongedLabor, p.result));
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "✅ CSV exported to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to export CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ExportCSV", "Error:", e);
        }
    }

    private void exportToPDF(List<PredictionModel> predictions) {
        try {
            PdfDocument pdf = new PdfDocument();
            Paint paint = new Paint();
            int pageWidth = 595;
            int pageHeight = 842;
            int lineHeight = 30;
            int y = 40;
            int pageNum = 1;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            for (int i = 0; i < predictions.size(); i++) {
                PredictionModel p = predictions.get(i);
                String line = String.format(Locale.getDefault(),
                        "ID:%d Age:%d Parity:%d Hb:%.1f Result:%s",
                        p.id, p.age, p.parity, p.haemoglobin, p.result);
                canvas.drawText(line, 40, y, paint);
                y += lineHeight;

                if (y > pageHeight - 40 || i == predictions.size() - 1) {
                    pdf.finishPage(page);
                    y = 40;
                    pageNum++;
                    if (i < predictions.size() - 1) {
                        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                        page = pdf.startPage(pageInfo);
                        canvas = page.getCanvas();
                    }
                }
            }

            File dir = new File(getExternalFilesDir(null), "exports");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "predictions.pdf");

            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            pdf.close();
            Toast.makeText(this, "✅ PDF exported to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to export PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ExportPDF", "Error:", e);
        }
    }
}
