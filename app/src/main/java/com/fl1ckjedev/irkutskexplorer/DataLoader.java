package com.fl1ckjedev.irkutskexplorer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.RawRes;
import androidx.lifecycle.LifecycleObserver;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DataLoader {

    //ApplicationContext.
    private final Context context;

    public DataLoader(Context context) {
        this.context = context;
    }

    public ArrayList<PlaceMarker> getPlaceMarkersFromJSON() {
        ArrayList<PlaceMarker> placeMarkers = new ArrayList<>();
        try {
            String jsonInfoString = readJSONDataFromRaw(R.raw.places_info);
            String jsonCoordinatesString = readJSONDataFromRaw(R.raw.places_coordinates);
            JSONArray jsonInfoArray = new JSONArray(jsonInfoString);
            JSONArray jsonLatLngArray = new JSONArray(jsonCoordinatesString);
            placeMarkers.ensureCapacity(jsonInfoArray.length());
            for (int i = 0; i < jsonInfoArray.length(); ++i) {
                JSONObject infoObject = jsonInfoArray.getJSONObject(i);
                JSONObject latLngObject = jsonLatLngArray.getJSONObject(i);
                int id = infoObject.getInt("id");
                String name = infoObject.getString("name");
                double latitude = latLngObject.getDouble("latitude");
                double longitude = latLngObject.getDouble("longitude");
                LatLng latLng = new LatLng(latitude, longitude);
                PlaceMarker placeMarker = new PlaceMarker(id, name, latLng);
                placeMarkers.add(i, placeMarker);
            }
            return placeMarkers;
        } catch (JSONException | IOException exception) {
            Log.e(context.toString(), "getPlaceMarkers: ", exception);
        }
        return null;
    }

    public ArrayList<PlaceInfo> getPlacesInfoFromJSON() {
        ArrayList<PlaceInfo> placeInfos = new ArrayList<>();
        try {
            String jsonInfoString = readJSONDataFromRaw(R.raw.places_info);
            String jsonLatLngString = readJSONDataFromRaw(R.raw.places_coordinates);
            JSONArray jsonInfoArray = new JSONArray(jsonInfoString);
            JSONArray jsonCoordinatesArray = new JSONArray(jsonLatLngString);
            placeInfos.ensureCapacity(jsonInfoArray.length());
            for (int i = 0; i < jsonInfoArray.length(); ++i) {
                JSONObject infoObject = jsonInfoArray.getJSONObject(i);
                JSONObject latLngObject = jsonCoordinatesArray.getJSONObject(i);
                int id = infoObject.getInt("id");
                String name = infoObject.getString("name");
                String type = infoObject.getString("type");
                String address = infoObject.getString("address");
                String description = infoObject.getString("description");
                double latitude = latLngObject.getDouble("latitude");
                double longitude = latLngObject.getDouble("longitude");
                LatLng latLng = new LatLng(latitude, longitude);
                PlaceInfo placeInfo = new PlaceInfo(id, name, type, address, description, latLng);
                placeInfos.add(i, placeInfo);
            }
            return placeInfos;
        } catch (JSONException | IOException exception) {
            Log.e(context.toString(), "getPlacesInfo: ", exception);
        }
        return null;
    }

    public ArrayList<String> getPlaceImagesURLs(int id) {
        ArrayList<String> placeImagesURLs = new ArrayList<>();
        try {
            String jsonURLsString = readJSONDataFromRaw(R.raw.places_images_urls);
            JSONArray jsonURLsArray = new JSONArray(jsonURLsString);
            placeImagesURLs.ensureCapacity(jsonURLsArray.length());
            JSONArray URLArray = jsonURLsArray.getJSONObject(id).getJSONArray("url");
            for (int i = 0; i < URLArray.length(); ++i) {
                String url = URLArray.getString(i);
                placeImagesURLs.add(i, url);
            }
            return placeImagesURLs;
        } catch (JSONException | IOException exception) {
            Log.e(context.toString(), "getPlaceImagesURLs: ", exception);
        }
        return null;
    }

    /**
     * Reads JSON data from raw and returns it as String.
     *
     * @param rawResId raw resource id.
     * @return json string content.
     * @throws IOException input/output exception.
     */
    private String readJSONDataFromRaw(@RawRes int rawResId) throws IOException {
        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();
        try {
            String jsonString;
            inputStream = context.getResources().openRawResource(rawResId);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((jsonString = bufferedReader.readLine()) != null) {
                builder.append(jsonString);
            }
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
        return new String(builder);
    }
}
