package com.xs.testapp.ui.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author shenxiang
 * @date 2022/9/30
 * @description
 */
public class MqttHelper {

    private static class Holder {
        private static MqttHelper INSTANCE = new MqttHelper();
    }

    public static MqttHelper getInstance() {
        return Holder.INSTANCE;
    }

    private static final String TAG = "MqttHelper";

    private static String URL = "tcp://test.ranye-iot.net:1883";

    public static String userName = "androidDevice&hez5VbWz1Ll";
    public static String passWord = "ad7290fa4d7e2f560518f4c0e1b9302892d53709545acbd8850cbe2b0751a90f";

    private String clientId = "mobile_1";

    private String mTopic = "123";


    private String sub_topic_device_status = "/device/status";

    private String post_topic_door = "/device/control/door";

    private MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    private static final Integer qos = 0;

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            LogUtils.d("onSuccess", asyncActionToken.toString());

            subscribeTopic(mTopic);

            subscribeTopic(sub_topic_device_status);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            LogUtils.e("onFailure", exception.toString());
        }
    };

    private MqttCallbackExtended mqttCallbackExtended = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {

            LogUtils.d("connectComplete,reconnect:" + reconnect, "uri:" + serverURI);
        }

        @Override
        public void connectionLost(Throwable cause) {

            LogUtils.d("connectionLost:" + cause.toString());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            LogUtils.d("messageArrived:", topic, message);
            ToastUtils.showShort(message.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

//            LogUtils.d("deliveryComplete:" + token.toString());
        }
    };

    public void init() {
        client = new MqttAndroidClient(Utils.getApp(), URL, clientId);
        client.setCallback(mqttCallbackExtended);
        conOpt = new MqttConnectOptions();

        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(60);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组
        //设置断开后重新连接
        conOpt.setAutomaticReconnect(true);
        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        Log.e(getClass().getName(), "message是:" + message + " myTopic " + mTopic);
        String topic = mTopic;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }
    }

    private void doClientConnection() {
        if (!client.isConnected() && NetworkUtils.isConnected()) {
            try {
                LogUtils.d("connect");
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribeTopic(String topic) {
        try {
            client.subscribe(topic, qos.intValue(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void sendOpenDoor() {

        String msg = "1";
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes());
        message.setQos(qos.intValue());
        publish(post_topic_door, message);

    }

    private void publish(String topic, MqttMessage msg) {
        try {
            if (client != null) {
                client.publish(topic, msg, qos.intValue(), null);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
