package com.frozendust.zlandorf.bitcoincurrencyconverter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frozendust.zlandorf.bitcoincurrencyconverter.R;
import com.frozendust.zlandorf.bitcoincurrencyconverter.models.entities.Rate;

import java.util.List;

public class RateAdapter extends ArrayAdapter<Rate> {
    public RateAdapter(Context context, int resource, List<Rate> rates) {
        super(context, resource, rates);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Rate rate = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.rate_item, parent, false);
        }

        TextView pair = (TextView) convertView.findViewById(R.id.rate_pair_name);
        TextView value = (TextView) convertView.findViewById(R.id.rate_value);

        pair.setText(String.format("%s/%s :", rate.getFrom(), rate.getTo()));
        pair.setText(String.valueOf(rate.getValue()));

        return super.getView(position, convertView, parent);
    }
}