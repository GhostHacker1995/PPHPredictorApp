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

    // UI and data components
    RecyclerView recyclerView;
    DatabaseHelper dbHelper;
    PredictionAdapter adapter;
    List<PredictionModel> predictionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_list);

        // Initialize RecyclerView for displaying predictions
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database helper and fetch all stored predictions
        dbHelper = new DatabaseHelper(this);
        predictionList = dbHelper.getAllPredictions();

        // Show message if no predictions are available
        if (predictionList.isEmpty()) {
            Toast.makeText(this, "No predictions found.", Toast.LENGTH_SHORT).show();
        }

        // Set up the adapter to bind prediction data to the RecyclerView
        adapter = new PredictionAdapter(predictionList);
        recyclerView.setAdapter(adapter);

        // Initialize export and navigation buttons
        Button btnExportCSV = findViewById(R.id.btnExportCSV);
        Button btnExportPDF = findViewById(R.id.btnExportPDF);
        Button btnBack = findViewById(R.id.btnBack);

        // Handle export actions and back navigation
        btnExportCSV.setOnClickListener(v -> exportToCSV(predictionList));
        btnExportPDF.setOnClickListener(v -> exportToPDF(predictionList));
        btnBack.setOnClickListener(v -> {
            // Go back to MainActivity and finish current screen
            startActivity(new Intent(PredictionListActivity.this, MainActivity.class));
            finish();
        });
    }
    /**
     * Exports prediction data to a CSV file.
     * The file is saved in the app's external files directory under "exports".
     */
    private void exportToCSV(List<PredictionModel> predictions) {
        try {
            // Create a directory named "exports" if it doesn't exist
            File dir = new File(getExternalFilesDir(null), "exports");
            if (!dir.exists()) dir.mkdirs();

            // Create or overwrite the CSV file
            File file = new File(dir, "predictions.csv");
            FileWriter writer = new FileWriter(file);

            // Write the CSV headers
            writer.append("ID,Age,Parity,Mode,Haemoglobin,PreviousPPH,ProlongedLabor,Result\n");

            // Write each prediction as a CSV row
            for (PredictionModel p : predictions) {
                writer.append(String.format(Locale.getDefault(),
                        "%d,%d,%d,%d,%.1f,%d,%d,%s\n",
                        p.id, p.age, p.parity, p.mode, p.haemoglobin, p.prevPPH, p.prolongedLabor, p.result));
            }

            // Finish writing and notify user
            writer.flush();
            writer.close();
            Toast.makeText(this, "CSV exported to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // Handle export errors
            Toast.makeText(this, "Failed to export CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ExportCSV", "Error:", e);
        }
    }

    /**
     * Exports prediction data to a multi-page PDF file.
     * Each prediction is written line-by-line on the canvas.
     */
    private void exportToPDF(List<PredictionModel> predictions) {
        try {
            // Initialize the PDF document and drawing tools
            PdfDocument pdf = new PdfDocument();
            Paint paint = new Paint();
            int pageWidth = 595;  // A4 width in points
            int pageHeight = 842; // A4 height in points
            int lineHeight = 30;  // Vertical spacing between lines
            int y = 40;           // Starting y position on page
            int pageNum = 1;      // Current page number

            // Start first page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            for (int i = 0; i < predictions.size(); i++) {
                PredictionModel p = predictions.get(i);

                // Format a line with key prediction details
                String line = String.format(Locale.getDefault(),
                        "ID:%d Age:%d Parity:%d Hb:%.1f Result:%s",
                        p.id, p.age, p.parity, p.haemoglobin, p.result);

                // Draw the line on the current page
                canvas.drawText(line, 40, y, paint);
                y += lineHeight;

                // If nearing the bottom or it's the last item, finish the current page
                if (y > pageHeight - 40 || i == predictions.size() - 1) {
                    pdf.finishPage(page);
                    y = 40;
                    pageNum++;

                    // If more predictions remain, start a new page
                    if (i < predictions.size() - 1) {
                        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                        page = pdf.startPage(pageInfo);
                        canvas = page.getCanvas();
                    }
                }
            }

            // Ensure export directory exists
            File dir = new File(getExternalFilesDir(null), "exports");
            if (!dir.exists()) dir.mkdirs();

            // Create the PDF file
            File file = new File(dir, "predictions.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            pdf.close();

            // Notify the user of successful export
            Toast.makeText(this, "PDF exported to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // Handle export errors
            Toast.makeText(this, "Failed to export PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ExportPDF", "Error:", e);
        }
    }
}
