package saulo.com.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;

import saulo.com.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String HASHTAG_POSTFIX = "Look at the weather! #Sunshine\t";
    static final String DETAIL_URI = "detail_uri";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    private static final String TAG = "DetailFragmentTAG_";

    private boolean mTransitionAnimation;
    private Uri mUri;

    private TextView mTextViewDate;
    private TextView mTextViewMinTemp;
    private TextView mTextViewMaxTemp;
    private TextView mTextViewCondition;
    private TextView mTextViewHumidity;
    private TextView mTextViewWind;
    private TextView mTextViewPressure;
    private ImageView mImageView;

    private String mForecast;

    private ShareActionProvider mShareActionProvider;

    public DetailFragment() {

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ForecastFragment.WEATHER_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        if (arguments != null) {

            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);

        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTextViewDate = (TextView) rootView.findViewById(R.id.f_main_textview_date);
        mTextViewMinTemp = (TextView) rootView.findViewById(R.id.f_main_textview_min_temp);
        mTextViewMaxTemp = (TextView) rootView.findViewById(R.id.f_main_textview_max_temp);
        mTextViewCondition = (TextView) rootView.findViewById(R.id.f_main_textview_condition);
        mTextViewHumidity = (TextView) rootView.findViewById(R.id.f_main_textview_humidity_detail);
        mTextViewWind = (TextView) rootView.findViewById(R.id.f_main_textview_wind_detail);
        mTextViewPressure = (TextView) rootView.findViewById(R.id.f_main_textview_pressure_detail);
        mImageView = (ImageView) rootView.findViewById(R.id.f_main_image_view);
        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    WeatherContract.FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        ViewParent vp = getView().getParent();
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && !data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView ) {
                ((View)vp).setVisibility(View.VISIBLE);
            }
        }


        String dateString = Utility.formatDate(
                data.getLong(WeatherContract.COL_WEATHER_DATE));
        mTextViewDate.setText(dateString);

        String weatherDescription =
                data.getString(WeatherContract.COL_WEATHER_DESC);
        mTextViewCondition.setText(weatherDescription);

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP));
        mTextViewMaxTemp.setText(high);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP));
        mTextViewMinTemp.setText(low);

        int condition = data.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        Glide.with(this)
                .load(Utility.getArtUrlForWeatherCondition(getActivity(), condition))
                .error(Utility.getArtResourceForWeatherCondition(condition))
                .crossFade()
                .into(mImageView);

        String humidity = data.getString(WeatherContract.COL_WEATHER_HUMIDITY) + "%";
        mTextViewHumidity.setText(humidity);

        Float wind = data.getFloat(WeatherContract.COL_WEATHER_WIND_SPEED);
        Float windDirection = data.getFloat(WeatherContract.COL_WIND_DEGREES);
        mTextViewWind.setText(Utility.getFormattedWind(getActivity(), wind, windDirection));

        String pressure = data.getString(WeatherContract.COL_WEATHER_PRESSURE);
        DecimalFormat df = new DecimalFormat("000.00##");
        String result = df.format(Double.parseDouble(pressure));
        mTextViewPressure.setText(result + " hPa");

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if ( mTransitionAnimation ) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.detail);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }

    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(ForecastFragment.WEATHER_LOADER_ID, null, this);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        Log.d(TAG, "finishCreatingMenu: ");

        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "finishCreatingMenuasdasd: ");
        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detail, menu);
            finishCreatingMenu(menu);
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG_POSTFIX + mForecast);
        return shareIntent;
    }
}