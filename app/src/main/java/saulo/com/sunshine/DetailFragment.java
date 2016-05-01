package saulo.com.sunshine;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
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
    static final String DETAIL_URI = "detail_uri";

    private Uri mUri;

    private TextView mTextViewDayName;
    private TextView mTextViewDate;
    private TextView mTextViewMinTemp;
    private TextView mTextViewMaxTemp;
    private TextView mTextViewCondition;
    private TextView mTextViewHumidity;
    private TextView mTextViewWind;
    private TextView mTextViewPressure;
    private ImageView mImageView;

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

        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTextViewDayName = (TextView) rootView.findViewById(R.id.f_main_day_name_textview);
        mTextViewDate = (TextView) rootView.findViewById(R.id.f_main_date_textview);
        mTextViewMinTemp = (TextView) rootView.findViewById(R.id.f_main_min_textview);
        mTextViewMaxTemp = (TextView) rootView.findViewById(R.id.f_main_max_textview);
        mTextViewCondition = (TextView) rootView.findViewById(R.id.f_main_condition_textview);
        mTextViewHumidity = (TextView) rootView.findViewById(R.id.f_main_humidity_textview);
        mTextViewWind = (TextView) rootView.findViewById(R.id.f_main_wind_textview);
        mTextViewPressure = (TextView) rootView.findViewById(R.id.f_main_pressure_textview);
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
        return null;
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
                data.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP));
        mTextViewMaxTemp.setText(high);

        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP));
        mTextViewMinTemp.setText(low);

        int condition = data.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        mImageView.setImageResource(Utility.getArtResourceForWeatherCondition(condition));

        String humidity = "Humidity " + data.getString(WeatherContract.COL_WEATHER_HUMIDITY) + "%";
        mTextViewHumidity.setText(humidity);

        Float wind = data.getFloat(WeatherContract.COL_WEATHER_WIND_SPEED);
        Float windDirection = data.getFloat(WeatherContract.COL_WIND_DEGREES);
        mTextViewWind.setText(Utility.getFormattedWind(getActivity(), wind, windDirection));

        String pressure = data.getString(WeatherContract.COL_WEATHER_PRESSURE);
        mTextViewPressure.setText("Pressure: " + pressure + " hPa");

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
}