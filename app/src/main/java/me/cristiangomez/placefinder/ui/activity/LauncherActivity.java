package me.cristiangomez.placefinder.ui.activity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class LauncherActivity extends BaseActivity {
    //region Overridden classes
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =  new Intent(this, PlacesActivity.class);
        startActivity(intent);
    }

    @Override
    boolean needToolbar() {
        return false;
    }
    //endregion
}
