package com.example.apple.mqttchatroom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

  private EditText ed;
  private TextView resultTv;
  private Button btn1;
  private Button btn2;
  private Button btn3;

  StringBuilder sb = new StringBuilder();

  //private  String host = "ws://119.29.3.36:5354";

  //private String host = "ws://35.194.216.170:8080";
  private String host = "tcp://topvme.info:1883";
  //private String host = "ws://broker.hivemq.com:8000";

  //private String host = "ws://m14.cloudmqtt.com:37113";

  private String userName = "aaa";
  private String passWord = "bbb";


  private MqttConnectOptions options;
  private MqttAndroidClient client;

  //private String myTopic = "test1";
  private String topic = "test1";
  private String clientId;

  private ScheduledExecutorService scheduler;

  private MyReceiver receiver;

  private Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message message) {

      Log.d("------Handler:", message.toString());
      sb.append(message.obj.toString());
      sb.append("\n");
      resultTv.setText(sb.toString());
      return true;
    }
  });

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    resultTv = (TextView) findViewById(R.id.subtext);
    ed = (EditText) findViewById(R.id.editText);
    btn1 = (Button) findViewById(R.id.button1);
    btn2 = (Button) findViewById(R.id.button2);
    btn3 = (Button) findViewById(R.id.button3);

    btn1.setOnClickListener(this);
    btn2.setOnClickListener(this);
    btn3.setOnClickListener(this);

    if (sb != null) {
      resultTv.setText(sb.toString());
    }

    receiver = new MyReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction("MqttBroker");
    this.registerReceiver(receiver, filter);

    //init();
    //
    //startReconnect();

  }

  //public void subscribe() {
  //  try {
  //    client.subscribe(topic, 0);
  //  } catch (MqttException e) {
  //    e.printStackTrace();
  //  }
  //}

  @Override public void onClick(View view) {

    String message = ed.getText().toString();
    ed.setText("");
    switch (view.getId()) {
      case R.id.button1:
        try {
          client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
          e.printStackTrace();
        }
        break;
      case R.id.button2:
        Log.d("--------", String.valueOf(client.isConnected()));
        Toast.makeText(MainActivity.this,
            "---Checked Connected--- : " + String.valueOf(client.isConnected()), Toast.LENGTH_LONG)
            .show();
        break;
      case R.id.button3:
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        Log.d("---Service--: ","is working");
        break;
        default:
    }
  }

  //private void startReconnect() {
  //  scheduler = Executors.newSingleThreadScheduledExecutor();
  //  scheduler.scheduleAtFixedRate(new Runnable() {
  //
  //    @Override
  //    public void run() {
  //      if(!client.isConnected()) {
  //        connect();
  //      }
  //    }
  //  }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
  //}
  //
  //private void init() {
  //  try {
  //
  //    //client = new MqttClient(host, "test", new MemoryPersistence());
  //    //client = new MqttAsyncClient(host, "shimizushane", new MemoryPersistence());
  //
  //    clientId = MqttClient.generateClientId();
  //    client = new MqttAndroidClient(MainActivity.this, host, clientId, new MemoryPersistence());
  //
  //    options = new MqttConnectOptions();
  //    options.setCleanSession(true);
  //    options.setUserName(userName);
  //    options.setPassword(passWord.toCharArray());
  //    options.setConnectionTimeout(10);
  //    options.setKeepAliveInterval(20);
  //    client.setCallback(new MqttCallback() {
  //
  //      @Override public void connectionLost(Throwable cause) {
  //        Log.d("TAG", "connectionLost----------");
  //      }
  //
  //      @Override public void deliveryComplete(IMqttDeliveryToken token) {
  //        Log.d("TAG", "deliveryComplete---------" + token.isComplete());
  //      }
  //
  //      @Override public void messageArrived(String topicName, MqttMessage message)
  //          throws Exception {
  //        //resultTv.setText(message.toString());
  //        Log.d("TAG", "messageArrived----------");
  //        Message msg = new Message();
  //        msg.what = 1;
  //        msg.obj = "Topic - " + topicName + " = " + message.toString();
  //        handler.sendMessage(msg);
  //      }
  //    });
  //    //			connect();
  //  } catch (Exception e) {
  //    e.printStackTrace();
  //  }
  //}
  //
  //private void connect() {
  //  new Thread(new Runnable() {
  //
  //    @Override public void run() {
  //
  //      try {
  //        //IMqttToken token = client.connect();
  //        client.connect(options, null, new IMqttActionListener() {
  //          @Override public void onSuccess(IMqttToken asyncActionToken) {
  //            Log.d("----onSuccess----", String.valueOf(client.isConnected()));
  //            Toast.makeText(MainActivity.this, "---onSuccess---", Toast.LENGTH_LONG).show();
  //            subscribe();
  //          }
  //
  //          @Override public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
  //            Log.d("----onFailure----",
  //                String.valueOf(client.isConnected()) + " " + exception.toString());
  //            Toast.makeText(MainActivity.this, "---onFailure---", Toast.LENGTH_LONG).show();
  //          }
  //        });
  //      } catch (MqttException e) {
  //        Log.d("E--------", e.toString());
  //        e.printStackTrace();
  //      }
  //    }
  //  }).start();
  //}

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if(client != null && keyCode == KeyEvent.KEYCODE_BACK) {
      try {
        client.disconnect();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onStop() {
    super.onStop();
    resultTv.setText(sb.toString());
  }

  @Override
  protected void onDestroy() {
    //super.onDestroy();
    //try {
    //  scheduler.shutdown();
    //  client.disconnect();
    //} catch (MqttException e) {
    //  e.printStackTrace();
    //}

    //Intent stopIntent = new Intent(this, MyService.class);
    //stopService(stopIntent);
  }

  public class MyReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      Bundle bundle = intent.getExtras();
      String strTemp = bundle.getString("mqtt");

      Log.d("------MyReceiver:", strTemp);
      Message msg = new Message();
      msg.obj = strTemp;
      handler.sendMessage(msg);
    }
  }
}
