package com.xs.testapp.ui.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author shenxiang
 * @date 2022-06-23
 */
public class MQTTAliHelper {

    //静态内部类,包含一个静态属性：Singleton
    private static class Holder {
        private static final MQTTAliHelper INSTANCE = new MQTTAliHelper();
    }
    //对外公有的静态方法，直接返回SingletonInstance.INSTANCE
    public static synchronized MQTTAliHelper getInstance() {
        return Holder.INSTANCE;
    }

    private final String TAG = "AiotMqtt";
    /* 设备三元组信息 */
    final private String PRODUCTKEY = "hez515hu6q7";
    final private String DEVICENAME = "deao";
    final private String DEVICESECRET = "fd5317b796d94db725eb1612a9877320";

    /* 自动Topic, 用于上报消息 */
    final private String PUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";
    /* 自动Topic, 用于接受消息 */
    final private String SUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/get";

    /* 阿里云Mqtt服务器域名 */
//    final String host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";
    final String host = "tcp://iot-06z00etj12xue7p.mqtt.iothub.aliyuncs.com:1883";
    private String clientId;
    private String userName;
    private String passWord;

    MqttAndroidClient mqttAndroidClient;

    public void init(Context ctx) {
        /* 获取Mqtt建连信息clientId, username, password */
        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (aiotMqttOption == null) {
            Log.e(TAG, "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
//            userName = aiotMqttOption.getUsername();
//            passWord = aiotMqttOption.getPassword();
            userName = aiotMqttOption.getUsername();
            passWord = aiotMqttOption.getPassword();

            clientId = "hez515hu6q7.deao|securemode=2,signmethod=hmacsha256,timestamp=1663322091829|";
            userName = "deao&hez515hu6q7";
            passWord = "4a2b238d696b66c9bb5f10f707fd79e39fe9dac9dbfee6f3e44b8d45fa1627eb";
        }

        /* 创建MqttConnectOptions对象并配置username和password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setUserName(userName);
//        mqttConnectOptions.setPassword(passWord.toCharArray());

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(2);
//        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());


        Log.i(TAG, "clientId:" + clientId);
        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(ctx, host, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.i(TAG, "connectComplete:" + reconnect + ",uri:" + serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });

        /* Mqtt建连 */
        try {
            Log.i(TAG, "connect:" + mqttConnectOptions.toString());
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");

                    subscribeTopic(SUB_TOPIC);

                    subscribeTopic("bst_fae");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅特定的主题
     *
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
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

    /**
     * 向默认的主题/user/update发布消息
     *
     * @param payload 消息载荷
     */
    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     */
    static class AiotMqttOption {
        private String username = "";
        private String password = "";
        private String clientId = "";

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public String getClientId() {
            return this.clientId;
        }

        /**
         * 获取Mqtt建连选项对象
         *
         * @param productKey   产品秘钥
         * @param deviceName   设备名称
         * @param deviceSecret 设备机密
         * @return AiotMqttOption对象或者NULL
         */
        public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
            if (productKey == null || deviceName == null || deviceSecret == null) {
                return null;
            }

            try {
                String timestamp = Long.toString(System.currentTimeMillis());

                // clientId
                this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                        ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";

                // userName
                this.username = deviceName + "&" + productKey;

                // password
                String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                        deviceName + "productKey" + productKey + "timestamp" + timestamp;
                String algorithm = "HmacSHA256";
                Mac mac = Mac.getInstance(algorithm);
                SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
                mac.init(secretKeySpec);
                byte[] macRes = mac.doFinal(macSrc.getBytes());
                password = String.format("%064x", new BigInteger(1, macRes));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return this;
        }
    }


}
