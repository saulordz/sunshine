package saulo.com.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by saulo on 4/16/16.
 */
public class ForecastFragment extends Fragment {

    private static final String TAG = "ForecastFragmentTAG_";
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
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastData = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_text_view,
                mForecastData);

        mListView = (ListView) rootView.findViewById(R.id.f_main_list_view);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = mForecastData.get(position);
//                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, text);
                getActivity().startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateWeather() {
        String userLocation = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getActivity().getString(R.string.pref_location_key), getActivity().getString(R.string.pref_location_default));
        Log.d(TAG, "updateWeather: " + userLocation);
        new FetchWeatherTask(getActivity(), mAdapter).execute(userLocation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
        }

        return super.onOptionsItemSelected(item);
    }

}
