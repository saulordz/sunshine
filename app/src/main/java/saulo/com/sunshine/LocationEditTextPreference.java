package saulo.com.sunshine;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by saulo on 5/6/16.
 */
public class LocationEditTextPreference extends EditTextPreference implements TextWatcher {

    public static final int DEFAULT_MIN_LOCATION_LENGTH = 3;
    private int minLength;
    private EditText mET;
    private Context mContext;

    public LocationEditTextPreference(Context context, AttributeSet atts) {
        super(context, atts);
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                atts,
                R.styleable.LocationEditTextPreference,
                0, 0);
        try {
            minLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MIN_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            // Add the get current location widget to our location preference
            setWidgetLayoutResource(R.layout.pref_current_location);
        }
    }


    @Override
    protected View onCreateView(final ViewGroup parent) {
        View view = super.onCreateView(parent);
        View currentLocation = view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity settingsActivity = (SettingsActivity) mContext;
                try {
                    Intent i = new PlacePicker.IntentBuilder().build(settingsActivity);
                    settingsActivity.startActivityForResult(i, SettingsActivity.PLACE_PICKER_REQUEST);
                } catch (
                        GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e
                        ){
                }
            }
        });
        return view;
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        mET = getEditText();
        mET.addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Dialog d = getDialog();

        if (d instanceof AlertDialog) {
            AlertDialog dialog = (AlertDialog) d;
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            // Check if the EditText is empty
            if (s.length() < minLength) {
                // Disable OK button
                positiveButton.setEnabled(false);
            } else {
                // Re-enable the button.
                positiveButton.setEnabled(true);
            }
        }
    }
}
