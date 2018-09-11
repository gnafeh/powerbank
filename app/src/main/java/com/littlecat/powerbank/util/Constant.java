package com.littlecat.powerbank.util;

public class Constant {

    public static final String LOG_TAG = "logTag";
    public static final String URL = "http://ftj.gongxiangchongdianbao.org";
    //public static final String URL = "http://nomotest.api.thinker.vc";
	//public static final String URL = "http://tcp.p2go.com.tw";
    public static final String API_SYNC_SETTING = "/api/device/sync_setting";
    public static final String API_SYNC_BATTERY = "/api/device/sync_battery?device_id=";
    public static final String API_REMOVE_BATTERY = "/api/device/remove_battery?device_id=";
    public static final String API_BORROW_CONFIRM = "/api/device/borrow_confirm?device_id=";
    public static final String API_RETURN_BACK = "/api/device/return_back?device_id=";
    public static final String SUCCESS = "0";
    public static final String FAILURE = "1";
    public static final String MAC = "mac";
    public static final String DEVICE = "device";
    public static final String SOFT_VER = "soft_ver";
    public static final String DEVICE_VER = "device_ver";
    public static final String PUSH_ID = "push_id";
    public static final String TCP_CMD_ANS_LOGIN = "20";
    public static final String TCP_CMD_LOGIN = "10";
    public static final String TCP_CMD_ORDER = "11";
    public static final String TCP_CMD_ANS_ORDER = "21";
    public static final String TCP_CMD_SLOT = "12";
    public static final String TCP_CMD_ANS_SLOT = "22";
    public static final String TCP_CMD_RESTART = "13";
    public static final String TCP_CMD_RESETTING = "14";
    public static final String TCP_CMD_HEART = "15";
    public static final String TCP_CMD_LOCK_SLOT= "16";
    public static final String TCP_CMD_UNLOCK_SLOT= "17";
    public static final String TCP_CMD_ANS_HEART = "25";
    public static final String SPLIT = "|";
    public static final String ERR_CODE = "errcode";
    public static final String ERR_MSG = "errmsg";
    public static final String RESPONSE_RESULT = "result";
    public static final String INTENT_BORROW_CONFIRM = "borrow_confirm";
    public static final String ORDER_ID = "order_id";
    public static final long SYNC_SETTING_TIME = 5 * 60 * 1000;
    public static final char uart_send_buf_10[] = {0x55, 0xAA, 0x1A, 0x01, 0x00, 0x10,
            0x04, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x5D, 0x03, 0x00, 0x29,
            0x17, 0x8A, 0xC1, 0x00, 0x00, 0x00,
            0x1B, 0xAA, 0X55};

    public static final char uart_send_buf_12[] = {0x55, 0xAA, 0x1B, 0x01, 0x00, 0x12,
            0x88, 0x80, 0x5D, 0x03, 0x00, 0x29, 0x17, 0x8B,
            0x26, 0x00, 0x00, 0x00, 0x80, 0xC4, 0x01, 0x00, 0x29,
            0x17, 0x88, 0xFD, 0x00, 0x00, 0x00,
            0x91, 0xAA, 0X55};
    public static final int PORT_CMD_BATTERIES_COUNT = 0X10; //��׿��ԃ���ģ�M����
    public static final int PORT_CMD_BATTERIES_INFO = 0X12; //��׿��ԃģ��������Ϣ��0X12����500ms����������ѯ��
    public static final int PORT_CMD_BORROW_BATTERIES_INFO = 0X13;//��׿������ģ�M�YӍ
    public static final int PORT_CMD_UNLOCK_BATTERIES = 0X20;//��׿�������5V���
    public static final int PORT_CMD_LOCK_BATTERIES = 0X21;//��׿�����5V���
    public static final int PORT_CMD_CHARGING_BATTERIES = 0X22;//��׿����س��
    public static final int PORT_CMD_STOP_CHARGING_BATTERIES = 0X23;//��׿ֹͣ��س��
    public static final int PORT_CMD_LIGHT_BATTERIES = 0X25;//��׿��������������
    public static final int PORT_CMD_LIGHT_GROUP = 0X26;//��׿��ģ������
    public static final int PORT_CMD_UNLOCK_PROTECT = 0X28;//��׿����𱣻���
    public static final int PORT_CMD_CLEAN_ADDRESS = 0X30;//��׿�����ַ��
    public static final String TAG = "PowerBank";
    public static final int LOW_VOLTAGE = 3500;
    public static final int HIGH_VOLTAGE = 4200;
    public static final int VOLTAGE_PERCENT = 80 ;
    public static final long INVALIDATE_ID = 0xFFFFFFFFFFL;
    public static final int BORROW_STATUS_SUCCESS = 21;
    public static final int BORROW_STATUS_AGAIN = 22;
    public static final int BORROW_STATUS_NO_BATTERY = 42;
    public static final int BORROW_STATUS_TIMEOUT = 41;
    public static final int BORROW_STATUS_UNKNOWN = 43;

