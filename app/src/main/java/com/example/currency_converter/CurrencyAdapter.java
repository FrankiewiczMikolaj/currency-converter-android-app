package com.example.currency_converter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends ArrayAdapter<Currency> {
    private Context mContext;
    private List<Currency> mCurrencyList;
    private List<Currency> originalCurrencyList;

    public CurrencyAdapter(Context context, List<Currency> currencyList) {
        super(context, 0, currencyList);
        mContext = context;
        mCurrencyList = currencyList;
        originalCurrencyList = new ArrayList<>(currencyList);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_layout, parent, false);
        }

        ImageView flagImageView = convertView.findViewById(R.id.flag_image_view);
        TextView currencyTextView = convertView.findViewById(R.id.currency_text_view);

        Currency currency = mCurrencyList.get(position);

        flagImageView.setImageResource(currency.getCurrencyFlagDrawableId());
        currencyTextView.setText(currency.getCurrencyCode());

        return convertView;
    }
    @NonNull
    @Override
    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Currency> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalCurrencyList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Currency currency : originalCurrencyList) {
                        if (currency.getCurrencyCode().toLowerCase().contains(filterPattern)) {
                            filteredList.add(currency);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mCurrencyList.clear();
                mCurrencyList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }
}