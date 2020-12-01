package com.fl1ckjedev.irkutskexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // The navigation drawer layout.
    private DrawerLayout mDrawerLayout;

    // The string tag of activity.
    private static final String TAG = MainActivity.class.getName();

    // The google maps object.
    private GoogleMap googleMap;

    // Client of a fused location provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Location callback.
    //private LocationCallback locationCallback;

    // The geolocation where the device is currently located.
    // That is the last-known location retrieved by the fused location provider.
    private Location location;

    private LocationRequest locationRequest;

    private boolean requestingLocationUpdates;

    // The state of location permission grant.
    private boolean locationPermissionGranted;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "request_location_updates";

    // A default location and default zoom to use when location permission is not granted.
    private final LatLng defaultLocation = new LatLng(52.285165, 104.288647);
    private static final int DEFAULT_ZOOM = 12;

    // A zoom value to use when finding users location.
    private static final int ZOOM_MY_LOCATION = 17;

    // Min and max zoom values.
    private static final float MIN_ZOOM = 10f, MAX_ZOOM = 18f;

    // Fine location request code.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CODE = 1;

    // Location settings request code.
    //public static final int LOCATION_SETTINGS_REQUEST = 1;

    // Application preferences file name.
    public static final String APP_PREFERENCES = "settings";

    // Preferences keys.
    public static final String APP_PREFERENCES_MARKERS_VISIBILITY = "markers-visibility";

    // Markers visibility code.
    private int VISIBILITY_CODE;

    // Shared preferences instance
    private SharedPreferences sharedPreferences;

    // Places Markers list.
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<PlaceMarker> placeMarkers;

    private FloatingActionButton markersVisibilityFab,
            markersAllVisibleFab, markersNoVisibleFab, markersOnlyFavVisibleFab;

    // Text views as fab menu item labels.
    private TextView markersAllVisibleFabLabel, markersNoVisibleFabLabel, markersOnlyFavVisibleFabLabel;

    // Floating action buttons animations.
    private Animation fabOpenAnim, fabCloseAnim, fabClockAnim, fabAntiClockAnim;

    // Flag to state if floating buttons menu opened.
    private boolean fabMenuIsOpened;

    /**
     * Called firstly when intent is made.
     *
     * @param savedInstanceState bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context appContext = getApplicationContext();
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            // Last know user's location.
            location = savedInstanceState.getParcelable(KEY_LOCATION);
            requestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            // The google maps camera position.
            savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_nav_maps);

        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the floating action buttons and their labels.
        // Floating action buttons.
        FloatingActionButton myLocationFab = findViewById(R.id.my_location_fab);
        FloatingActionButton zoomInFab = findViewById(R.id.zoom_in_fab);
        FloatingActionButton zoomOutFab = findViewById(R.id.zoom_out_fab);
        markersVisibilityFab = findViewById(R.id.markers_visibility_fab);
        markersAllVisibleFab = findViewById(R.id.markers_visibility_all_visible_fab);
        markersNoVisibleFab = findViewById(R.id.markers_visibility_no_visible_fab);
        markersOnlyFavVisibleFab = findViewById(R.id.markers_visibility_only_favorite_fab);
        markersAllVisibleFabLabel = findViewById(R.id.fab_label_all_markers);
        markersNoVisibleFabLabel = findViewById(R.id.fab_label_no_markers);
        markersOnlyFavVisibleFabLabel = findViewById(R.id.fab_label_only_favorite_markers);
        myLocationFab.setOnClickListener(this);
        zoomInFab.setOnClickListener(this);
        zoomOutFab.setOnClickListener(this);
        markersVisibilityFab.setOnClickListener(this);
        markersAllVisibleFab.setOnClickListener(this);
        markersNoVisibleFab.setOnClickListener(this);
        markersOnlyFavVisibleFab.setOnClickListener(this);

        //Set up animations for floating action buttons.
        fabCloseAnim = AnimationUtils.loadAnimation(appContext, R.anim.fab_close);
        fabOpenAnim = AnimationUtils.loadAnimation(appContext, R.anim.fab_open);
        fabClockAnim = AnimationUtils.loadAnimation(appContext, R.anim.fab_rotate_clock);
        fabAntiClockAnim = AnimationUtils.loadAnimation(appContext, R.anim.fab_rotate_anticlock);

        // Set up the navigation drawer.
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Construct a fused location provider client.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Get google maps fragment.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (savedInstanceState == null)
        // First incarnation of this activity.
        {
            if (supportMapFragment != null)
                supportMapFragment.setRetainInstance(true);
        }
        // Get map asynchronously.
        if (supportMapFragment != null)
            supportMapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     *
     * @param outState bundle.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (googleMap != null) {
            outState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates);
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, location);
        }
        super.onSaveInstanceState(outState);
    }

    private void syncMapTheme() {
        // Detect current theme (light or dark).
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean success = false;
        try {
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    // Night mode is not active, we're using the light theme
                    success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    // Night mode is active, we're using dark theme
                    success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark));
                    break;
            }
            if (!success) Log.d(TAG, "Style parsing error!");
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(APP_PREFERENCES_MARKERS_VISIBILITY, VISIBILITY_CODE);
        editor.apply();
        if (sharedPreferences.contains(APP_PREFERENCES_MARKERS_VISIBILITY)) {
            VISIBILITY_CODE = sharedPreferences.getInt(APP_PREFERENCES_MARKERS_VISIBILITY, 0);
            setMarkersVisible(VISIBILITY_CODE);
        }
        setMarkersVisible(1);// A HUGE HACK.
        recreate();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     *
     * @param googleMap google maps object instance.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get google map object reference.
        this.googleMap = googleMap;

        // Set the boundaries so the user can't move the camera outside of specific area.
        LatLngBounds irkutskBounds = new LatLngBounds(
                new LatLng(52.203131, 104.094384), // SW bounds
                new LatLng(52.404666, 104.459680)  // NE bounds
        );
        googleMap.setLatLngBoundsForCameraTarget(irkutskBounds);

        // Set the theme according to night mode state.
        syncMapTheme();

        // Add markers to the map.
        AddMarkersToMap();

        // Set markers visibility according to shared preferences value.
        setMarkersVisible(VISIBILITY_CODE);

        // Set up the camera.
        setMapCameraToDefault();

        // Prompt the user to enable location.
        enableDeviceLocation();

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation(false);
    }

    //TODO: CUSTOM MARKER ICONS!!!
//    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
//        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
//        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
//                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        DrawableCompat.setTint(vectorDrawable, color);
//        vectorDrawable.draw(canvas);
//        return BitmapDescriptorFactory.fromBitmap(bitmap);
//    }

    private void AddMarkersToMap() {
        // Getting place markers data from local JSON.
        // Data loader object, which provides place markers data from local JSON file.
        DataLoader dataLoader = new DataLoader(getApplicationContext());
        placeMarkers = dataLoader.getPlaceMarkersFromJSON();

        // Initializing list of marker options.
        for (int i = 0; i < placeMarkers.size(); i++) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                            .draggable(false)
                            .flat(true)
                            .position(placeMarkers.get(i).getLatLng())
                            .title(placeMarkers.get(i).getName())
//                    .icon((vectorToBitmap(R.drawable.ic_nav_bar_places,
////                            Color.parseColor("#A4C639"))))
//                            Color.RED)))
            );
            markers.add(i, marker);
        }
    }

    /**
     * Changes visibility of markers.
     *
     * @param VISIBILITY_CODE code.
     */
    private void setMarkersVisible(int VISIBILITY_CODE) {
        for (Marker marker : markers)
            switch (VISIBILITY_CODE) {
                case 0:
                    markersAllVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.teal_300), android.graphics.PorterDuff.Mode.SRC_IN);
                    markersNoVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                    markersOnlyFavVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                    marker.setVisible(true);
                    break;
                case 1:
                    markersAllVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                    markersNoVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.teal_300), android.graphics.PorterDuff.Mode.SRC_IN);
                    markersOnlyFavVisibleFab.setColorFilter(ContextCompat.getColor(this,
                            R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                    marker.setVisible(false);
                    break;
            }
    }

    //TODO: ADD DIRECTION FINDER
