/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.service;

import io.github.data4all.handler.DataBaseHandler;
import io.github.data4all.logger.Log;
import io.github.data4all.model.data.Track;
import io.github.data4all.model.data.TrackPoint;
import io.github.data4all.util.Optimizer;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

/**
 * A service for listening for location changes.
 * 
 * @author konermann
 * 
 */
public class GPSservice extends Service implements LocationListener {
    private static final String TAG = "GPSservice";

    /**
     * LocationManager.
     */
    private LocationManager lmgr;
    private WakeLock wakeLock;
    private PowerManager powerManager;

    private DataBaseHandler dbHandler;
    /*
     * the minimum of time after we get a new locationupdate in ms.
     */
    private static final long MIN_TIME = 1000;
    /*
     * the minimum of Distance after we get a new locationupdate.
     */
    private static final float MIN_DISTANCE = 0;

    private Track track;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        dbHandler = new DataBaseHandler(this.getApplicationContext());
        // wakelock, so the cpu is never shut down and is able to track at all
        // time
        final PowerManager powerManager =
                (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock =
                powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
        wakeLock.acquire();

        // new track is initialized and gets timestamp.
        // Does not contain any trackpoints yet
        track = new Track();
        // dbHandler.createTrack(track);

        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lmgr.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // second value is minimum of time, third value is minimum of meters
            lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME,
                    MIN_DISTANCE, this);
        }

        if (lmgr.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            lmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME, MIN_DISTANCE, this);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove registration for location updates
        lmgr.removeUpdates(this);

        wakeLock.release();

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onLocationChanged(android.location.
     * Location)
     */
    @Override
    public void onLocationChanged(Location loc) {
        Optimizer.putLoc(loc);

        final Location tp = Optimizer.currentBestLoc();

        // check if new Location is already stored
        if (this.sameTrackPoints(track.getLastTrackPoint(), tp)) {
            track.addTrackPoint(tp);
            // After ten trackpoints updateDatabase
            if ((track.getTrackPoints().size() % 10) == 0) {
                // dbHandler.updateTrack(track);
            }
        }
    }

    /**
     * Easy comparison of two TrackPoints. Only compares longitude and latitude.
     * 
     * @param point1
     *            The first trackpoint
     * @param point2
     *            The second trackpoint
     * @return true if lon and lat of the trackpoints are the same, else false
     */
    private boolean sameTrackPoints(TrackPoint point1, Location loc) {
        final TrackPoint point2 = new TrackPoint(loc);
        if (point1.getLat() == point2.getLat()
                && point1.getLon() == point2.getLon()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String,
     * int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not interested in provider status
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
        if (track == null) {
            // start new track
            track = new Track();
            // dbHandler.createTrack(track);
        } else {
            // overrides old track with null and start a new track everytime gps
            // is enabled
            track = null;
            track = new Track();
            // dbHandler.createTrack(track);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
        // Remove registration for location updates
        lmgr.removeUpdates(this);
        if (track.getTrackPoints().isEmpty()) {
            // track does not contain any trackpoints and gps is not available,
            // so clear track
            // dbHandler.deleteTrack(track);
            track = null;
        } else {
            // Track with trackpoints exist, so save it to database
            // dbHandler.updateTrack(track);
            track = null; // override current track with null
        }
        // TODO localization
        Toast.makeText(getBaseContext(),
                "Gps turned off, GPS tracking not possible ", Toast.LENGTH_LONG)
                .show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
