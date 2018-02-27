package com.example.apple.mqttchatroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by apple on 2018/2/26.
 */

public class MyService extends Service {

  private Notification.Builder builder;
  private NotificationManager manager;

  private MqttConnectOptions options;
  private MqttAndroidClient client;

  //private String host = "tcp://topvme.info:1883";
  //private String host = "ws://broker.hivemq.com:8000";
  private String host = "tcp://broker.hivemq.com:1883";

  private String topic = "shanetest";
  private String clientId;

  private ScheduledExecutorService scheduler;

  public MyService() {

  }


  @Override public void onCreate() {
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    init();
    startReconnect();

    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  private void init() {
    try {
      clientId = MqttClient.generateClientId();
      client = new MqttAndroidClient(MyService.this, host, clientId, new MemoryPersistence());

      options = new MqttConnectOptions();
      options.setCleanSession(true);
      //options.setUserName(userName);
      //options.setPassword(passWord.toCharArray());
      options.setConnectionTimeout(10);
      options.setKeepAliveInterval(20);
      client.setCallback(new MqttCallback() {

        @Override public void connectionLost(Throwable cause) {
          Log.d("TAG", "connectionLost----------");
        }

        @Override public void deliveryComplete(IMqttDeliveryToken token) {
          Log.d("TAG", "deliveryComplete---------" + token.isComplete());
        }

        @Override public void messageArrived(String topicName, MqttMessage message)
            throws Exception {
          //resultTv.setText(message.toString());
          Log.d("TAG", "messageArrived----------");


          Intent it = new Intent(getApplicationContext(), MainActivity.class);
          //it.putExtra("mqtt", "Topic - " + topicName + " = " + message.toString());
          //it.setAction("MqttBroker");
          //sendBroadcast(it);

          PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 1212, it, PendingIntent.FLAG_UPDATE_CURRENT);

          builder = new Notification.Builder(getApplicationContext());
          builder.setSmallIcon(R.mipmap.ic_launcher)
                 .setContentTitle("Topic :" + topicName)
                 .setContentText("Topic - " + topicName + " = " + message.toString())
                 .setContentIntent(pi)
                 .setAutoCancel(true);

          manager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
          manager.notify(321, builder.build());

          Intent intent = new Intent();
          intent.putExtra("mqtt", "Topic - " + topicName + " = " + message.toString());
          intent.setAction("MqttBroker");
          sendBroadcast(intent);

        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void subscribe() {
    try {
      client.subscribe(topic, 0);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  private void startReconnect() {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        if(!client.isConnected()) {
          connect();
        }
      }
    }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
  }



  private void connect() {
    new Thread(new Runnable() {

      @Override public void run() {

        try {
          //IMqttToken token = client.connect();
          client.connect(options, null, new IMqttActionListener() {
            @Override public void onSuccess(IMqttToken asyncActionToken) {
              Log.d("----onSuccess----", String.valueOf(client.isConnected()));
              Toast.makeText(MyService.this, "---onSuccess---", Toast.LENGTH_LONG).show();
              subscribe();
            }

            @Override public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              Log.d("----onFailure----",
                  String.valueOf(client.isConnected()) + " " + exception.toString());
              Toast.makeText(MyService.this, "---onFailure---", Toast.LENGTH_LONG).show();
            }
          });
        } catch (MqttException e) {
          Log.d("E--------", e.toString());
          e.printStackTrace();
        }
      }
    }).start();
  }
}
