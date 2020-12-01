package com.fl1ckjedev.irkutskexplorer;

import com.google.android.gms.maps.model.LatLng;

/**
 * Base class for any child class, which represents any information about a place.
 */
public class Place {
    private final int id;
    private final String name;

    public Place(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

/**
 * Represents information about each place on the map.
 */
class PlaceMarker extends Place {

    private final LatLng latLng;

    public PlaceMarker(int id, String name, LatLng latLng) {
        super(id, name);
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}

/**
 * Represents information about each place in the list.
 */
class PlaceInfo extends PlaceMarker {

    private final String type;
    private final String address;
    private final String description;

    PlaceInfo(int id, String name, String type, String address, String description,
              LatLng latLng) {
        super(id, name, latLng);
        this.type = type;
        this.address = address;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }
}
