package com.littlecat.powerbank;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.littlecat.powerbank.bean.BatteryBean;
import com.littlecat.powerbank.bean.BatteryDeviceBean;
import com.littlecat.powerbank.bean.BatteryInfo;
import com.littlecat.powerbank.bean.BatterySyncBean;
import com.littlecat.powerbank.bean.DeviceBean;
import com.littlecat.powerbank.bean.MachineBean;
import com.littlecat.powerbank.bean.ReturnBatteryBean;
import com.littlecat.powerbank.bean.ReturnInfoBean;
import com.littlecat.powerbank.bean.SettingBean;
import com.littlecat.powerbank.http.HttpCallback;
import com.littlecat.powerbank.http.OkHttpUtils;
import com.littlecat.powerbank.http.ResultDesc;
import com.littlecat.powerbank.queue.MyQueue;
import com.littlecat.powerbank.serialPort.SerialPort;
import com.littlecat.powerbank.serialPort.SerialPortFinder;
import com.littlecat.powerbank.socket.SocketService;
import com.littlecat.powerbank.util.Constant;
import com.littlecat.powerbank.util.DeviceUtils;
import com.littlecat.powerbank.util.SDcardUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{
    byte uart_send_buf_10[] = {(byte) 0x55, (byte) 0xAA, 0x1A, 0x01, 0x00, 0x10,
            0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, (byte)0xC4, 0x01, 0x00, 0x29,
            0x17, (byte) 0x8B, (byte) 0x1A, 0x00, 0x00, 0x08,
            (byte)0xDF, (byte) 0xAA, 0X55};

    byte uart_send_buf_12[] = {0x55, (byte) 0xAA, 0x1B, 0x01, 0x00, 0x12,
            (byte) 0x88, (byte) 0x80, (byte)0xC4, 0x01, 0x00, 0x29, 0x17, (byte) 0x8B,
            0x4E, 0x00, 0x00, 0x08, (byte) 0x80, (byte) 0x5D, 0x03, 0x00, 0x29,
            0x17, (byte) 0x8A, (byte) 0xF2, 0x00, 0x00, 0x08,
            (byte) 0xC0, (byte) 0xAA, 0X55};
    byte pop_out_index;
    private PowerManager.WakeLock mWakeLock = null;
    private  boolean com_data_parse_done = true;
    protected BaseApplication mApplication;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    private byte[] mBuffer;
    Gson gson = new Gson();
    private SettingBean settingBean;
    private long sync_interval = -1;
    private long temp_sync_interval = -1;
    private long net_connect_audio_play_time = 0;
    DecimalFormat df=new DecimalFormat("0.00");
    //hefang add 20181008
    public boolean isFirstBootup = SystemProperties.get("persist.id.first.poweron").equals("1");
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    syncSetting();
                    break;
                case 1:
                    syncBattery();
                    break;

                case 2:
                    break;
                case 3:
                    playTips(Constant.SOUND_POWER_ON);
                    TextView ResText = (TextView) findViewById(R.id.batteryInfo);
                    ResText.setText("count " + batteryCount);
                    break;
                case 4:
                    TextView ResText2 = (TextView)findViewById(R.id.batteryInfo);
                    ResText2.setText("count " + batteryCount + "usable " + batteryUsableCount);
                    break;
                case 5:
					if(net_connect_audio_play_time == 0)
					{
						net_connect_audio_play_time = System.currentTimeMillis();
	                    playTips(Constant.SOUND_NET_CONNECT);
					}
                    break;
				case 6:
					comGetBatInfo();
					break;
				case 7:
					borrowPopoutTimeout();
					break;
                default:
                    break;
            }
        }
    };
    private int batteryCount;//slot count
    private int batteryTotalCount;//in slot count
    private int batteryEmptyCount;//empty slot count
    private int batteryUsableCount;//usable battery count
    private int battery_addr;
    private Map<String, BatteryInfo> batteryMap = new HashMap<String, BatteryInfo>();
    private Long batteryId;
    private byte[] cmd_param_23 = new byte[23];
    private byte[] cmd_param_22 = new byte[22];
    private byte[] battery_info1 = new byte[10];
    private byte[] battery_info2 = new byte[10];
    private byte[] battery_id1 = new byte[5];
    private byte[] battery_id2 = new byte[5];
    private LinkedList<String> linkedList = new LinkedList<String>();
    private long batteryId1;
    private long batteryId2;
    private byte[] battery_voltage1 = new byte[2];
    private byte[] battery_voltage2 = new byte[2];
    private int batteryVoltage1;
    private int batteryVoltage2;
    private MyQueue queue = new MyQueue();
    private HandlerThread myHandlerThread;
    private Handler handler;
    private int right_battery_addr = -1;
    private boolean borrowSucess = false;
    private String order_id;
    private SoundPool soundPool;
    private boolean loaded;
    Map<Integer, Integer> musicId = new HashMap<Integer, Integer>();
    private Timer timer;
    private boolean lock_slot = true;//0代表槽位锁定，1代表槽位解锁
    private int slot_id;
    private HashMap<String,Boolean> slotMap = new HashMap<String,Boolean>();
	private int borrow_slot_index = 1;
    private int focus_slot_index = 0;
    private int polling_slot_index = 1;

    public void setByteArray(byte[] byteArray) {
        this.mBuffer = byteArray;
    }

    public void Btnclick1(View view) {
        pop_out_index = (byte) 0x01;
        comDataPack(Constant.PORT_CMD_BORROW_BATTERIES_INFO, 1);
        sendDataToPort(mBuffer);

    }

    public void Btnclick2(View view) {
        pop_out_index = (byte) 0x02;
        comDataPack(Constant.PORT_CMD_BORROW_BATTERIES_INFO, 2);
        sendDataToPort(mBuffer);
    }

    // 申请电源锁，禁止休眠
    private void acquireWakeLock() {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
                    .getClass().getCanonicalName());
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null)
                        return;
                    size = mInputStream.read(buffer);
                    Log.d(Constant.TAG, "read " + size);
                    byte[] read = new byte[size];
                    System.arraycopy(buffer, 0, read, 0, size);
                    if (size > 0) {
//                        onDataReceived(buffer, size);
                        parseReceiveData(read);
//                        String reader = new String(read);
                        //Log.d("lixiang", "  " + read[5]);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    private void refreshBatteryInfo() {
        int empty_count = batteryCount;
        int usable_count = 0;
        Iterator<Map.Entry<String, BatteryInfo>> it = batteryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BatteryInfo> entry = it.next();
            BatteryInfo batteryInfo = (BatteryInfo) entry.getValue();

            if (batteryInfo.getBat_id() != Constant.INVALIDATE_ID) {
                empty_count--;
                if ((batteryInfo.getBat_percent() > Constant.VOLTAGE_PERCENT)
                        && (batteryInfo.getBorrow_status() == Constant.BAT_BORROW_STATUS_NO_REQUEST)
                        && (batteryInfo.getBat_temperature() == 0)
                        && ((batteryInfo.getBat_status() & Constant.BAT_STATUS_PROTECT) == 0)) {
                    usable_count++;
                }
            }


        }
        batteryUsableCount = usable_count;
        batteryEmptyCount = empty_count;
        batteryTotalCount = batteryCount - empty_count;
    }

    boolean borrow_success = false;

	private void initFakeBatteryInfo()
	{
		batteryCount = 2;
		batteryTotalCount = 1;
		batteryUsableCount = 1;
		batteryEmptyCount = 0;
        BatteryInfo batteryInfo;
        String slot_str = String.valueOf(1);
		batteryInfo = new BatteryInfo();
        batteryInfo.setBat_id(123456);
        batteryInfo.setBat_voltage(4200);
        batteryInfo.setStatus(8);
        batteryInfo.setBat_status((byte) 0);
        batteryInfo.setSensors((byte)0);

        batteryMap.put(slot_str, batteryInfo);
		
        slot_str = String.valueOf(2);
		batteryInfo = new BatteryInfo();
        batteryInfo.setBat_id(Constant.INVALIDATE_ID);
        batteryInfo.setBat_voltage(4200);
        batteryInfo.setStatus(8);
        batteryInfo.setBat_status((byte)0);
        batteryInfo.setSensors((byte)0);
        batteryMap.put(slot_str, batteryInfo);

	}

    private void parseAndSaveBatteryInfo(byte[] bat_info, int slot_index) {
        boolean insert_action = false;
        boolean pop_out_action = false;
        boolean new_slot = false;
        int vol_ref1 = 2;
        int slot_status = 0;
        byte bat_status = 0;
        byte signal_status = 0;
        long borrowTime = 0;
        BatteryInfo batteryInfo;
        String slot_str = String.valueOf(slot_index);

        if (batteryMap.containsKey(slot_str)) {
            batteryInfo = (BatteryInfo) batteryMap.get(slot_str);
        } else {
            batteryInfo = new BatteryInfo();
            new_slot = true;
        }
        System.arraycopy(bat_info, 1, battery_id1, 0, 5);
        batteryId1 = Constant.byteArrayToInt5(battery_id1);

        if (batteryId1 != Constant.INVALIDATE_ID) {
            System.arraycopy(bat_info, 6, battery_voltage1, 0, 2);
            batteryVoltage1 = Constant.byteArrayToInt2(battery_voltage1);
            if ((batteryVoltage1 & 0x8000) == 0x8000) {
                vol_ref1 = 3;
            }
            batteryVoltage1 = batteryVoltage1 & 0x7FFF;
            batteryVoltage1 = (batteryVoltage1 * 2 * vol_ref1 * 1000) / 4096;
            if (batteryVoltage1 > 5000) {
                Log.e(Constant.TAG, "wrong battery Voltage !");
                return;
            }
            if (new_slot) {
                batteryVoltage2 = batteryVoltage1;
            } else {
                batteryVoltage2 = batteryInfo.getBat_voltage();
                if ((batteryVoltage2 > 4000) && (batteryVoltage2 <= 4060) && (batteryVoltage1 > 4060) && (batteryVoltage1 < 4095)) {
                    batteryVoltage2 = batteryVoltage2;
                } else {
                    batteryVoltage2 = batteryVoltage1;
                }
                batteryId2 = batteryInfo.getBat_id();
                if ((batteryId2 == Constant.INVALIDATE_ID) && (batteryId1 != Constant.INVALIDATE_ID)) {
                    insert_action = true;
                }
            }

            signal_status = bat_info[0];
            bat_status = bat_info[10];

            if ((bat_status & Constant.BAT_STATUS_PROTECT) == Constant.BAT_STATUS_PROTECT) {
                slot_status = 6;//prevent removal
            } else if ((bat_status & Constant.BAT_STATUS_TEMPERATURE) == Constant.BAT_STATUS_TEMPERATURE) {
                slot_status = 7;//high temperature
            }
        } else if (new_slot == false) {
            batteryId2 = batteryInfo.getBat_id();
            if (batteryId2 != Constant.INVALIDATE_ID) {
				pop_out_action = true;
				if(borrowSucess)
				{
					mHandler.removeMessages(7);
	                playTips(Constant.SOUND_BORROW_OK);//播放借出成功
	                borrowSucess = false;
	                borrow_success = true;
					borrow_slot_index = slot_index;

	                borrowTime = batteryInfo.getBorrow_time();
	                borrowTime++;
	                batteryInfo.setBorrow_time(borrowTime);
				}
				else
				{
					//syncBattery();
					mHandler.removeMessages(1);
					mHandler.sendEmptyMessageDelayed(1, 10 * 1000);
				}
            }
        }

        batteryInfo.setBat_id(batteryId1);
        batteryInfo.setBat_voltage(batteryVoltage2);
        batteryInfo.setStatus(slot_status);
        batteryInfo.setBat_status(bat_status);
        batteryInfo.setSensors(bat_info[0]);

        batteryMap.put(slot_str, batteryInfo);

        refreshBatteryInfo();

        if (insert_action) {
            playTips(Constant.SOUND_RETURN_OK);
            returnBackBattery(slot_index);
            if ((bat_status & Constant.BAT_STATUS_LOCK_5V_OUT) != Constant.BAT_STATUS_LOCK_5V_OUT) {
				comDataPack(Constant.PORT_CMD_LOCK_BATTERIES, slot_index);
				sendDataToPort(mBuffer);
            }
        }
        if (borrow_success && pop_out_action) {
			focus_slot_index = 0;
            borrowConfirmAync(order_id, Constant.BORROW_STATUS_AGAIN, batteryId);
        }


    }

    private void parseReceiveData(byte[] read) {
        //解析串口data
        int length = read.length;
        if (length < 5) {
            return;
        }
        com_data_parse_done = false;
        int cmd = read[5];
        int cmd_length = read[2];
        switch (cmd) {
            case Constant.PORT_CMD_BATTERIES_COUNT:
                if (Arrays.equals(read, mBuffer)) {
                    read = uart_send_buf_10;
                }
                batteryCount = (int) read[6];
                batteryCount = batteryCount & 0xFF;
                batteryCount = batteryCount * 2;
                //sendDataRepeat(mBuffer);
				comGetBatInfo();
                syncSetting();
                mHandler.sendEmptyMessageDelayed(3, 5000);
                break;
            case Constant.PORT_CMD_BATTERIES_INFO:
                if (Arrays.equals(read, mBuffer)) {
                    read = uart_send_buf_12;
                    cmd_length = 27;
                }
                //parse the batteries info
                if ((read.length != 32) || (cmd_length != 27)) {
                    Log.e(Constant.TAG, "wrong battery group info !");
                    break;
                }
                battery_addr = read[3];
                if (battery_addr > batteryCount) {
                    Log.e(Constant.TAG, "wrong battery address !");
                    break;
                }

                if ((read[6] & 0xF0) == 0x80) {
                    System.arraycopy(read, 7, cmd_param_23, 0, 11);
                    parseAndSaveBatteryInfo(cmd_param_23, (battery_addr));
                }
                if ((read[6] & 0x0F) == 0x08) {
                    System.arraycopy(read, 18, cmd_param_23, 0, 11);
                    parseAndSaveBatteryInfo(cmd_param_23, (battery_addr + 1));
                }
                break;
            case Constant.PORT_CMD_CLEAN_ADDRESS:
                break;
        }
        com_data_parse_done = true;
    }

    private void comGetBatInfo() {
		if (mOutputStream != null) {
			if ((polling_slot_index > batteryCount) || (polling_slot_index == 0)) {
				polling_slot_index = 1;
			}
			if(focus_slot_index == 0)
			{
				comDataPack(Constant.PORT_CMD_BATTERIES_INFO, polling_slot_index);
			}
			else
			{
				comDataPack(Constant.PORT_CMD_BATTERIES_INFO, focus_slot_index);
			}
			polling_slot_index += 2;
			//if (com_data_parse_done) {
            	sendDataToPort(mBuffer);
			//}
			Log.d("lixiang", "comGetBatInfo");
		}
        mHandler.sendEmptyMessageDelayed(6, 100);
	}

    private void sendDataRepeat(byte[] mBuffer) {
        SendingThread mSendingThread = new SendingThread();
        mSendingThread.start();
    }

    private class SendingThread extends Thread {
        private int index = 0;

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null) {
                        if ((index > batteryCount) || (index == 0)) {
                            index = 1;
                        }
						if(focus_slot_index == 0)
						{
	                        comDataPack(Constant.PORT_CMD_BATTERIES_INFO, index);
						}
						else
						{
	                        comDataPack(Constant.PORT_CMD_BATTERIES_INFO, focus_slot_index);
						}
                        index += 2;
                        if (com_data_parse_done) {
                            mOutputStream.write(mBuffer);
                        }
                        Thread.sleep(100);
                        Log.d("lixiang", "lixiang---send");
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void comDataPack(int cmd, int des) {
        byte[] bytes = new byte[31];
        byte pack_len = 0;
        int i = 0;
        byte src_addr = 0;
        byte dest_addr = 0;
        byte sum_value = 0;
        int arr_index = 0;
        byte[] cmd_param;

        //head
        bytes[arr_index] = (byte) 0x55;
        arr_index++;
        bytes[arr_index] = (byte) 0xAA;
        arr_index++;
        //length
        bytes[arr_index] = (byte) 0x1A;
        arr_index++;
        //source address
        bytes[arr_index] = (byte) 0x00;
        arr_index++;

        switch (cmd) {
            case Constant.PORT_CMD_BATTERIES_COUNT:
            case Constant.PORT_CMD_BATTERIES_INFO:
            case Constant.PORT_CMD_UNLOCK_BATTERIES:
            case Constant.PORT_CMD_LOCK_BATTERIES:
            case Constant.PORT_CMD_CHARGING_BATTERIES:
            case Constant.PORT_CMD_CLEAN_ADDRESS: {
                bytes[arr_index] = (byte) des;
                arr_index++;
                bytes[arr_index] = (byte) cmd;
                arr_index++;
                arr_index += 22;
                break;
            }
            case Constant.PORT_CMD_BORROW_BATTERIES_INFO: {
                bytes[arr_index] = (byte) des;
                arr_index++;
                bytes[arr_index] = (byte) cmd;
                arr_index++;

                //unlock time 30 * 0.1 s
                bytes[arr_index] = 10;
                arr_index++;

                //light time 50 * 0.1 s
                bytes[arr_index] = 20;
                arr_index++;

                arr_index += 20;
                break;
            }
        }
        for (i = 0; i < 26; i++) {
            sum_value += (byte) bytes[i + 2];
        }
        bytes[28] = sum_value;
        bytes[29] = (byte) 0xAA;
        bytes[30] = (byte) 0x55;

        this.mBuffer = bytes;
    }


    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSlotDataFromLocal(getFilePath());
        readSerialPort();
        comDataPack(Constant.PORT_CMD_BATTERIES_COUNT, 1);
        sendDataToPort(mBuffer);
        startOrderQueue();
        initSound();
        //playTips(Constant.SOUND_POWER_ON);
        acquireWakeLock();
		//initFakeBatteryInfo();
        //syncSetting();

    }

	
    private void initSerialPro() {
        readSerialPort();
        comDataPack(Constant.PORT_CMD_BATTERIES_COUNT, 1);
        sendDataToPort(mBuffer);

    }

    private void getSlotDataFromLocal(String filePath) {
        slotMap = gson.fromJson(readJson(filePath),HashMap.class);
    }

    private String getFilePath() {
        String headerDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            headerDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "power_bank_slot";
        } else {
            headerDir = getFilesDir().getAbsolutePath()+ "power_bank_slot";
        }
        File file = new File(headerDir + "/slot_id");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public  String readJson(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        StringBuffer data = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                data.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        file.delete();
        return data.toString();
//        return "{\"data\":" + data.toString() + "}";
    }

    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        musicId.put(Constant.SOUND_BORROW_OK, soundPool.load(this, R.raw.borrow_ok, 1));
        musicId.put(Constant.SOUND_DEVICE_ERROR, soundPool.load(this, R.raw.device_error, 1));
        musicId.put(Constant.SOUND_NET_CONNECT, soundPool.load(this, R.raw.net_connect, 1));
        musicId.put(Constant.SOUND_NET_ERROR, soundPool.load(this, R.raw.net_error, 1));
        musicId.put(Constant.SOUND_POWER_ON, soundPool.load(this, R.raw.power_on, 1));
        musicId.put(Constant.SOUND_RESET_NOTICE, soundPool.load(this, R.raw.reset_notice, 1));
        musicId.put(Constant.SOUND_RETURN_FAIL_1, soundPool.load(this, R.raw.return_fail_1, 1));
        musicId.put(Constant.SOUND_RETURN_FAIL_2, soundPool.load(this, R.raw.return_fail_2, 1));
        musicId.put(Constant.SOUND_RETURN_OK, soundPool.load(this, R.raw.return_ok, 1));
        musicId.put(Constant.SOUND_TAKE_AWAY, soundPool.load(this, R.raw.take_away, 1));
        musicId.put(Constant.SOUND_BORROW_FAIL, soundPool.load(this, R.raw.borrow_fail, 1));
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });

    }

    private void startOrderQueue() {
        myHandlerThread = new HandlerThread("queue-thread");
        myHandlerThread.start();
        handler = new Handler(myHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dealOrder(msg);
                /*
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                Log.d("handler ", "消息： " + msg.what + "  线程： " + Thread.currentThread().getName() + "time= " + System.currentTimeMillis());
            }
        };

    }

    private void dealOrder(Message msg) {
        int address = getRightBattery();
        if (address != -1) {
            comDataPack(Constant.PORT_CMD_UNLOCK_BATTERIES, address);
            sendDataToPort(mBuffer);
            borrowConfirmAync((String) msg.obj, Constant.BORROW_STATUS_SUCCESS, batteryId);
        } else {
            //todo
            //play the failed tips
            borrowConfirmAync((String) msg.obj, Constant.BORROW_STATUS_NO_BATTERY, batteryId);
            playTips(Constant.SOUND_BORROW_FAIL);
        }
    }

    private void playTips(int id) {
        soundPool.play(musicId.get(id), 1, 1, 0, 0, 1);
    }

    private int getRightBattery() {
        int borrow_count_min = 0xFFFFFFFF;
        int error_count_min = 0xFFFFFFFF;
        int cur_error_count;
		boolean get_bat = false;

        Iterator<Map.Entry<String, BatteryInfo>> it = batteryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BatteryInfo> entry = it.next();
            BatteryInfo batteryInfo = entry.getValue();
            int borrowTime = (int)batteryInfo.getBorrow_time();
            if (batteryInfo.getBat_id() != Constant.INVALIDATE_ID
                    && batteryInfo.getBat_percent() > Constant.VOLTAGE_PERCENT
                    && batteryInfo.getBat_temperature() == 0
                    && (((slotMap != null)&&slotMap.containsKey(entry.getKey())) ? slotMap.get(entry.getKey()) : true)
                    && ((batteryInfo.getBat_status() & Constant.BAT_STATUS_PROTECT) == 0)) {
                cur_error_count = batteryInfo.getError_count();
                if(error_count_min == 0xFFFFFFFF)
                {
                    error_count_min = cur_error_count;
                }
                if(borrow_count_min == 0xFFFFFFFF)
                {
                    borrow_count_min = borrowTime + 1;
                }
                if(cur_error_count == error_count_min)
                {
                    if(borrow_count_min > borrowTime)
                    {
                        borrow_count_min = borrowTime;
                        right_battery_addr = Integer.valueOf(entry.getKey());
                        batteryId = batteryInfo.getBat_id();
						get_bat = true;
                    }
                }
                else if(cur_error_count < error_count_min)
                {
                    borrow_count_min = borrowTime;
                    error_count_min = cur_error_count;
                    right_battery_addr = Integer.valueOf(entry.getKey());
                    batteryId = batteryInfo.getBat_id();
					get_bat = true;
                }

            }

        }
		if(get_bat == false)
		{
			return -1;
		}
        return right_battery_addr;
    }

	private int getSlotIndexByBatId(long bat_id){
        int i = 0;
        BatteryInfo batteryInfo;
        String slot_str;
        for (i = 1; i <= batteryCount; i++) {

            slot_str = String.valueOf(i);

            if (batteryMap.containsKey(slot_str)) {
                batteryInfo = (BatteryInfo) batteryMap.get(slot_str);
				if(batteryInfo.getBat_id() == bat_id)
				{
					return i;
				}
            }
        }
        return 0;
	}

    private void batteryErrorNotify(long bat_id) {
        long cur_id;
        int cur_error_count;

        Iterator<Map.Entry<String, BatteryInfo>> it = batteryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BatteryInfo> entry = it.next();
            BatteryInfo batteryInfo = entry.getValue();
            cur_id = batteryInfo.getBat_id();
            if(cur_id == bat_id){
                cur_error_count = batteryInfo.getError_count();
                cur_error_count++;
                batteryInfo.setError_count(cur_error_count);
            }
        }
    }

    private void sendDataToPort(byte[] mBuffer) {
        if (mSerialPort != null) {
            mOutputStream = mSerialPort.getOutputStream();
            try {
                mOutputStream.write(this.mBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void syncBattery() {
        mHandler.sendEmptyMessageDelayed(1, sync_interval * 1000);
        BatteryDeviceBean deviceBean = new BatteryDeviceBean();
        deviceBean.setSlot_count(batteryCount);
        deviceBean.setTotal(batteryTotalCount);
        deviceBean.setUsable(batteryUsableCount);
        deviceBean.setEmpty(batteryEmptyCount);
        deviceBean.setSdcard(0);
        deviceBean.setStatus(0);
        int i = 0;
        BatteryInfo batteryInfo;
        String slot_str;
        BatteryBean batteryBean;
        Map<String, Object> map = new HashMap<String, Object>();
        for (i = 1; i <= batteryCount; i++) {

            slot_str = String.valueOf(i);

            if (batteryMap.containsKey(slot_str)) {
                batteryInfo = (BatteryInfo) batteryMap.get(slot_str);
                batteryBean = new BatteryBean();
				if(batteryInfo.getBat_id() != Constant.INVALIDATE_ID)
				{
	                batteryBean.setId(batteryInfo.getBat_id());
				}
				else
				{
	                batteryBean.setId(0);
				}
                batteryBean.setSlot(i);
                batteryBean.setPower(batteryInfo.getBat_percent());
                batteryBean.setVoltage(batteryInfo.getBat_voltage());
                batteryBean.setCurrent(0);
                batteryBean.setTemperature(batteryInfo.getBat_temperature());
                batteryBean.setBattery_status(batteryInfo.getBat_status());
                batteryBean.setSlot_status(batteryInfo.getStatus());
                batteryBean.setSensors(batteryInfo.getSensor_str());
                map.put(String.valueOf(i), batteryBean);
            }
        }

        mHandler.sendEmptyMessageDelayed(4, 30);
        BatterySyncBean batterySyncBean = new BatterySyncBean();
        batterySyncBean.setDeviceBean(deviceBean);
        batterySyncBean.setBatteryBeanList(map);
        String json = gson.toJson(batterySyncBean);
        OkHttpUtils.postAync(Constant.URL + Constant.API_SYNC_BATTERY + settingBean.getDevice_id(), json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    Log.d("lixiang", "lixiang---code= " + resultDesc.getError_code() + "  msg= " + resultDesc.getReason());
                }
            }

            @Override
            public void onFailure(int code, String message) {
                mHandler.removeMessages(1);
                //syncBattery();
                mHandler.sendEmptyMessageDelayed(1, 60 * 1000);

            }
        });
    }

    private void syncSetting() {
        mHandler.sendEmptyMessageDelayed(0, Constant.SYNC_SETTING_TIME);
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setDevice_ver("2");
        deviceBean.setSoft_ver(String.valueOf(DeviceUtils.getLocalVersion(getApplicationContext()) + 10000));
        deviceBean.setPush_id("1");
        MachineBean machineBean = new MachineBean();
        machineBean.setDeviceBean(deviceBean);
        machineBean.setMac(DeviceUtils.getImei(getApplicationContext()));
        //machineBean.setMac("123456789010000");
        //machineBean.setMac("357807073033281");

        String json = gson.toJson(machineBean);
        OkHttpUtils.postAync(Constant.URL + Constant.API_SYNC_SETTING, json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    String result = resultDesc.getResult();
                    if (null != result) {
                        settingBean = gson.fromJson(result, SettingBean.class);
                        checkUpdateApk(settingBean.getApp_url());
                        if (!flag) {
                            initSocket();
                            localBroadcastManager.registerReceiver(mReciver, mIntentFilter);
                            flag = bindService(mServiceIntent, conn, BIND_AUTO_CREATE);
                        }
                        try {
                            sync_interval = Long.valueOf(settingBean.getSync_interval());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (sync_interval != -1 && sync_interval != temp_sync_interval) {
                            //syncBattery();
                            mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                            temp_sync_interval = sync_interval;
                        }
//                        sendTcpLogin();

                    } else {

                    }
                }
            }

            public void onFailure(int code, String message) {
                mHandler.removeMessages(0);
                //hefang add for first netConect slow 20181008
                if(isFirstBootup) {
                    mHandler.sendEmptyMessageDelayed(0, 1 * 1000);
                }else{
                    mHandler.sendEmptyMessageDelayed(0, 60 * 1000);
                }
            }
        });
    }

    private void checkUpdateApk(String app_url) {
        if (null != app_url && !"null".equals(app_url)) {
            OkHttpUtils.downloadAsynFile(app_url, SDcardUtils.getCachePath(SDcardUtils.apkCache), Constant.APK_NAME, new HttpCallback() {
                @Override
                public void onSuccess(ResultDesc resultDesc) {
                    super.onSuccess(resultDesc);
//                    installApk("",SDcardUtils.getCachePath(SDcardUtils.apkCache)+"/"+Constant.APK_NAME);
                    Log.d("littlecat", "littlecat" + SDcardUtils.getCachePath(SDcardUtils.apkCache) + "/" + Constant.APK_NAME);
//                    Process su = null;
//                    try {
//                        su = Runtime.getRuntime().exec("su");
//                        String cmd = "chmod 777 "+SDcardUtils.getCachePath(SDcardUtils.apkCache);
//                        su.getOutputStream().write(cmd.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    installApk(SDcardUtils.getCachePath(SDcardUtils.apkCache) + "/" + Constant.APK_NAME);
                }

                @Override
                public void onFailure(int code, String message) {
                    super.onFailure(code, message);
                }

                @Override
                public void onProgress(long currentTotalLen, long totalLen) {
                    super.onProgress(currentTotalLen, totalLen);
                    Log.d("littlecat", "  currentTotalLen= " + currentTotalLen + "  totalLen= " + totalLen);
                }
            });
        }
    }

    private void returnBackBattery(int slot_index) {
        ReturnBatteryBean returnBatteryBean = new ReturnBatteryBean();
        ReturnInfoBean returnInfoBean = new ReturnInfoBean();
        BatteryInfo batteryInfo;
        String slot_str = String.valueOf(slot_index);

        batteryInfo = (BatteryInfo) batteryMap.get(slot_str);
        returnBatteryBean.setId(batteryInfo.getBat_id());
        returnBatteryBean.setSlot(slot_index);
        returnBatteryBean.setPower(batteryInfo.getBat_percent());
        returnBatteryBean.setTemperature(batteryInfo.getTemperature());
        returnBatteryBean.setVoltage(batteryInfo.getBat_voltage());
        returnBatteryBean.setCurrent(0);
        returnInfoBean.setBatteryBean(returnBatteryBean);

        String json = gson.toJson(returnInfoBean);
        OkHttpUtils.postAync(Constant.URL + Constant.API_RETURN_BACK + settingBean.getDevice_id(), json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---return onSuccess");
                if (null != resultDesc) {
                    String result = resultDesc.getResult();
                    if (null == result) {
                        int err_code = resultDesc.getError_code();

                        mHandler.removeMessages(1);
                        //syncBattery();
                        mHandler.sendEmptyMessageDelayed(1, 20 * 1000);

                    }
                }
            }

            public void onFailure(int code, String message) {
                mHandler.removeMessages(1);
                //syncBattery();
                mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                /*
                if (DeviceUtils.isNetwork(MainActivity.this)) {
                    returnBackBattery(slot_index);
                }
                */
            }
        });
    }

    private void sendTcpLogin() {
        if (null != iBackService) {
            String msg = Constant.getMsg(Constant.TCP_CMD_LOGIN, settingBean.getDevice_id());
            try {
                iBackService.sendMessage(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }


    private void readSerialPort() {
        try {
            mSerialPort = getSerialPort();

            if (mSerialPort != null) {
                mInputStream = mSerialPort.getInputStream();
                mReadThread = new ReadThread();
                mReadThread.start();
            }
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

//    protected abstract void onDataReceived(final byte[] buffer, final int size);

    @Override
    protected void onDestroy() {
        if (flag) {
            unbindService(conn);
            localBroadcastManager.unregisterReceiver(mReciver);
        }
        releaseWakeLock();
		net_connect_audio_play_time = 0;

        if (mReadThread != null)
            mReadThread.interrupt();
        closeSerialPort();
        mSerialPort = null;
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public IBackService iBackService;


    public MessageBackReciver mReciver;
    private IntentFilter mIntentFilter;
    private Intent mServiceIntent;
    private LocalBroadcastManager localBroadcastManager;
    private boolean flag = false;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iBackService = IBackService.Stub.asInterface(iBinder);
//            sendTcpMsg();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBackService = null;
        }
    };

    public void initSocket() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReciver = new MessageBackReciver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("lixiang", "lixiang---intent= " + intent.getAction());
                String action = intent.getAction();
                switch (action) {
                    case SocketService.SOCKET_INIT_ACTION:
                        sendTcpLogin();
                        break;
                    case Constant.INTENT_BORROW_CONFIRM:
                        order_id = intent.getStringExtra(Constant.ORDER_ID);
                        Message msg = handler.obtainMessage();
                        msg.obj = order_id;
                        handler.sendMessage(msg);
                        break;
                    case Constant.INTENT_LOGIN_SUCCESS:
                        mHandler.sendEmptyMessageDelayed(5, 8000);
                        break;
                    case Constant.INTENT_SLOT_BATTERY:
                        comDataPack(Constant.PORT_CMD_BORROW_BATTERIES_INFO, intent.getIntExtra(Constant.SLOT_ID, 1));
                        sendDataToPort(mBuffer);
                        playTips(Constant.SOUND_TAKE_AWAY);
                        break;

                    case Constant.INTENT_RESETTING:
                        comDataPack(Constant.PORT_CMD_CLEAN_ADDRESS, 1);
                        sendDataToPort(mBuffer);
                        break;
                    case Constant.TCP_CMD_LOCK_SLOT:
                        slot_id = intent.getIntExtra(Constant.LOCK_SLOT, 1);
                        slotMap.put(String.valueOf(slot_id),false);
                        writeToLocal();
                        break;

                    case Constant.TCP_CMD_UNLOCK_SLOT:
                        slot_id = intent.getIntExtra(Constant.LOCK_SLOT, 1);
                        slotMap.put(String.valueOf(slot_id),true);
                        writeToLocal();
                        break;
                }
            }
        };
        mServiceIntent = new Intent(this, SocketService.class);
        mServiceIntent.putExtra("IP",settingBean.getIp());
        mServiceIntent.putExtra("PORT",settingBean.getPort());
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SocketService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(SocketService.MESSAGE_ACTION);
        mIntentFilter.addAction(SocketService.SOCKET_INIT_ACTION);
        mIntentFilter.addAction(Constant.INTENT_BORROW_CONFIRM);
        mIntentFilter.addAction(Constant.INTENT_LOGIN_SUCCESS);
        mIntentFilter.addAction(Constant.INTENT_SLOT_BATTERY);
        mIntentFilter.addAction(Constant.INTENT_RESETTING);
        mIntentFilter.addAction(Constant.TCP_CMD_LOCK_SLOT);
        mIntentFilter.addAction(Constant.TCP_CMD_UNLOCK_SLOT);

    }

	private void borrowPopoutTimeout() {
		borrowSucess = false;
		focus_slot_index = 0;
		borrowFailed();
		batteryErrorNotify(batteryId);
		borrowConfirmAync(order_id, Constant.BORROW_STATUS_UNKNOWN, batteryId);
	}


    private void borrowConfirmAync(final String order_id, final int status, long bat_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> subMap = new HashMap<String, Object>();
		int slot_index = getSlotIndexByBatId(bat_id);
        map.put("orderid", order_id);
        map.put("status", status);
        map.put("retry", 0);
        subMap.put("id", bat_id);
        //subMap.put("slot", slot_index);
        map.put("battery", subMap);
        String json = gson.toJson(map);
        OkHttpUtils.postAync(Constant.URL + Constant.API_BORROW_CONFIRM + settingBean.getDevice_id(), json, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    if (resultDesc.getError_code() == 0) {
                        //Todo成功
                        //通知串口弹出电池
                        if (status == 21) {
                            borrowSucess = true;
                            borrow_success = false;
                            comDataPack(Constant.PORT_CMD_BORROW_BATTERIES_INFO, right_battery_addr);
                            sendDataToPort(mBuffer);
							if((right_battery_addr % 2) == 0)
							{
								focus_slot_index = right_battery_addr - 1;
							}
							else
							{
								focus_slot_index = right_battery_addr;
							}
                            playTips(Constant.SOUND_TAKE_AWAY);//播放借出
                            mHandler.removeMessages(1);
                            //syncBattery();
                            mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                            mHandler.removeMessages(7);
                            mHandler.sendEmptyMessageDelayed(7, 7000);
							/*
                            int i = 0;
                            while(i<140){
                                try {
                                    i++;
                                    if(borrow_success){
                                        borrow_success = false;
                                        //playTips(Constant.SOUND_TAKE_AWAY);
										focus_slot_index = 0;
                                        return ;
                                    }
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
							focus_slot_index = 0;
                            borrowFailed();
                            batteryErrorNotify(batteryId);
                            borrowConfirmAync(order_id, Constant.BORROW_STATUS_UNKNOWN, batteryId);
                            */

                        } else {
                            mHandler.removeMessages(1);
                            //syncBattery();
                            mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                        }

                    } else {
                        if(status == 21) {
                            borrowFailed();
                        }

                    }
                }
            }

            public void onFailure(int code, String message) {
            }
        });
    }

    private boolean writeToLocal() {
        try {
            FileOutputStream fos = new FileOutputStream(getFilePath(), false);
            fos.write(gson.toJson(slotMap).getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void borrowConfirm(final String order_id, final int status, long bat_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> subMap = new HashMap<String, Object>();
		int slot_index = getSlotIndexByBatId(bat_id);
		if(Constant.BORROW_STATUS_AGAIN == status)
		{
			slot_index = borrow_slot_index;
		}
        map.put("orderid", order_id);
        map.put("status", status);
        map.put("retry", 0);
        subMap.put("id", bat_id);
        //subMap.put("slot", slot_index);
        map.put("battery", subMap);
        String json = gson.toJson(map);
        OkHttpUtils.postSync(Constant.URL + Constant.API_BORROW_CONFIRM + settingBean.getDevice_id(), json, null, new HttpCallback() {
            public void onSuccess(ResultDesc resultDesc) {
                Log.d("lixiang", "lixiang---onSuccess");
                if (null != resultDesc) {
                    if (resultDesc.getError_code() == 0) {
                        //Todo成功
                        //通知串口弹出电池
                        if (status == 21) {
                            borrowSucess = true;
                            borrow_success = false;
                            comDataPack(Constant.PORT_CMD_BORROW_BATTERIES_INFO, right_battery_addr);
                            sendDataToPort(mBuffer);
							if((right_battery_addr % 2) == 0)
							{
								focus_slot_index = right_battery_addr - 1;
							}
							else
							{
								focus_slot_index = right_battery_addr;
							}
                            playTips(Constant.SOUND_TAKE_AWAY);//播放借出
                            mHandler.removeMessages(1);
                            //syncBattery();
                            mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                            int i = 0;
                            while(i<140){
                                try {
                                    i++;
                                    if(borrow_success){
                                        borrow_success = false;
										focus_slot_index = 0;
                                        //playTips(Constant.SOUND_TAKE_AWAY);
                                        return ;
                                    }
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
							focus_slot_index = 0;
                            borrowFailed();
                            batteryErrorNotify(batteryId);
                            borrowConfirmAync(order_id, Constant.BORROW_STATUS_UNKNOWN, batteryId);

                        } else {
                            mHandler.removeMessages(1);
                            //syncBattery();
                            mHandler.sendEmptyMessageDelayed(1, 20 * 1000);
                        }

                    } else {
                        if(status == 21) {
                            borrowFailed();
                        }

                    }
                }
            }

            public void onFailure(int code, String message) {
            }
        });
    }

    private void borrowFailed() {
        comDataPack(Constant.PORT_CMD_LOCK_BATTERIES, right_battery_addr);
        sendDataToPort(mBuffer);
        playTips(Constant.SOUND_BORROW_FAIL);//播放借出失败
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    public abstract class MessageBackReciver extends BroadcastReceiver {
        @Override
        public abstract void onReceive(Context context, Intent intent);
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            String path = "dev/ttyMT1";
            //String path = "dev/ttyS1";
            int baudrate = 38400;
            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private void installApk(String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        startActivity(intent);

    }

    private void installApk(String packageName, final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                if (filePath == null || filePath.length() == 0 || file == null) {
                    Log.d(Constant.TAG, "file not found");
                    return;
                }
//                String[] args = { "pm", "install", "-r", filePath };
                String[] args = {"pm", "install", "-i", "com.littlecat.powerbank", "--user", "0", filePath};
                ProcessBuilder processBuilder = new ProcessBuilder(args);
                Process process = null;
                BufferedReader successResult = null;
                BufferedReader errorResult = null;
                StringBuilder successMsg = new StringBuilder();
                StringBuilder errorMsg = new StringBuilder();
                try {
                    process = processBuilder.start();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String s;
                    while ((s = successResult.readLine()) != null) {
                        successMsg.append(s);
                    }
                    while ((s = errorResult.readLine()) != null) {
                        errorMsg.append(s);
                    }
                    Log.d("littlecat", "littlecat" + successMsg + "   errorMsg= " + errorMsg);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (process != null) {
                        process.destroy();
                    }
                }
                if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
                    Log.d(Constant.TAG, "install success");
                } else {
                    Log.d(Constant.TAG, "install failed");
                }
            }
        }).start();
    }

}
