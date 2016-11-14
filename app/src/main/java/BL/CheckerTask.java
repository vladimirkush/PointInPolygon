package BL;

import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;


public class CheckerTask extends AsyncTask<Checker, Void, Boolean> {

    private  final WeakReference<TextView> textViewReference;
    private LatLng location;
    private LatLng nearestPoint;


    public CheckerTask(TextView textViewReference, LatLng location) {
        this.textViewReference = new WeakReference<TextView>(textViewReference);
        this.location = location;


    }

    @Override
    protected Boolean doInBackground(Checker... checkers) {
        Checker checker = checkers[0];
        Boolean inside = checker.isInside(location);
        if(inside){

        }else{
            nearestPoint =  checker.findNearestPoint(location);
        }
        return inside;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        if (textViewReference != null){
            final TextView text = textViewReference.get();
            if(b.booleanValue()) {
                text.setText("Point inside");
            }else{
                float[] results = new float[3];
                Location.distanceBetween(location.latitude, location.longitude, nearestPoint.latitude, nearestPoint.longitude, results);
                text.setText("Point outside, dist: "+ results[0] + "m");




            }
        }
    }
}
