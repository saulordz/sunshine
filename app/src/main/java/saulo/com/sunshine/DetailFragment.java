package saulo.com.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import saulo.com.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String HASHTAG_POSTFIX = "#SunshineApp";
    private ShareActionProvider mShareActionProvider;

    private TextView mTextViewDayName;
    private TextView mTextViewDate;
    private TextView mTextViewMinTemp;
    private TextView mTextViewMaxTemp;
    private TextView mTextViewCondition;
    private TextView mTextViewHumidity;
    private TextView mTextViewWind;
    private TextView mTextViewPressure;

    private ImageView mImageView;

    private String dayName;
    private String date;
    private String minTemp;
    private String maxTemp;
    private String condition;
    private String humidity;
    private String wind;
    private String pressure;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
//                forecast = intent.getDataString();
            getLoaderManager().initLoader(ForecastFragment.WEATHER_LOADER_ID, null, this);
        }

        mTextViewDayName = (TextView) rootView.findViewById(R.id.f_main_day_name_textview);
        mTextViewDate = (TextView) rootView.findViewById(R.id.f_main_date_textview);
        mTextViewMinTemp = (TextView) rootView.findViewById(R.id.f_main_min_textview);
        mTextViewMaxTemp = (TextView) rootView.findViewById(R.id.f_main_max_textview);
        mTextViewCondition = (TextView) rootView.findViewById(R.id.f_main_condition_textview);
        mTextViewHumidity = (TextView) rootView.findViewById(R.id.f_main_humidity_textview);
        mTextViewWind = (TextView) rootView.findViewById(R.id.f_main_wind_textview);
        mTextViewPressure = (TextView) rootView.findViewById(R.id.f_main_pressure_textview);
        mImageView = (ImageView) rootView.findViewById(R.id.f_main_image_view);

//            mTextView = (TextView) rootView.findViewById(R.id.f_detail_text_view);
//            mTextView.setText(forecast);
        return rootView;
    }

    private void initData() {

    }

    private void initViews() {

    }

    private Intent getShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_TEXT,
//                    forecast + " " + HASHTAG_POSTFIX);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        mShareActionProvider.setShareIntent(getShareIntent());
//            if (forecast != null) {
//                mShareActionProvider.setShareIntent(getShareIntent());
//            }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                WeatherContract.FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String nameOfTheDay = Utility.getFriendlyDayString(
                getActivity(), data.getLong(WeatherContract.COL_WEATHER_DATE));
        mTextViewDayName.setText(nameOfTheDay);

        String dateString = Utility.formatDate(
                data.getLong(WeatherContract.COL_WEATHER_DATE));
        mTextViewDate.setText(dateString);

        String weatherDescription =
                data.getString(WeatherContract.COL_WEATHER_DESC);
        mTextViewCondition.setText(weatherDescription);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP), isMetric);
        mTextViewMaxTemp.setText(high);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP), isMetric);
        mTextViewMinTemp.setText(low);

        mImageView.setImageResource(R.mipmap.ic_launcher);
//            String humidity = "Humidity " + data.getString(WeatherContract.COL_WEATHER)
//            String wind = data.getString(WeatherContract.COL_);
//            String pressure
//            mTextView.setText(forecast);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}