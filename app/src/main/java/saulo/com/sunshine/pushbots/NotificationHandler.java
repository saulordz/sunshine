package saulo.com.sunshine.pushbots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pushbots.push.PBNotificationIntent;
import com.pushbots.push.Pushbots;
import com.pushbots.push.utils.PBConstants;

import java.util.HashMap;

/**
 * Created by saulo on 5/11/16.
 */
public class NotificationHandler  extends BroadcastReceiver
{
    private static final String TAG = "customHandlerTAG_";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        Log.d(TAG, "action=" + action);
        // Handle Push Message when opened
        if (action.equals(PBConstants.EVENT_MSG_OPEN)) {
            //Check for Pushbots Instance
            Pushbots pushInstance = Pushbots.sharedInstance();
            if (!pushInstance.isInitialized()) {
                Log.d(TAG, "Initializing Pushbots.");
                Pushbots.sharedInstance().init(context.getApplicationContext());
            }

            //Clear Notification array
            if (PBNotificationIntent.notificationsArray != null) {
                PBNotificationIntent.notificationsArray = null;
            }
        }
    }
}