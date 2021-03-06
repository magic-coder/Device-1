package com.speedata.device.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.DeviceControlSpd;
import android.serialport.SerialPortSpd;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.speedata.device.R;
import com.speedata.libutils.DataConversionUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


public class HelperActivity extends Activity {

    private ImageView imgBack;
    private TextView title;
    private ImageView imgFaq;
    private Context mContext;
    private Button close;
    public Button sendButton;
    private Button clear;
    private Button clear1;
    private Button set;
    private TextView tvState;

    private EditText EditTextaccept;
    private EditText EditTextsend;
    public String sendstring = "";

    private static final String TAG = "SerialPortBackup";
    private int fd;
    private SerialPortSpd mSerialPortBackup;

    private SettingsDialog setPBP;
    private Handler handler = null;
    private byte[] temp1 = new byte[1024];
    private byte[] temp2 = new byte[1024];
    private String buff5 = "";
    private String buff6 = "";
    private boolean key_hex_send = false;
    private boolean key_hex_read = false;
    private CheckBox m_CheckBox1;
    private CheckBox m_CheckBox2;
    private ReadThread mReadThread;
    private boolean debugtest = false;
    private DeviceControlSpd DevCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        imgBack = (ImageView) findViewById(R.id.img_back);
        title = (TextView) findViewById(R.id.tv_title);
        imgFaq = (ImageView) findViewById(R.id.img_faq);
        imgFaq.setImageDrawable(getResources().getDrawable(R.drawable.icon_set));
        imgFaq.setOnClickListener(new ClickEvent());
        title.setText(R.string.serialport);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperActivity.this.finish();
            }
        });
        mContext = this;
        mSerialPortBackup = new SerialPortSpd();
        setPBP = new SettingsDialog(this, this);
        close = (Button) this.findViewById(R.id.close);
        sendButton = (Button) this.findViewById(R.id.send);
        clear = (Button) this.findViewById(R.id.clear);
        clear1 = (Button) this.findViewById(R.id.button1);
        set = (Button) this.findViewById(R.id.set);
        EditTextaccept = (EditText) findViewById(R.id.EditTextaccept);
        tvState = (TextView) findViewById(R.id.tv_state);
        EditTextsend = (EditText) findViewById(R.id.EditTextsend);

        sendButton.setOnClickListener(new ClickEvent());
        close.setOnClickListener(new ClickEvent());
        clear.setOnClickListener(new ClickEvent());
        clear1.setOnClickListener(new ClickEvent());
        set.setOnClickListener(new ClickEvent());
        m_CheckBox1 = (CheckBox) findViewById(R.id.checkBox1);
        m_CheckBox2 = (CheckBox) findViewById(R.id.checkBox2);
        sendButton.setEnabled(false);
