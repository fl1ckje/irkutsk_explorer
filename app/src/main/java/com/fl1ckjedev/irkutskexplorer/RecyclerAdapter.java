package com.fl1ckjedev.irkutskexplorer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {

    @SuppressWarnings("FieldMayBeFinal")
    private List<PlaceInfo> placeInfoList;
    private final List<PlaceInfo> placeInfoAllList;

    public RecyclerAdapter(List<PlaceInfo> placeInfoList) {
        this.placeInfoList = placeInfoList;
        this.placeInfoAllList = new ArrayList<>(placeInfoList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.nameTextView.setText(placeInfoList.get(position).getName());
        holder.typeTextView.setText(placeInfoList.get(position).getType());
        holder.addressTextView.setText(placeInfoList.get(position).getAddress());
        holder.itemView.setTag(placeInfoList.get(position));
    }

    @Override
    public int getItemCount() {
        return placeInfoList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PlaceInfo> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(placeInfoAllList);
            } else {
                for (PlaceInfo placeInfo : placeInfoAllList) {
                    if (placeInfo
                            .getName()
                            .toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            placeInfo
                                    .getType()
                                    .toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            placeInfo.getAddress().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(placeInfo);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            placeInfoList.clear();
            placeInfoList.addAll((Collection<? extends PlaceInfo>) results.values);
            notifyDataSetChanged();
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView nameTextView;
        final TextView typeTextView;
        final TextView addressTextView;

        ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.list_item_name);
            typeTextView = view.findViewById(R.id.list_item_type);
            addressTextView = view.findViewById(R.id.list_item_address);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PlaceInfo item = (PlaceInfo) view.getTag();
            Context context = view.getContext();
            Intent intent = new Intent(context, PlaceDetailActivity.class);
            intent.putExtra(PlaceDetailActivity.PLACE_ID, item.getId())
                    .putExtra(PlaceDetailActivity.PLACE_NAME, item.getName())
                    .putExtra(PlaceDetailActivity.PLACE_TYPE, item.getType())
                    .putExtra(PlaceDetailActivity.PLACE_ADDRESS, item.getAddress())
                    .putExtra(PlaceDetailActivity.PLACE_DESCRIPTION, item.getDescription())
                    .putExtra(PlaceDetailActivity.PLACE_LAT_LNG, item.getLatLng());
            context.startActivity(intent);
        }
    }
}