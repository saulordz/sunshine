package saulo.com.sunshine;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by saulo on 5/7/16.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GcmReceiverTAG_";
    private final String LOG_TAG = BroadcastReceiver.class.getSimpleName();

    private static final String EXTRA_SENDER = "from";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Is this our message?? Better be if you're going to act on it!
                if (MainActivity.PROJECT_NUMBER.equals(extras.getString(EXTRA_SENDER))) {
                    // Process message and then post a notification of the received message.
                    String weather = extras.getString(EXTRA_WEATHER);
                    String location = extras.getString(EXTRA_LOCATION);
                    String alert = "Heads up: " + weather + " in " + location + "!";
                    sendNotification(context, alert);
                }

            }
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with a GCM message.
    private void sendNotification(Context context, String msg) {
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.art_storm)
                        .setContentTitle("Weather Alert!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}