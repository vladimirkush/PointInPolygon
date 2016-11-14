package BL;

import android.location.Location;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.vladimirkush.pointinpolygon.R;

import java.lang.ref.WeakReference;


public class CheckerTask extends AsyncTask<Checker, Void, Boolean> {

    private  final WeakReference<TextView> mDistTextViewReference;
    private  final WeakReference<ImageView> mStatusInsideImgViewReference;
    private LatLng location;
    private LatLng nearestPoint;


    public CheckerTask(TextView textViewReference, ImageView imgView, LatLng location) {
        this.mDistTextViewReference = new WeakReference<TextView>(textViewReference);
        mStatusInsideImgViewReference = new WeakReference<ImageView>(imgView);
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
        if (mDistTextViewReference != null && mStatusInsideImgViewReference != null){
            final TextView text = mDistTextViewReference.get();
            final ImageView imageView = mStatusInsideImgViewReference.get();
            if(b.booleanValue()) {
                imageView.setImageResource(R.mipmap.btn_green);
                text.setText("");
            }else{
                float[] results = new float[3];
                Location.distanceBetween(location.latitude, location.longitude, nearestPoint.latitude, nearestPoint.longitude, results);
                imageView.setImageResource(R.mipmap.btn_red);
                text.setText("Distance:\n "+ String.format("%.3f", results[0]) + "m");




            }
        }
    }
}
