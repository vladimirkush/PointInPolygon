package BL;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

public class Checker {
    private static final String TAG = "MAIN_ACT";
    private double              xMin;
    private double              xMax;
    private double              yMin;
    private double              yMax;
    private ArrayList<LatLng>   polygonCoords;

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
            if (((polygonCoords.get(i).longitude > location.longitude) != (polygonCoords.get(j).longitude > location.longitude)) &&     //this row checks if a point in boundaries of a line
                    (location.latitude < (polygonCoords.get(j).latitude - polygonCoords.get(i).latitude) * (location.longitude - polygonCoords.get(i).longitude) / (polygonCoords.get(j).longitude - polygonCoords.get(i).longitude) + polygonCoords.get(i).latitude))
                inside = !inside;
        }
        Log.d(TAG, "isInside: "+ inside);
        return inside;
    }

    /* fast check if the location is outside the boundaries of a polygon to improve performance */
    private boolean isOutsideOfBoundaries(LatLng loc){
        if (loc.longitude < xMin || loc.longitude > xMax || loc.latitude < yMin || loc.latitude > yMax )
            return true;
        return false;
    }


    /*
       Algorithm to find the nearest point of a polygon's side from a test LatLng position
       Taken from here: http://stackoverflow.com/questions/36104809/find-the-closest-point-on-polygon-to-user-location
    */
    public LatLng findNearestPoint(LatLng test) {
        double distance = -1;
        LatLng minimumDistancePoint = test;

        if (test == null || polygonCoords == null) {
            return minimumDistancePoint;
        }

        for (int i = 0; i < polygonCoords.size(); i++) {
            LatLng point = polygonCoords.get(i);

            int segmentPoint = i + 1;
            if (segmentPoint >= polygonCoords.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(test, point, polygonCoords.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(test, point, polygonCoords.get(segmentPoint));
            }
        }

        return minimumDistancePoint;
    }

    /*
        Based on `distanceToLine` method from
        https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     */
    private LatLng findNearestPoint(final LatLng p, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(p.latitude);
        final double s0lng = Math.toRadians(p.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));


    }



}
