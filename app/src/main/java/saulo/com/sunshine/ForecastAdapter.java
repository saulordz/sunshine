package saulo.com.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import saulo.com.sunshine.data.WeatherContract;

/**
 * Created by saulo on 4/19/16.
 */
public class ForecastAdapter extends CursorAdapter {

    private final static int VIEW_TYPE_TODAY = 0;
    private final static int VIEW_TYPE_REGULAR = 1;
    private boolean mUseTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        String highLowStr = Utility.formatTemperature(mContext, high) + "/" + Utility.formatTemperature(mContext, low);
        return highLowStr;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_REGULAR;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
            This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
            string.
         */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String highAndLow = formatHighLows(
                cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(WeatherContract.COL_WEATHER_DATE)) +
                " - " + cursor.getString(WeatherContract.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        int conditionId = cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(
                        conditionId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(
                        conditionId);
                break;
            }
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, conditionId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.iconView);

        // Read date from cursor
        long dateInMillis = cursor.getLong(WeatherContract.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        // Get description from weather condition ID
        String description = Utility.getStringForWeatherCondition(context, conditionId);
        // Find TextView and set weather forecast on it
        viewHolder.weatherView.setText(description);

        // Read high temperature from cursor
        String high = Utility.formatTemperature(
                context, cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP));
        viewHolder.highView.setText(high);

        // Read low temperature from cursor
        String low = Utility.formatTemperature(context, cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP));
        viewHolder.lowView.setText(low);

    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView weatherView;
        public final TextView highView;
        public final TextView lowView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            weatherView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}