package me.cristiangomez.placefinder.model;

import java.util.List;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class PlaceResponse {
    List<Place> results;

    public List<Place> getResults() {
        return results;
    }

    public void setResults(List<Place> results) {
        this.results = results;
    }
}
