package saulo.com.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by saulo on 4/16/16.
 */
public class ForecastFragment extends Fragment {

    private ArrayList<String> mForecastData;
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastData = new ArrayList<>();
        mForecastData.add("Today\tSunny\t88/63");
        mForecastData.add("Tomorrow\tRainy\t60/58");
        mForecastData.add("The day after\tSunny\t98/70");
        mForecastData.add("The next day\tHot\t110/100");

        mAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_text_view,
                mForecastData);

        mListView = (ListView) rootView.findViewById(R.id.f_main_list_view);
        mListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("30339");
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "FetchWeatherTaskTAG_";
        private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private static final String ZIP_CODE_PARAMETER = "q";
        private static final String MODE_PARAMETER = "mode";
        private static final String UNIT_PARAMETER = "units";
        private static final String DAYS_PARAMETER = "cnt";
        private static final String API_KEY_PARAMETER= "appid";

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String zipCode = params[0];
            if (zipCode == null) {
                zipCode = "30339"; //default zip code
            }

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

//                Uri.Builder urlBuilder = new Uri.Builder();
//                urlBuilder.appendPath("http://api.openweathermap.org/data/2.5/forecast/daily");
//                urlBuilder.appendQueryParameter(ZIP_CODE_PARAMETER, zipCode);
//                urlBuilder.appendQueryParameter(MODE_PARAMETER, "json");
//                urlBuilder.appendQueryParameter(UNIT_PARAMETER, "metric");
//                urlBuilder.appendQueryParameter(DAYS_PARAMETER, "7");

                Uri urlBuilder = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(ZIP_CODE_PARAMETER, zipCode)
                        .appendQueryParameter(MODE_PARAMETER, "json")
                        .appendQueryParameter(UNIT_PARAMETER, "metric")
                        .appendQueryParameter(DAYS_PARAMETER, "7")
                        .appendQueryParameter(API_KEY_PARAMETER, "245a178c9694509056d37a592a355b5d")
                        .build();
                Log.d(TAG, "doInBackground: " + urlBuilder.toString());

                URL url = new URL(urlBuilder.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            Log.d(TAG, "doInBackground: " + forecastJsonStr);

            return forecastJsonStr;
        }

    }
}
