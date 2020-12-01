package com.fl1ckjedev.irkutskexplorer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * An activity representing a single Place detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PlaceListActivity}.
 */
public class PlaceDetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static String PLACE_ID = "place_id",
            PLACE_NAME = "place_name",
            PLACE_TYPE = "place_type",
            PLACE_ADDRESS = "place_address",
            PLACE_DESCRIPTION = "place_description",
            PLACE_LAT_LNG = "place_lat_lng";

    private PlaceInfo placeInfo;
    private ViewPagerAdapter viewPagerAdapter;
    private DataLoader dataLoader;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Get intent data.
        Bundle args = getIntent().getExtras();
        placeInfo = new PlaceInfo(args.getInt(PLACE_ID),
                args.getString(PLACE_NAME),
                args.getString(PLACE_TYPE),
                args.getString(PLACE_ADDRESS),
                args.getString(PLACE_DESCRIPTION),
                (LatLng) args.get(PLACE_LAT_LNG));

        // Set up toolbar.
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getResources()
                            .getString(R.string.title_place_detail));
                    isShow = true;
                } else if (isShow) {
                    // Careful there should a space between double quote otherwise it won't work.
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        dataLoader = new DataLoader(this);
        List<String> placeImagesURLs = dataLoader.getPlaceImagesURLs(placeInfo.getId());

        //Set up view pager and it's indicator.
        ViewPager viewPager = findViewById(R.id.image_view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, placeImagesURLs);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        // Set up floating action button.
        FloatingActionButton fab = findViewById(R.id.fab_route);
        fab.setOnClickListener(this);

        // Set up information in text views.
        TextView nameTextView = findViewById(R.id.place_name);
        nameTextView.setText(placeInfo.getName());
        TextView typeTextView = findViewById(R.id.place_type);
        typeTextView.setText(placeInfo.getType());
        TextView addressTextView = findViewById(R.id.place_address);
        addressTextView.setText(placeInfo.getAddress());
        TextView descriptionTextView = findViewById(R.id.place_description);
        descriptionTextView.setText(placeInfo.getDescription());

    }

    /**
     * Handles buttons click event.
     *
     * @param view button view.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        switch (view.getId()) {
            case R.id.fab_route:
                navigateUpTo(intent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, PlaceListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        setResult(3);
    }
}