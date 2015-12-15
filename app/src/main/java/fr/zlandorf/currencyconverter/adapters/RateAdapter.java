package fr.zlandorf.currencyconverter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import fr.zlandorf.currencyconverter.R;
import fr.zlandorf.currencyconverter.models.entities.Rate;

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

        TextView pairText = (TextView) convertView.findViewById(R.id.rate_pair_name);
        TextView valueText = (TextView) convertView.findViewById(R.id.rate_value);

        pairText.setText(String.format("%s/%s :", rate.getPair().getFrom(), rate.getPair().getTo()));
        valueText.setText(String.valueOf(rate.getValue()));

        return convertView;
    }
}
