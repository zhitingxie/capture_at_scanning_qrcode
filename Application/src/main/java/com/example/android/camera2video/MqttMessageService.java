package com.example.android.camera2video;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttMessageService extends Service {

    private static final String TAG = "MqttMessageService";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;

    public MqttMessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL, Constants.CLIENT_ID);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                String topic = "xm_demo/qrcode/id";
                if (!topic.isEmpty()) {
                    try {
                        pahoMqttClient.subscribe(mqttAndroidClient, topic, 1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                pahoMqttClient.reconnect();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                String payload=new String(mqttMessage.getPayload());
                Log.d(TAG, "messageArrived: " + s + " , " + payload);
                //setMessageNotification(s, new String(mqttMessage.getPayload()));

                Intent sendIntent = new Intent("com.google.android.cameraview.demo");
                sendIntent.putExtra("message", "mqtt");
                sendIntent.putExtra("msg1", s);
                sendIntent.putExtra("msg2", new String(mqttMessage.getPayload()));
                sendBroadcast(sendIntent);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {

//            }
//        }, 5000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_message_black_24dp)
                        .setContentTitle(topic)
                        .setContentText(msg);
        Intent resultIntent = new Intent(this, CameraActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(CameraActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());
    }
}
