package tajiboi.compassapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by chavi on 9/4/16.
 */
public class CompassApplication extends Application implements BootstrapNotifier {
    private RegionBootstrap regionBootstrap;
    private boolean isInRange = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("CompassApplication", "set region");
        Region region = new Region(BuildConfig.APPLICATION_ID,
                Identifier.parse("00000f3e-0000-1000-8000-00805f9b34fb"), null, null);
                 //"E2:3E:18:DE:C5:9E");

        regionBootstrap = new RegionBootstrap(this, region);
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d("CompassApplication", "did enter region.");

        // Important:  make sure to add android:launchMode="singleInstance" in the manifest
        // to keep multiple copies of this activity from getting created if the user has
        // already manually launched the app.
        isInRange = true;
        startService(new Intent(this, CompassService.class)
                .setAction(CompassService.COMPASS_DATA_START));

    }

    @Override
    public void didExitRegion(Region region) {
        isInRange = false;
        stopService(new Intent(this, CompassService.class));
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
