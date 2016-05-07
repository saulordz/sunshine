package saulo.com.sunshine.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by saulo on 5/7/16.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
