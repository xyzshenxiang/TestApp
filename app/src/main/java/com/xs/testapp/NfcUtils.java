package com.xs.testapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiang.shen
 * @create 2019/4/1
 * @Describe
 */
public class NfcUtils {

    //nfc
    public static NfcAdapter mNfcAdapter;
    public static IntentFilter[] mIntentFilter = null;
    public static PendingIntent mPendingIntent = null;
    public static String[][] mTechList = null;

    /**
     * 构造函数，用于初始化nfc
     */
    public NfcUtils(Activity activity) {
        mNfcAdapter = NfcCheck(activity);
        NfcInit(activity);
    }

    /**
     * 检查NFC是否打开
     */
    public static NfcAdapter NfcCheck(Activity activity) {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            return null;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                activity.startActivity(setNfc);
            }
        }
        return mNfcAdapter;
    }

    /**
     * 初始化nfc设置
     */
    public static void NfcInit(Activity activity) {
        mPendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mIntentFilter = new IntentFilter[]{filter, filter2};
        mTechList = null;
    }

    /**
     * 读取NFC的数据
     */
    public static String readNFCFromTag(Intent intent) throws UnsupportedEncodingException {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            if (mNdefRecord != null) {
                String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                return readResult;
            }
        }
        return "";
    }


    /**
     * 往nfc写入数据
     */
    public static void writeNFCToTag(String data, Intent intent) throws IOException, FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefRecord ndefRecord = NdefRecord.createTextRecord(null, data);
        NdefRecord[] records = {ndefRecord};
        NdefMessage ndefMessage = new NdefMessage(records);
        ndef.writeNdefMessage(ndefMessage);
    }

    /**
     * 读取nfcID
     */
    public static String readNFCId(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String id = ByteArrayToHexString(tag.getId());
        return id;
    }

    /**
     * 将字节数组转换为字符串
     */
    public static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public static byte[] pwd1 = {1, 2, 3, 4, 5, 6};
    public static byte[][] pwd = {MifareClassic.KEY_DEFAULT, pwd1};

    public static void readmfc(Tag tag) {
        //拿来装读取出来的数据，key代表扇区数，后面list存放四个块的内容
        Map<String, List<String>> map = new HashMap<>();
        MifareClassic mfc = MifareClassic.get(tag);
        //如果当前IC卡不是这个格式的mfc就会为空
        if (null != mfc) {
            try {
                //链接NFC
                mfc.connect();
                //获取扇区数量
                int count = mfc.getSectorCount();
                //用于判断时候有内容读取出来
                boolean flag = false;
                for (int i = 0; i < count; i++) {
                    List<String> list = new ArrayList<>();
                    //默认密码，如果是自己已知密码可以自己设置
//                    byte[] bytes = {(byte) 0, (byte) 0, (byte) 0,
//                            (byte) 0, (byte) 0, (byte) 0};
                    for (int p = 0; p < pwd.length; p++) {
                        //验证扇区密码，否则会报错（链接失败错误）
                        //这里验证的是密码A，如果想验证密码B也行，将方法中的A换成B就行
                        boolean isOpen = mfc.authenticateSectorWithKeyA(i, pwd[p]);
                        if (isOpen) {
                            //获取扇区里面块的数量
                            int bCount = mfc.getBlockCountInSector(i);
                            //获取扇区第一个块对应芯片存储器的位置
                            //（我是这样理解的，因为第0扇区的这个值是4而不是0）
                            int bIndex = mfc.sectorToBlock(i);
                            bCount = bIndex + bCount;
                            Log.d("NfcUtils,Sector:", String.format("%d,%d", i, bIndex));
                            for (; bIndex < bCount; bIndex++) {
                                //读取数据，这里是循环读取全部的数据
                                //如果要读取特定扇区的特定块，将i，j换为固定值就行
                                byte[] data = mfc.readBlock(bIndex);
                                list.add(ByteArrayToHexString(data));
                            }
                            flag = true;
                            break;
                        }
                    }
                    map.put(i + "", list);
                    Log.d("NfcUtils"+i, list.toString());
                }
                if (flag) {
                    //回调，因为我把方法抽出来了
//                    callback.callBack(map);
                } else {
//                    callback.error();
                }
            } catch (Exception e) {
//                callback.error();
                e.printStackTrace();
            } finally {
                try {
                    mfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final static String password = "123";

    public static void writemfc(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        byte[] data = new byte[16];
        if (null != mfc) {
            try {
                mfc.connect();
//                if (password.length() != PASSWORD_LENTH) {
////                    callback.isSusses(false);
//                    return;
//                }
                int count = mfc.getSectorCount();
//                if (a > count - 1 || a < 0) {
//                    callback.isSusses(false);
//                    return;
//                }
                //将密码转换为keyA
                for (int i = 0; i < password.length(); i++) {
                    data[i] = (byte) password.charAt(i);
                }
                //将密码转换为KeyB 我AB密码一样的，也可以不一样
                for (int i = 0; i < password.length(); i++) {
                    data[i + password.length() + 4] = (byte) password.charAt(i);
                }
                //输入控制位
                data[password.length()] = (byte) 0xff;
                data[password.length() + 1] = (byte) 0x07;
                data[password.length() + 2] = (byte) 0x80;
                data[password.length() + 3] = (byte) 0x69;
                //验证密码
//                boolean isOpen = mfc.authenticateSectorWithKeyA(a, bytes);
//                if (isOpen) {
//                    int bIndex = mfc.sectorToBlock(a);
//                    int bCount = mfc.getBlockCountInSector(a);
//                    //写到扇区的最后一个块
//                    mfc.writeBlock(bIndex + bCount - 1, data);
//                }
//                callback.isSusses(true);
            } catch (Exception e) {
                e.printStackTrace();
//                callback.isSusses(false);
            } finally {
                try {
                    mfc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
