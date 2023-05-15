package com.example.neurosky;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;

import android.content.Context;
import android.content.DialogInterface;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.text.InputType;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TgStreamReader tgStreamReader;

    private BluetoothAdapter mBluetoothAdapter;

    private int badPacketCount = 0;
    private int time = 0;

    private String fileName = null;

    private List<RecordedData> recordedDataList = new ArrayList<>();

    private TextView connectText;
    private TextView signalQualityText;
    private TextView deltaText;
    private TextView thetaText;
    private TextView lowAlphaText;
    private TextView highAlphaText;
    private TextView lowBetaText;
    private TextView highBetaText;
    private TextView lowGammaTex;
    private TextView highGammaText;
    private TextView stateText;

    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectText = (TextView) this.findViewById(R.id.connectText);
        signalQualityText = (TextView) this.findViewById(R.id.signalQualityText);
        deltaText = (TextView) this.findViewById(R.id.deltaText);
        thetaText = (TextView) this.findViewById(R.id.thetaText);
        lowAlphaText = (TextView) this.findViewById(R.id.lowAlphaText);
        highAlphaText = (TextView) this.findViewById(R.id.highAlphaText);
        lowBetaText = (TextView) this.findViewById(R.id.lowBetaText);
        highBetaText = (TextView) this.findViewById(R.id.highBetaText);
        lowGammaTex = (TextView) this.findViewById(R.id.lowGammaText);
        highGammaText = (TextView) this.findViewById(R.id.highGammaText);
        stateText = (TextView) this.findViewById(R.id.stateText);

        buttonStart = (Button) this.findViewById(R.id.buttonStart);
        buttonStop = (Button) this.findViewById(R.id.buttonStop);
        buttonSave = (Button) this.findViewById(R.id.buttonSave);

        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(false);
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
        tgStreamReader.setGetDataTimeOutTime(6);
        tgStreamReader.startLog();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                badPacketCount = 0;

                if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }
                    tgStreamReader.connect();

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                    buttonSave.setEnabled(false);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (tgStreamReader != null) {
                    tgStreamReader.stop();
                    tgStreamReader.close();

                    buttonSave.setEnabled(true);
                    buttonStop.setEnabled(false);
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "Save file");
                writeData();

                buttonSave.setEnabled(false);
                buttonStart.setEnabled(true);
            }
        });
    }

    public void stop() {
        if (tgStreamReader != null) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }
    }

    @Override
    protected void onDestroy() {
        if (tgStreamReader != null) {
            tgStreamReader.close();
            tgStreamReader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    connectText.setText(getString(R.string.connect) + " Connecting");
                    break;

                case ConnectionStates.STATE_CONNECTED:
                    tgStreamReader.start();
                    connectText.setText(getString(R.string.connect) + " Connected");
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;

                case ConnectionStates.STATE_WORKING:
                    tgStreamReader.startRecordRawData();
                    showToast("Working", Toast.LENGTH_SHORT);
                    break;

                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    tgStreamReader.stopRecordRawData();
                    showToast("Get data time out!", Toast.LENGTH_SHORT);
                    break;

                case ConnectionStates.STATE_STOPPED:
                    connectText.setText(getString(R.string.connect) + " Stopped");
                    break;

                case ConnectionStates.STATE_DISCONNECTED:
                    connectText.setText(getString(R.string.connect) + " Disconnected");
                    break;

                case ConnectionStates.STATE_ERROR:
                    connectText.setText(getString(R.string.connect) + " Error");
                    break;

                case ConnectionStates.STATE_FAILED:
                    connectText.setText(getString(R.string.connect) + " Failed");
                    buttonStart.setEnabled(true);
                    buttonStop.setEnabled(false);
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onRecordFail(int flag) {
            Log.e(TAG, "onRecordFail: " + flag);
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            badPacketCount++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }
    };

    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;

    int raw;
    private Handler LinkDetectedHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            // (8) demo of MindDataType
            switch (msg.what) {
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower) msg.obj;
                    if (power.isValidate()) {
                        deltaText.setText(getString(R.string.delta) + " " + power.delta);
                        thetaText.setText(getString(R.string.theta) + " " + power.theta);
                        lowAlphaText.setText(getString(R.string.low_alpha) + " " + power.lowAlpha);
                        highAlphaText.setText(getString(R.string.high_alpha) + " " + power.highAlpha);
                        lowBetaText.setText(getString(R.string.low_beta) + " " + power.lowBeta);
                        highBetaText.setText(getString(R.string.high_beta) + " " + power.highBeta);
                        lowGammaTex.setText(getString(R.string.low_gamma) + " " + power.lowGamma);
                        highGammaText.setText(getString(R.string.high_gamma) + " " + power.middleGamma);

                        recordedDataList.add(new RecordedData());
                        recordedDataList.get(recordedDataList.size() - 1).setTime(time);
                        recordedDataList.get(recordedDataList.size() - 1).setDelta(power.delta);
                        recordedDataList.get(recordedDataList.size() - 1).setTheta(power.theta);
                        recordedDataList.get(recordedDataList.size() - 1).setLowAlpha(power.lowAlpha);
                        recordedDataList.get(recordedDataList.size() - 1).setHighAlpha(power.highAlpha);
                        recordedDataList.get(recordedDataList.size() - 1).setLowBeta(power.lowBeta);
                        recordedDataList.get(recordedDataList.size() - 1).setHighBeta(power.highBeta);
                        recordedDataList.get(recordedDataList.size() - 1).setLowGamma(power.lowGamma);
                        recordedDataList.get(recordedDataList.size() - 1).setHighGamma(power.middleGamma);
                        if ((recordedDataList.size() - 1) > 1) {
                            recordedDataList.get(recordedDataList.size() - 1).setState(recordedDataList.get(recordedDataList.size() - 2).getDominates());
                        } else recordedDataList.get(recordedDataList.size() - 1).setState(null);

                        stateText.setText(getString(R.string.state) + " " + recordedDataList.get(recordedDataList.size() - 1).getState());

                        time++;
                        if (time == 86400) time = 0;
                    }
                    break;

                case MindDataType.CODE_POOR_SIGNAL://
                    int poorSignal = msg.arg1;
                    Log.d(TAG, "poorSignal:" + poorSignal);
                    signalQualityText.setText(getString(R.string.signalQuality) + " " + msg.arg1);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void showToast(final String msg, final int timeStyle) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    private void writeData() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString() == "") fileName = null;
                else {
                    fileName = input.getText().toString() + ".txt";
                    try {
                        File myFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
                        myFile.createNewFile();
                        FileOutputStream outputStream = new FileOutputStream(myFile);
                        outputStream.write(Write("Time", 10).getBytes());
                        /*outputStream.write(Write("Delta", 10).getBytes());
                        outputStream.write(Write("Theta", 10).getBytes());
                        outputStream.write(Write("LowAlpha", 10).getBytes());
                        outputStream.write(Write("HighAlpha", 10).getBytes());
                        outputStream.write(Write("LowBeta", 10).getBytes());
                        outputStream.write(Write("HighBeta", 10).getBytes());
                        outputStream.write(Write("LowGamma", 10).getBytes());
                        outputStream.write(Write("HighGamma", 10).getBytes());*/
                        outputStream.write("State\n".getBytes());

                        for (RecordedData rd : recordedDataList) {
                            outputStream.write(Write(rd.getTime(), 10).getBytes());
                            /*outputStream.write(Write(rd.getDelta(), 10).getBytes());
                            outputStream.write(Write(rd.getTheta(), 10).getBytes());
                            outputStream.write(Write(rd.getLowAlpha(), 10).getBytes());
                            outputStream.write(Write(rd.getHighAlpha(), 10).getBytes());
                            outputStream.write(Write(rd.getLowBeta(), 10).getBytes());
                            outputStream.write(Write(rd.getHighBeta(), 10).getBytes());
                            outputStream.write(Write(rd.getLowGamma(), 10).getBytes());
                            outputStream.write(Write(rd.getHighGamma(), 10).getBytes());*/
                            outputStream.write(Write(rd.getState(), 10).getBytes());
                            outputStream.write("\n".getBytes());
                        }

                        outputStream.close();

                        recordedDataList.clear();
                        time = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private String Write(String text, int line) {

        if (text.length() < line) {
            line = line - text.length();
            for (int i = 0; i < line; i++) {
                text += " ";
            }
        }

        return text;
    }
}