package me.cristiangomez.placefinder.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.cristiangomez.placefinder.BR;
import me.cristiangomez.placefinder.R;
import me.cristiangomez.placefinder.model.Place;

/**
 * Created by cristianjgomez on 9/5/15.
 */
public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesAdapterViewHolder> {
    private List<Place> mPlaces = new ArrayList<>();
    private static PlaceAdapterCallbacks mCallbacks;

    public PlacesAdapter(PlaceAdapterCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    @Override
    public PlacesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent,
                false);
        PlacesAdapterViewHolder holder = new PlacesAdapterViewHolder(view);
        return holder;
    }

    public void addPlace(Place place) {
        mPlaces.add(place);
        notifyItemInserted(mPlaces.size()-1);
    }

    public Place getPlace(int position) {
        return mPlaces.get(position);
    }

    public void addPlace(List<Place> places) {
        int start = mPlaces.size() -1;
        mPlaces.addAll(places);
        notifyItemRangeInserted(start, places.size()-1);
    }

    public void clearPlaces() {
        mPlaces.clear();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(PlacesAdapterViewHolder holder, int position) {
        final Place place = mPlaces.get(position);
        holder.getBinding().setVariable(BR.place, place);
        holder.getBinding().executePendingBindings();
        holder.position = position;
    }

    public static class PlacesAdapterViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        ViewDataBinding binding;
        int position;
        public PlacesAdapterViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCallbacks != null) {
                mCallbacks.onItemTouch(position);
            }
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    public interface PlaceAdapterCallbacks {
        void onItemTouch(int position);
    }
}
