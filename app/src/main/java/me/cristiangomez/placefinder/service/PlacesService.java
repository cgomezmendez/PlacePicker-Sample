package me.cristiangomez.placefinder.service;

import me.cristiangomez.placefinder.model.PlaceResponse;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class PlacesService {
    //region Constants
    public static final String API_URL = "https://maps.googleapis.com";
    //endregion

    //region Constructors
    private PlacesService() {

    }
    //endregion

    //region Interfaces

    /**
     * Defines
     */
    public interface GooglePlaces {
        @GET("/maps/api/place/nearbysearch/json")
        Call<PlaceResponse> getPlaceByLocationRequest(
                @Query("location") String location,
                @Query("radius") int radius,
                @Query("key") String key,
                @Query("language") String language
        );
    }
    //endregion

    //region Getters and setters
    public static GooglePlaces getService() {
        Retrofit retrofit =  new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_URL)
                .build();
        return retrofit.create(GooglePlaces.class);
    }
    //endregion
}
