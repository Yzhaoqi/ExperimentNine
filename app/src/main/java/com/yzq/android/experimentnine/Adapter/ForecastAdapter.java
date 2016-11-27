package com.yzq.android.experimentnine.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yzq.android.experimentnine.Compoment.Forecast;
import com.yzq.android.experimentnine.R;

import java.util.List;

/**
 * Created by YZQ on 2016/11/27.
 */

public class ForecastAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Forecast> forecastList;

    public ForecastAdapter(Context context, List<Forecast> forecastList) {
        this.context = context;
        this.forecastList = forecastList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.forecast_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.date = (TextView) view.findViewById(R.id.forecast_date);
        viewHolder.situation = (TextView) view.findViewById(R.id.forecast_situation);
        viewHolder.temperate = (TextView) view.findViewById(R.id.forecast_temperate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.date.setText(forecastList.get(position).getDate());
        viewHolder.situation.setText(forecastList.get(position).getType());
        String temp = forecastList.get(position).getLow() + "/" + forecastList.get(position).getHigh();
        viewHolder.temperate.setText(temp);
    }

    @Override
    public int getItemCount() {
        if (forecastList == null) return 0;
        return forecastList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public TextView date, situation, temperate;
    }
}
