package com.remedius.pphpredictorapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView.Adapter for displaying a list of PPH predictions in a scrollable list.
 * Binds each PredictionModel to a simple summary view.
 */
public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {

    // List of predictions to display
    private final List<PredictionModel> predictions;

    /**
     * Constructor to initialize the adapter with prediction data.
     * @param predictions List of PredictionModel objects to display
     */
    public PredictionAdapter(List<PredictionModel> predictions) {
        this.predictions = predictions;
    }

    /**
     * ViewHolder class that represents a single list item.
     * Holds a reference to the TextView that displays the prediction summary.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSummary;

        public ViewHolder(View view) {
            super(view);
            // Find the TextView within the item layout
            txtSummary = view.findViewById(R.id.txtSummary);
        }
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     * Inflates the layout for a single prediction row.
     */
    @NonNull
    @Override
    public PredictionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prediction, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds data from a PredictionModel to the corresponding view holder.
     * Converts raw data into a readable summary format for display.
     */
    @Override
    public void onBindViewHolder(@NonNull PredictionAdapter.ViewHolder holder, int position) {
        PredictionModel p = predictions.get(position);

        // Build a multi-line summary string for each prediction
        String display = "Age: " + p.age +
                ", Parity: " + p.parity +
                ", Mode: " + (p.mode == 1 ? "C/S" : "Vaginal") +
                "\nHb: " + p.haemoglobin +
                ", Prev PPH: " + p.prevPPH +
                ", Prolonged: " + p.prolongedLabor +
                "\nResult: " + p.result;

        // Set the formatted text to the summary TextView
        holder.txtSummary.setText(display);
    }

    /**
     * Returns the total number of items in the dataset.
     */
    @Override
    public int getItemCount() {
        return predictions.size();
    }
}