//    private void trackMovement() {
//        requestingLocationUpdates = true;
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    // Update UI with location data
//                    // ...
//                }
//            }
//        };
//    }

    /**
     * Handles buttons click event.
     *
     * @param view button view.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.my_location_fab:
                // Check if GPS is enabled and get device location
                // or make an intent to location settings.
                if (isLocationEnabled(getApplicationContext())) {
                    getDeviceLocation(true);
                } else {
                    enableDeviceLocation();
                    //askUserToTurnOnLocation();
                }
                break;
            case R.id.zoom_in_fab:
                // Zoom in camera.
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.zoom_out_fab:
                // Zoom out camera.
                googleMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.markers_visibility_fab:
                if (fabMenuIsOpened) {
                    CloseFabMenu(markersAllVisibleFab,
                            markersNoVisibleFab,
                            markersOnlyFavVisibleFab);
                } else {
                    OpenFabMenu(markersAllVisibleFab,
                            markersNoVisibleFab,
                            markersOnlyFavVisibleFab);
                }
                break;
            case R.id.markers_visibility_all_visible_fab:
                Log.e(TAG, "ALL VISIBLE");
                VISIBILITY_CODE = 0;
                setMarkersVisible(VISIBILITY_CODE);
                break;
            case R.id.markers_visibility_no_visible_fab:
                Log.e(TAG, "NO VISIBLE");
                VISIBILITY_CODE = 1;
                setMarkersVisible(VISIBILITY_CODE);
                break;
            case R.id.markers_visibility_only_favorite_fab:
                break;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

//    private void startLocationUpdates() {
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
//                    locationCallback,
//                    Looper.getMainLooper());
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CODE);
//        }
//    }
//
//    private void stopLocationUpdates() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location updates if activity is paused.
//        if (requestingLocationUpdates)
//            stopLocationUpdates();

        // Save shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(APP_PREFERENCES_MARKERS_VISIBILITY, VISIBILITY_CODE);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Request location updates.
//        if (requestingLocationUpdates) {
//            startLocationUpdates();
//        }

        // Get shared preferences.
        if (sharedPreferences.contains(APP_PREFERENCES_MARKERS_VISIBILITY)) {
            VISIBILITY_CODE = sharedPreferences.getInt(APP_PREFERENCES_MARKERS_VISIBILITY, 0);
            setMarkersVisible(VISIBILITY_CODE);
        }
    }

    /**
     * Closes fab menu which is above map fragment.
     *
     * @param menuItems menu fab items.
     */
    private void CloseFabMenu(FloatingActionButton... menuItems) {
        markersAllVisibleFabLabel.startAnimation(fabCloseAnim);
        markersNoVisibleFabLabel.startAnimation(fabCloseAnim);
        markersOnlyFavVisibleFabLabel.startAnimation(fabCloseAnim);
        markersVisibilityFab.startAnimation(fabAntiClockAnim);
        for (FloatingActionButton fab : menuItems) {
            fab.startAnimation(fabCloseAnim);
            fab.setClickable(false);
            fabMenuIsOpened = false;
        }
    }

    /**
     * Opens fab menu which is above map fragment.
     *
     * @param menuItems menu fab items.
     */
    private void OpenFabMenu(FloatingActionButton... menuItems) {
        markersAllVisibleFabLabel.startAnimation(fabOpenAnim);
        markersNoVisibleFabLabel.startAnimation(fabOpenAnim);
        markersOnlyFavVisibleFabLabel.startAnimation(fabOpenAnim);
        markersVisibilityFab.startAnimation(fabClockAnim);
        for (FloatingActionButton fab : menuItems) {
            fab.startAnimation(fabOpenAnim);
            fab.setClickable(true);
            fabMenuIsOpened = true;
        }
    }

