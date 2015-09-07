package me.cristiangomez.placefinder.ui.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import java.util.List;

import me.cristiangomez.placefinder.R;
import me.cristiangomez.placefinder.model.Place;
import me.cristiangomez.placefinder.ui.adapter.PlacesAdapter;
import me.cristiangomez.placefinder.ui.widgets.DividerItemDecoration;

/**
 * Created by cristianjgomez on 9/5/15.
 */
public class PlacesListFragment extends BaseFragment {
    //region Variables
    private PlacesAdapter mPlacesAdapter;
    private PlaceListFragmentCallbacks mCallbacks;
    private RecyclerView mRecyclerView;
    //endregion

    //region Overridden methods
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (PlaceListFragmentCallbacks) context;
        } catch (ClassCastException e) {
            Logger.e(
                    context.getClass().getCanonicalName() + " Should implement" +
                            PlaceListFragmentCallbacks.class.getCanonicalName()
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_places_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_places);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPlacesAdapter = new PlacesAdapter(new AdapterEventDelegator());
        mRecyclerView.setAdapter(mPlacesAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //endregion

    //region Methods
    public void loadNewPlaces(List<Place> places) {
        if (mPlacesAdapter != null) {
            mPlacesAdapter.clearPlaces();
            mPlacesAdapter.addPlace(places);
        }
    }
    //endregion

    //region Inner classes

    public class AdapterEventDelegator implements PlacesAdapter.PlaceAdapterCallbacks {
        public void onItemTouch(int position) {
            Place place = mPlacesAdapter.getPlace(position);
            mCallbacks.onPlaceTouched(place);
        }
    }

    //endregion

    //region Interfaces
    public interface PlaceListFragmentCallbacks {
        void onPlaceTouched(Place place);
    }
    //endregion
}
