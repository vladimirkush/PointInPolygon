package BL;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Checker {
    private static final String TAG = "MAIN_ACT";
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private ArrayList<LatLng> polygonCoords;

    public Checker(ArrayList<LatLng> polygonCoords) {

        this.polygonCoords = polygonCoords;
        findBoundaries();
    }
    /* find boundaries for fast checking if the location is outside */
    private void findBoundaries(){
        xMin = 180;    //lon min -180
        xMax = -180;   //lon max 180
        yMin = 90;     //lat min -90
        yMax = -90;    //lat max 90
        for (LatLng coord:polygonCoords) {
            if(coord.longitude > xMax) xMax = coord.longitude;
            if(coord.longitude < xMin) xMin = coord.longitude;
            if(coord.latitude > yMax) yMax = coord.latitude;
            if(coord.latitude < yMin) yMin = coord.latitude;
        }
        Log.d(TAG, "xmin: "+ xMin+ " xmax: " +xMax+" ymin: "+yMin+ " ymax: "+yMax);
    }

    /*
        Algorithm uses a raycasting approach, the fastest implementation I found,
        taken from here: http://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
    */
    public boolean isInside(LatLng location) {
        
        if(isOutsideOfBoundaries(location)) {
            Log.d(TAG, "outside of boundaries");
            return false;
        }
        int i, j;
        boolean inside = false;
        for (i = 0, j = polygonCoords.size() - 1; i < polygonCoords.size(); j = i++) {
            if (((polygonCoords.get(i).longitude > location.longitude) != (polygonCoords.get(j).longitude > location.longitude)) &&
                    (location.latitude < (polygonCoords.get(j).latitude - polygonCoords.get(i).latitude) * (location.longitude - polygonCoords.get(i).longitude) / (polygonCoords.get(j).longitude - polygonCoords.get(i).longitude) + polygonCoords.get(i).latitude))
                inside = !inside;
        }
        Log.d(TAG, "isInside: "+ inside);
        return inside;
    }

    /* fast check if the location is outside to improve performance */
    private boolean isOutsideOfBoundaries(LatLng loc){
        if (loc.longitude < xMin || loc.longitude > xMax || loc.latitude < yMin || loc.latitude > yMax )
            return true;
        return false;
    }
}
