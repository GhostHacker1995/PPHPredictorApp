package com.remedius.pphpredictorapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {

    private final List<PredictionModel> predictions;

    public PredictionAdapter(List<PredictionModel> predictions) {
        this.predictions = predictions;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSummary;

        public ViewHolder(View view) {
            super(view);
            txtSummary = view.findViewById(R.id.txtSummary);
        }
    }

    @NonNull
    @Override
    public PredictionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prediction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionAdapter.ViewHolder holder, int position) {
        PredictionModel p = predictions.get(position);

        String display = "Age: " + p.age +
                ", Parity: " + p.parity +
                ", Mode: " + (p.mode == 1 ? "C/S" : "Vaginal") +
                "\nHb: " + p.haemoglobin +
                ", Prev PPH: " + p.prevPPH +
                ", Prolonged: " + p.prolongedLabor +
                "\nResult: " + p.result;

        holder.txtSummary.setText(display);
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }
}
