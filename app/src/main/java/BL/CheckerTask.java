package BL;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;


public class CheckerTask extends AsyncTask<Checker, Void, Boolean> {

    private  final WeakReference<TextView> textViewReference;
    LatLng location;

    public CheckerTask(TextView textViewReference, LatLng location) {
        this.textViewReference = new WeakReference<TextView>(textViewReference);
        this.location = location;
    }

    @Override
    protected Boolean doInBackground(Checker... checkers) {
        Checker checker = checkers[0];
        return checker.isInside(location);

    }

    @Override
    protected void onPostExecute(Boolean b) {
        if (textViewReference != null){
            final TextView text = textViewReference.get();
            if(b.booleanValue()) {
                text.setText("Point inside");
            }else{
                text.setText("Point outside");
            }
        }
    }
}
