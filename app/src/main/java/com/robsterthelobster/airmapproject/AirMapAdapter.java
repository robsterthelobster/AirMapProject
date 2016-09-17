package com.robsterthelobster.airmapproject;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusWeather;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by robin on 9/15/2016.
 */
public class AirMapAdapter extends RecyclerView.Adapter<AirMapAdapter.ViewHolder>{

    private final List<AirMapStatus> mValues;

    public AirMapAdapter(List<AirMapStatus> mValues) {
        this.mValues = mValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        int color = 0;
        String safetyText = "No data.";
        switch(holder.mItem.getAdvisoryColor()){
            case Red:
                color = ContextCompat.getColor(holder.mView.getContext(), R.color.red);
                safetyText = "Unsafe or unknown. Do not fly!";
                break;
            case Green:
                color = ContextCompat.getColor(holder.mView.getContext(), R.color.green);

                safetyText = "No known issues.";
                break;
            case Yellow:
                color = ContextCompat.getColor(holder.mView.getContext(), R.color.yellow);
                safetyText = "Action required to fly.";
                break;
        }
        holder.cardView.setBackgroundColor(color);
        holder.safetyText.setText(safetyText);

        String advisoryText = "";
        if(holder.mItem.getAdvisories().size() == 0){
            advisoryText = "No advisory nearby.";
        }else{
            for(AirMapStatusAdvisory advisory : holder.mItem.getAdvisories()){
                advisoryText += "\n"+advisory.toString();
            }
        }

        holder.advisoryText.setText(advisoryText);

        AirMapStatusWeather weather = holder.mItem.getWeather();
        String weatherString = weather.getTemperature() + " degrees, " + weather.getCondition();
        holder.weatherText.setText(weatherString);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @BindView(R.id.safety_text) TextView safetyText;
        @BindView(R.id.advisory_text) TextView advisoryText;
        @BindView(R.id.weather_text) TextView weatherText;
        @BindView(R.id.card_view) CardView cardView;

        public final View mView;
        public AirMapStatus mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}
