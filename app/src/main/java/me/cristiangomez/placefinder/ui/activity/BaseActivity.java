package me.cristiangomez.placefinder.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import me.cristiangomez.placefinder.R;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class BaseActivity extends AppCompatActivity {
    //region Variables
    protected Toolbar mToolbar;
    //endregion

    //region Overridden methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayoutResource() != 0) {
            setContentView(getLayoutResource());
        }

        if (needToolbar()) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
        }
    }
    //endregion

    //region methods
    boolean needToolbar() {
        return true;
    }
    //endregion

    //region Getters and setters
    int getLayoutResource() {
        return 0;
    }
    //endregion
}
