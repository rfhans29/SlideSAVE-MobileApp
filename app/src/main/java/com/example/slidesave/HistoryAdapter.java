package com.example.slidesave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    List<History> historyList;
    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = historyList.get(position);
        //Display landslide history information
        holder.tvLevel.setText(history.getAlert_level());
        holder.tvTimestamp.setText(history.getHist_dateTime());
        holder.tvMessage.setText(history.getAlert_message());
        holder.tvMoisture.setText("Soil Moisture : " + history.getMoist_value() + "%");
        holder.tvTilt.setText("Tilt Angle : " + history.getTilt_value() + "°");
        holder.tvAcc.setText("Ground Movement : " + history.getAcc_value() + " m/s²");
        holder.tvBarricade.setText("Barricade : " + history.getBarr_status());

        String level = history.getAlert_level().trim().toUpperCase();
        holder.tvLevel.setText(level);
        //Update status color based on alert level
        if (level.equals("WARNING")) {
            holder.tvLevel.setTextColor(holder.itemView.getResources().getColor(R.color.status_warning));
            holder.statusBar.setBackgroundColor(holder.itemView.getResources().getColor(R.color.status_warning));
        }
        else if (level.equals("DANGER")) {
            holder.tvLevel.setTextColor(holder.itemView.getResources().getColor(R.color.status_danger));
            holder.statusBar.setBackgroundColor(holder.itemView.getResources().getColor(R.color.status_danger));
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLevel;
        TextView tvMessage;
        TextView tvMoisture;
        TextView tvTilt;
        TextView tvAcc;
        TextView tvBarricade;
        TextView tvTimestamp;
        View statusBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvMessage = itemView.findViewById( R.id.tvMessage);
            tvAcc = itemView.findViewById(R.id.tvAcc);
            tvMoisture = itemView.findViewById(R.id.tvMoisture);
            tvTilt = itemView.findViewById(R.id.tvTilt);
            tvBarricade = itemView.findViewById(R.id.tvBarricade);
            tvTimestamp =itemView.findViewById(R.id.tvTimestamp);
            statusBar = itemView.findViewById(R.id.statusBar);
        }
    }
}