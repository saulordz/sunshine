package saulo.com.sunshine;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by saulo on 5/6/16.
 */
public class LocationEditTextPreference extends EditTextPreference implements TextWatcher {

    public static final int DEFAULT_MIN_LOCATION_LENGTH = 3;
    private int minLength;
    private EditText mET;

    public LocationEditTextPreference(Context context, AttributeSet atts) {
        super(context, atts);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                atts,
                R.styleable.LocationEditTextPreference,
                0, 0);
        try {
            minLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MIN_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
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
