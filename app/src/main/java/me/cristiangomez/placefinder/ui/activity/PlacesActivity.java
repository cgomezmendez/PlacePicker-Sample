package me.cristiangomez.placefinder.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Locale;

import me.cristiangomez.placefinder.R;
import me.cristiangomez.placefinder.model.Place;
import me.cristiangomez.placefinder.model.PlaceResponse;
import me.cristiangomez.placefinder.service.PlacesService;
import me.cristiangomez.placefinder.ui.activity.dialog.LocationEnableDialog;
import me.cristiangomez.placefinder.ui.activity.dialog.LocationPermissionExplanationDialog;
import me.cristiangomez.placefinder.ui.activity.fragment.PlacesListFragment;
import me.cristiangomez.placefinder.util.Util;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class PlacesActivity extends BaseActivity implements PlacesListFragment.PlaceListFragmentCallbacks,
        LocationEnableDialog.LocationEnableDialogCallbacks, LocationPermissionExplanationDialog.LocationExplanationDialogCallbacks {
    //region Constants
    private static final String FRAGMENT_TAG_PLACE_LIST = "me.cristiangomez.placefin" +
            "der.ui.activity.fragment.PlacesListFragment";
    private static final String FRAGMENT_TAG_MAP = "com.google.android.gms.maps.SupportMapFragment";
    private static final String FRAGMENT_TAG_LOCATION_EXPLANATION = "me.cristiangomez.plac" +
            "efinder.ui.activity.fragment.LocationExplanationDialog";
    private static final long LOCATION_UPDATE_INTERVAL = 1000; // 1 second
    private static final long LOCATION_FASTEST_INTERVAL = 6000; // 6 seconds
    private static final float LOCATION_SMALLEST_DISPLACEMENT = 100; // 100 meters
    private static final float PLACE_TOUCHED_ZOOM_LEVEL = 20;
    private static final float CURRENT_LOCATION_ZOOM_LEVEL = 15;
    private static final int ACTIVITY_FOR_RESULT_LOCATION_SETTINGS = 100;
    private static final int ACTIVITY_FOR_RESULT_LOCATION_PERMISSION = 200;
    //endregion

    //region Variables
    private PlacesListFragment mPlacesListFragment;
    private TextView mPermissionExplanationTv;
    private TextView mPermissionGrantBtn;
    private SupportMapFragment mMapFragment;
    private PlacesService.GooglePlaces mPlacesService;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private PlacesRequestListener mPlacesRequestListener;
    private GoogleMap mMap;
    private HashMap<String, Marker> mPlacesMarkers = new HashMap<>();
    //endregion

    //region Overriden methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlacesListFragment = (PlacesListFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_PLACE_LIST);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG_MAP);
        mPermissionExplanationTv = (TextView)
                findViewById(R.id.f_places_list_tv_permission_explanation);
        mPermissionGrantBtn = (AppCompatButton)
                findViewById(R.id.f_places_list_btn_permission_grant);
        mPermissionGrantBtn.setOnClickListener(new OnPermissionGrantedCallback());
        boolean hasPermissions = true;
        if (Build.VERSION.SDK_INT >= 23) {
            hasPermissions = checkPermissions();
        }
        if (hasPermissions) {
            initializeLocationService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Util.isLocationEnabled(this)) {
            LocationEnableDialog dialog =
                    new LocationEnableDialog();
            dialog.show(getSupportFragmentManager(), "");
        }
        getSupportFragmentManager().beginTransaction().attach(mPlacesListFragment).commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getSupportFragmentManager().beginTransaction().attach(mPlacesListFragment).commit();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(mConnectionCallbacks);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACTIVITY_FOR_RESULT_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionExplanationTv.setVisibility(View.GONE);
                    mPermissionGrantBtn.setVisibility(View.GONE);
                    initializeLocationService();
                } else {
                    mPermissionExplanationTv.setVisibility(View.VISIBLE);
                    mPermissionGrantBtn.setVisibility(View.VISIBLE);
                }
        }
    }

    @Override
    int getLayoutResource() {
        return R.layout.a_places;
    }

    @Override
    public void onLocationEnableCancel() {
        finish();
    }

    @Override
    public void onLocationEnableAccept() {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, ACTIVITY_FOR_RESULT_LOCATION_SETTINGS);
    }

    @Override
    public void onPlaceTouched(Place place) {
        LatLng latLng = new LatLng(place.getGeometry().getLocation().getLat(),
                place.getGeometry().getLocation().getLng());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                PLACE_TOUCHED_ZOOM_LEVEL);
        mMap.animateCamera(cameraUpdate);
        mPlacesMarkers.get(place.getId()).showInfoWindow();
    }

    @Override
    public void onLocationExplanationAccept() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                ACTIVITY_FOR_RESULT_LOCATION_PERMISSION);
    }

    //endregion

    //region Methods
    public void initializeLocationService() {
        mPlacesService = PlacesService.getService();
        mLocationRequest = createLocationRequest();
        mConnectionCallbacks = new ConnectionCallbacks();
        mConnectionFailedListener = new ConnectionFailedListener();
        mLocationListener = new LocationListener();
        mPlacesRequestListener = new PlacesRequestListener();
        buildGoogleApiClient();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }


    public void loadNearbyPlacesByLocation(Location location) {
        String locStr = location.getLatitude()+","+location.getLongitude();
        Call<PlaceResponse> placeRequest = mPlacesService.getPlaceByLocationRequest(
                locStr, 500, getString(R.string.google_api_key_browser),
                Locale.getDefault().getLanguage()
        );
        placeRequest.enqueue(mPlacesRequestListener);
    }


    private LocationRequest createLocationRequest() {
        LocationRequest request = new LocationRequest();
        request.setInterval(LOCATION_UPDATE_INTERVAL);
        request.setFastestInterval(LOCATION_FASTEST_INTERVAL);
        request.setSmallestDisplacement(LOCATION_SMALLEST_DISPLACEMENT);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return request;
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener
        );
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new ConnectionCallbacks())
                .addOnConnectionFailedListener(new ConnectionFailedListener())
                .addApi(LocationServices.API)
                .build();
    }

    private void loadMarker(final Marker marker, String imageUrl) {
        Glide.with(PlacesActivity.this)
                .load(imageUrl)
                .asBitmap()
                .fitCenter()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
                    }
                });
    }

    /**
     * Check if have permissions in android > 6
     * if doesn't have ask for it
     * if should show rationale show a dialog explaining why it's needed
     */

    private boolean checkPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                LocationPermissionExplanationDialog locationPermissionExplanationDialog =
                        new LocationPermissionExplanationDialog();
                locationPermissionExplanationDialog.show(getSupportFragmentManager(),
                        FRAGMENT_TAG_LOCATION_EXPLANATION);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACTIVITY_FOR_RESULT_LOCATION_PERMISSION);
            }
            return false;
        }
        return true;
    }
    //endregion

    //region Inner classes
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            switch (i) {
                case CAUSE_SERVICE_DISCONNECTED:
                    Logger.e("Google Services - Connection Lost: Services Disconected");
                    break;
                case CAUSE_NETWORK_LOST:
                    Logger.e("Google Services - Connection Lost: Network Disconnected");
            }
        }
    }

    private static class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    }

    private class LocationListener implements com.google.android.gms.location.LocationListener {
        @Override
        public void onLocationChanged(final Location location) {
            Logger.d("location changed");
            mMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    LatLng latLng = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory
                            .newLatLngZoom(latLng, CURRENT_LOCATION_ZOOM_LEVEL);
                    googleMap.moveCamera(update);
                    UiSettings uiSettings = googleMap.getUiSettings();
                    uiSettings.setMyLocationButtonEnabled(false);
                    uiSettings.setMapToolbarEnabled(false);
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    googleMap.setMyLocationEnabled(true);
                }
            });
            loadNearbyPlacesByLocation(location);
        }
    }

    private class PlacesRequestListener implements Callback<PlaceResponse> {
        @Override
        public void onResponse(final Response<PlaceResponse> response) {
            Logger.d(response.raw().request().urlString());
            Logger.d(response.body().getResults().size()+"");
            mPlacesListFragment.loadNewPlaces(response.body().getResults());
            mMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    for (final Place place: response.body().getResults()) {
                        LatLng latLng = new LatLng(place.getGeometry().getLocation().getLat(),
                                place.getGeometry().getLocation().getLng());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .draggable(false)
                                .title(place.getName());
                        Marker marker = googleMap.addMarker(markerOptions);
                        mPlacesMarkers.put(place.getId(), marker);
                        loadMarker(marker, place.getIcon());
                    }
                }
            });
        }

        @Override
        public void onFailure(Throwable t) {

        }
    }

    public class OnPermissionGrantedCallback implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            checkPermissions();
        }
    }
    //endregion

    //region Interfaces
    //endregion

    //region Getter and setters
    //endregion
}