    public static final int BAT_BORROW_STATUS_NO_REQUEST = 0;
    public static final int BAT_BORROW_STATUS_UNLOCK_5V = 1;
    public static final int BAT_BORROW_STATUS_POP_OUT = 2;
    public static final int BAT_BORROW_STATUS_BORROW_OUT = 3;

    public static final byte BAT_STATUS_CHARGING = 0x01;
    public static final byte BAT_STATUS_TEMPERATURE = 0x02;
    public static final byte BAT_STATUS_PROTECT = 0x04;
    public static final byte BAT_STATUS_LOCK_5V_OUT = 0x08;

    public static final int SOUND_BORROW_OK = 1;
    public static final int SOUND_DEVICE_ERROR = 2;
    public static final int SOUND_NET_CONNECT = 3;
    public static final int SOUND_NET_ERROR = 4;
    public static final int SOUND_POWER_ON = 5;
    public static final int SOUND_RESET_NOTICE = 6;
    public static final int SOUND_RETURN_FAIL_1 = 7;
    public static final int SOUND_RETURN_FAIL_2 = 8;
    public static final int SOUND_RETURN_OK = 9;
    public static final int SOUND_TAKE_AWAY = 10;
    public static final int SOUND_BORROW_FAIL = 11;
    public static final String INTENT_LOGIN_SUCCESS = "login_success";
    public static final String APK_NAME = "powerbank.apk";
    public static final long SYNC_SETTING_TIME_NO_NET = 60 * 1000;
    public static final String INTENT_SLOT_BATTERY = "slot_battery";
    public static final String SLOT_ID = "slot_id";
    public static final String INTENT_RESETTING = "resetting";
    public static final String LOCK_SLOT = "lock_slot";


    public static String getMsg(String... info) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : info) {
            stringBuilder.append(str).append(Constant.SPLIT);
        }
        int checksum = 0;
        for (byte b : stringBuilder.toString().getBytes()) {
            checksum += b;
        }
        String temp = stringBuilder.append(checksum).toString();
        return temp;
    }

    public static int getHeight4(byte data) {//获取高四位
        int height;
        height = ((data & 0xf0) >> 4);
        return height;
    }

    public static int getLow4(byte data) {//获取低四位
        int low;
        low = (data & 0x0f);
        return low;
    }

    public static long byteArrayToInt5(byte[] b) {
        long batId = 0;
        long tempValue = 0;

        tempValue = b[0] & 0xFF;
        tempValue = tempValue * 0x100000000l;
        batId += tempValue;
        tempValue = b[1] & 0xFF;
        tempValue = tempValue * 0x1000000;
        batId += tempValue;
        tempValue = b[2] & 0xFF;
        tempValue = tempValue * 0x10000;
        batId += tempValue;
        tempValue = b[3] & 0xFF;
        tempValue = tempValue * 0x100;
        batId += tempValue;
        tempValue = b[4] & 0xFF;
        batId += tempValue;

        return batId;
    }

    public static int byteArrayToInt2(byte[] b) {
        return b[1] & 0xFF |
                (b[0] & 0xFF) << 8;
    }
}