//    public void askUserToTurnOnLocation() {
//        DialogFragment newFragment = new LocationDialogFragment();
//        newFragment.show(getSupportFragmentManager(), "Location enable dialog");
//    }

    /**
     * Handles changing location settings.
     */
    private void enableDeviceLocation() {
        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> result = client.checkLocationSettings(builder.build());
        result.addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    MainActivity.this,
                                    LocationRequest.PRIORITY_HIGH_ACCURACY);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    /**
     * Handles main activity results with request and result codes.
     *
     * @param requestCode request code.
     * @param resultCode  result code.
     * @param data        intent data.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            switch (resultCode) {
                case MainActivity.RESULT_OK:
                    // All required changes were successfully made.
                    break;
                case MainActivity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to.
                    setMapCameraToDefault();
                    requestingLocationUpdates = false;
                    break;
                case 3:
                    onConfigurationChanged(getResources().getConfiguration());
                    break;
                default:
                    break;
            }
//        } else if (requestCode == LOCATION_SETTINGS_REQUEST) {
//            if (isLocationEnabled(getApplicationContext())) {
//                enableDeviceLocation();
//                getDeviceLocation(false);
//                requestingLocationUpdates = true;
//            }
        }
    }

    /**
     * Checks if location is enabled on device.
     *
     * @param context context.
     * @return location mode enabled state (true or false).
     */
    @NonNull
    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     * Otherwise uses default ones.
     */
    private void getDeviceLocation(boolean animate) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        location = task.getResult();
                        if (location != null) {
                            if (animate) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(location.getLatitude(), location.getLongitude()),
                                        ZOOM_MY_LOCATION));
                            } else {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(location.getLatitude(), location.getLongitude()),
                                        ZOOM_MY_LOCATION));
                            }
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        setMapCameraToDefault();
                    }
                });
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                setMapCameraToDefault();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Sets camera to the default position with default zoom value.
     */
    private void setMapCameraToDefault() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                defaultLocation, DEFAULT_ZOOM));
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CODE);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     *
     * @param requestCode  request code.
     * @param permissions  permissions.
     * @param grantResults granted results array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CODE) {
            //If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation(false);
            }
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                location = null;
                getLocationPermission();
            }
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            googleMap.setMinZoomPreference(MIN_ZOOM);
            googleMap.setMaxZoomPreference(MAX_ZOOM);
            googleMap.setIndoorEnabled(false);
            googleMap.setTrafficEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Handles navigation drawer menu item selection.
     *
     * @param item selected navigation menu item
     * @return true.
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_city_info:
                break;
            case R.id.nav_places:
                startActivity(new Intent(MainActivity.this, PlaceListActivity.class));
                break;
            case R.id.nav_favourites:
                //code()
                break;
//            case R.id.nav_settings:
//                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//                break;
            case R.id.nav_about:
                //code2333()
                //ok
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handles back button press event.
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            //Close the navigation drawer if it's opened.
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //Otherwise behave like default.
            super.onBackPressed();
        }
    }
}