//        EditTextsend.setText("7e000ec00600000000000000d47e");
        EditTextsend.setText("bb0003000100047e");
        fd = mSerialPortBackup.getFd();

        mSerialPortBackup.WriteSerialByte(mSerialPortBackup.getFd(),
                HexString2Bytes(sendstring));

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    super.handleMessage(msg);
                    temp2 = (byte[]) msg.obj;
                    if (temp2 != null) {
                        Log.d("read", "read len=" + temp2.length);
                        if (EditTextaccept.getText().toString().length() > 1000) {
                            EditTextaccept.setText("");
                        }
                        if (key_hex_read) {
                            String accept_show = bytesToHexString(temp2);
                            EditTextaccept.append(accept_show + "\n");
                        } else {

                            EditTextaccept.append(DataConversionUtils
                                    .byteArrayToAscii(temp2));

                            EditTextaccept.append("\n");
                        }
                    }
                }
            }

        };

        m_CheckBox1
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (m_CheckBox1.isChecked()) {
                            key_hex_read = true;
                            try {
                                buff5 = "";
                                buff6 = EditTextaccept.getText().toString();
                                StringTokenizer ps = new StringTokenizer(
                                        buff6);
                                buff6 = "";
                                int y = ps.countTokens();
                                for (int s = 0; s < y; s++) {
                                    String ss = ps.nextToken();
                                    buff6 = buff6 + ss;
                                }
                                buff5 = toHexString(buff6);
                                EditTextaccept.setText(buff5);
                            } catch (NumberFormatException p1) {
                                return;
                            }
                            Log.e("checkbox", "m_CheckBox1.isChecked");
                        } else {
                            try {
                                buff5 = EditTextaccept.getText().toString();
                                StringTokenizer ps = new StringTokenizer(buff5);
                                int r = ps.countTokens();
                                buff5 = "";
                                for (int i = 0; i < r; i++) {
                                    String z = ps.nextToken();
                                    buff5 = buff5 + z;
                                }
                            } catch (NumberFormatException p1) {
                                return;
                            }
                            buff5 = toStringHex(buff5);
                            EditTextaccept.setText(buff5);
                            Log.e("checkbox", "m_CheckBox1.isnotChecked");
                            key_hex_read = false;
                        }
                    }
                });
        m_CheckBox2
                .setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (m_CheckBox2.isChecked()) {
                            key_hex_send = true;
                            System.out.println(true);
                            Log.e("checkbox", "m_CheckBox2.isChecked");
                            EditTextsend.setHint(getResources().getString(
                                    R.string.send_hex_hint));

                        } else {
                            key_hex_send = false;
                            EditTextsend.setHint(getResources().getString(
                                    R.string.send_hex_hint_null));
                            System.out.println(key_hex_send);
                        }

                    }

                });
    }

    public SerialPortSpd getmSerialPortBackup() {
        return mSerialPortBackup;
    }

    public DeviceControlSpd getDevCtrl() {
        return DevCtrl;
    }


    private int temp_count = 0;

    private class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == sendButton) {
                sendstring = EditTextsend.getText().toString();
                temp_count++;
                if (!"".equals(sendstring)) {
                    if (!key_hex_send) {
                        send(sendstring);
                    } else {
                        Log.d(TAG, "send_hex " + sendstring);
                        mSerialPortBackup.WriteSerialByte(mSerialPortBackup.getFd(),
                                HexString2Bytes(sendstring));
                    }
                }
            } else if (v == close) {
                setPBP.closeDev();
                setPBP.closeSerial();
                finish();
            } else if (v == clear) {
                EditTextaccept.setText("");
            } else if (v == clear1) {
                EditTextsend.setText("");
            } else if (v == set) {
                setPBP.setTitle(R.string.sure);
                setPBP.show();
            } else if (v == imgFaq) {
                setPBP.setTitle(R.string.sure);
                setPBP.show();
            }
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (debugtest && !isInterrupted()) {

                try {
                    if (mSerialPortBackup != null) {
                        temp1 = mSerialPortBackup.ReadSerial(mSerialPortBackup.getFd(), 1024);
                        Log.d(TAG, "read " + Arrays.toString(temp1));
                        if (temp1 != null) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = temp1;
                            handler.sendMessage(msg);
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc >= 0 ? aSrc : aSrc + 256;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(" ");
        }
        return stringBuilder.toString();
    }

    public static String toHexString(String s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch) + " ";
            str.append(s4);
        }
        return str.toString();
    }

    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];

        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShowState();
    }

    public void startRead() {
        if (!debugtest) {
            debugtest = true;
            mReadThread = new ReadThread();
            mReadThread.start();
            System.out.println("ReadThread_start");
            Log.d(TAG, "read startRead()");
        }
    }

    public void stopRead() {
        Log.d(TAG, "read stopRead()");
        debugtest = false;
    }

    public void ShowState() {
        tvState.setText("");
        if (setPBP.getSerial() > 0) {
            tvState.setText("baudrate:" + setPBP.getBaudrate()
                    + " serial_port:" + setPBP.getSerialPath());
        }
        if (setPBP.getPowercount() > 0) {
            tvState.append(" powerpath:" + setPBP.getPower_path());
        }
        if (setPBP.getPowercount() < 0 && setPBP.getSerial() < 0) {
            tvState.setText("no setting");
        }
    }

    @Override
    public void onDestroy() {
        debugtest = false;
        setPBP.closeDev();
        setPBP.closeSerial();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    int send(String passwd) {
        byte[] pss = passwd.getBytes();
        mSerialPortBackup.WriteSerialByte(fd, pss);
        return 0;
    }


    /**
     * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
     * 0xD9}
     *
     * @param src String
     * @return byte[]
     */
    public byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < src.length() / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public byte uniteBytes(byte src0, byte src1) {
        try {
            byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}));
            _b0 = (byte) (_b0 << 4);
            byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}));
            return (byte) (_b0 ^ _b1);
        } catch (Exception e) {
            Toast.makeText(mContext, "error" + mContext.getResources().getString(R.string.send_hex_hint), Toast.LENGTH_SHORT)
                    .show();
            return 0;
        }
    }
}
