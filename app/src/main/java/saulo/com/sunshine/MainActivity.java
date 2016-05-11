package saulo.com.sunshine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.bumptech.glide.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmReceiver;
import com.google.android.gms.gcm.GcmListenerService;

import com.pushbots.push.Pushbots;

import java.io.IOException;
import java.util.Locale;

import saulo.com.sunshine.gcm.MyGcmListenerService;
import saulo.com.sunshine.gcm.RegistrationIntentService;
import saulo.com.sunshine.pushbots.NotificationHandler;
import saulo.com.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.CallbackForecastFragment, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String TAG = "MainActivityTAG_";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String PROJECT_NUMBER = "165515085157";

    private static int selectionCount = 0;

    private String mLocation;
    private boolean mTwoPane;

    ForecastFragment mForecastFragment;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.a_main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        if(Utility.isFirstLaunch(this)){
            askForLocation();
        }

        mLocation = Utility.getPreferredLocation(getApplicationContext());

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG);
                ft.commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        mForecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        mForecastFragment.setUseTodayLayout(!mTwoPane);


        SunshineSyncAdapter.initializeSyncAdapter(this);

        if (checkPlayServices()) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }

        Pushbots.sharedInstance().init(this);
        Pushbots.sharedInstance().setRegStatus(true);
        Pushbots.sharedInstance().register();
//        Pushbots.sharedInstance().setRegStatus(false);
//        Pushbots.sharedInstance().unregister();
//        Pushbots.sharedInstance().setCustomHandler(GcmReceiver.class);

    }

    private void askForLocation() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder.setTitle("Location");
//        builder.setMessage("Would you like to use your current location?");
//
//        builder.setCancelable(false);
//
//        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//                // Do nothing but close the dialog
//                if (checkForPermissions()) {
//                    connectToLocationAPI();
//                } else {
//                    askForPermissions();
//                }
//                dialog.dismiss();
//            }
//
//        });
//
//        builder.setNegativeButton("Enter zip code", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                requestLocation();
//            }
//        });
//
//        AlertDialog alert = builder.create();
//        alert.show();

        if (checkForPermissions()) {
            connectToLocationAPI();
        } else {
            askForPermissions();
        }
    }

    private void requestLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your zip code");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setCancelable(false);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String location = input.getText().toString();
                saveLocationPreference(location);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                askForLocation();
            }
        });

        builder.show();
    }

    private void askForPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private void connectToLocationAPI() {
        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private boolean checkForPermissions() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            mForecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != mForecastFragment) {
                mForecastFragment.onLocationChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
        selectionCount = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri uri, ForecastAdapter.ForecastAdapterViewHolder vh) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, uri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);


            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (selectionCount > 1) { //temporal workaround for bad animations
                ft.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit);
            } else {
                selectionCount++;
            }
            ft.replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG);
            ft.commitAllowingStateLoss();


        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(uri);

            ActivityOptionsCompat activityOptions =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            new Pair<View, String>(vh.iconView, getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //supress the warning since we already checked for the permission
        @SuppressWarnings("MissingPermission") Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String zipCode = null;
        try {
            zipCode = geocoder.getFromLocation(latitude, longitude, 1).get(0).getPostalCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (zipCode != null) {
            saveLocationPreference(zipCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    connectToLocationAPI();

                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected synchronized GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void saveLocationPreference(String location) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_location_key), location);
        editor.putBoolean(getString(R.string.pref_first_launch), false);
        editor.commit();

//        SunshineSyncAdapter.syncImmediately(this);
//        SunshineSyncAdapter.initializeSyncAdapter(this);
        mForecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        if (null != mForecastFragment) {
            mForecastFragment.onLocationChanged();
        }
        mLocation = location;

    }
}
