package com.example.e_commerceproject.Map;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.widget.Toast;
import androidx.annotation.NonNull;
public class myLocationListener implements LocationListener{
    Context activityContext;
    public myLocationListener(Context context) {
        activityContext = context;
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(activityContext, location.getLatitude()+" "+location.getLatitude(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(activityContext, "GPS provider is Enabled.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(activityContext, "GPS provider is Disabled. Please, Enable GPS.", Toast.LENGTH_LONG).show();
    }
}