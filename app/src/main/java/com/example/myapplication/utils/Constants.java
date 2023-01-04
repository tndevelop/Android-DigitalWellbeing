package com.example.myapplication.utils;


public class Constants {
    public static final int RESULT_ENABLE = 1;

    public static final int INTERVENTION_FIRST_WEEK = 0;
    public static final int INTERVENTION_2 = 2;



    //REAL MODE
    public static final int N_INTERVENTIONS_P2_LEVEL_UP = 10;//4;
    public static final double N_DAYS_LEVEL_UP = 3;//0;
    public static final int HOUR_DAILY_RECAP = 21;
    public static final double N_DAYS_MONITORING_PHASE = 7;//0.002;
    public static final float PERCENTAGE_MORE_THAN_AVERAGE = 1.2f;//0.2f;

    //DEBUG MODE
    /*
    public static final int N_INTERVENTIONS_P2_LEVEL_UP = 4;
    public static final double N_DAYS_LEVEL_UP = 0;
    public static final int HOUR_DAILY_RECAP = 10;
    public static final double N_DAYS_MONITORING_PHASE = 0.002;
    public static final float PERCENTAGE_MORE_THAN_AVERAGE = 0.2f;
*/
    public static final int MSEC_OF_POLLING = 5000;

    public static final int DAYS_TO_MSEC = 24*60*60*1000;
    public static final int HOUR_TO_MSEC = 3600000;
    public static final int MIN_TO_MSEC = 60000;
    public static final int SEC_TO_MSEC = 1000;

    public static final int PATH_UPLOAD = 1;
    public static final int INTERVENTION_RESULT_UPLOAD = 2;
    public static final int USAGE_UPLOAD = 3;
    public static final int CHAT_UPLOAD = 4;
    public static final int LEVEL_DOWN = 0;
    public static final int LEVEL_SAME = 1;
    public static final int LEVEL_UP = 2;

    public static final int MAX_DELAY_INT2 = 2;

    public static final int DOWN_AVG_SEC_DELAY = 60;
    public static final int UP_AVG_SEC_DELAY = 60;
    public static final double DOWN_RATIO_END_PEN = 0.5;
    public static final double UP_RATIO_END_PEN = 0.3;
    public static final double DOWN_RATIO_DEL_PEN = 0.4;
    public static final double UP_RATIO_DEL_PEN = 0.2;

    public static final String FIREBASE_CREATION = "CREATE";
    public static final String FIREBASE_FW_END = "FW_END";
    public static final String FIREBASE_LEVEL_CHANGE = "LEVEL_CHANGE";
    public static final String FIREBASE_PATH_END = "PATH_END";
    public static final String FIREBASE_INTERVENTION = "INTERVENTION_DONE";
    public static final String FIREBASE_DELETE = "PATH_REMOVED";
    public static final String FIREBASE_UPDATE = "PATH_UPDATED";

    public static final int PENDING_INTENT_ALARM_CODE = 123;
    public static final String DEFAULT_CHANNEL_ID = "default-channel";
    public static final String  FOREGROUND_CHANNEL_ID = "foreground-service-channel";
    public static final String INTERVENTION2_NOTIFICATION_ID = "intervention2-channel";
    public static final int DEFAULT_FOREGROUND = 110;
    public static final int INTERVENTION2_FOREGROUND = 112;

    public static final int NOTIFICATION_USAGE = 1;
    public static final int NOTIFICATION_INACTIVITY = 2;

    public static final boolean TODAY = true;
    public static final boolean TOMORROW = false;

    public static final int WA_THRESHOLD = 300;

    public static final int[] PERCENTAGES_L1_VIBRATION = {50, 75, 100, 125, 150, 175, 200};
    public static final int[] PERCENTAGES_L1_BRIGHTNESS = {70, 90, 100, 125, 150, 175, 200};
    public static final int[] PERCENTAGES_L2_VIBRATION = {75, 100, 150, 200};
    public static final int[] PERCENTAGES_L2_BRIGHTNESS = {90, 100, 150, 200};
    public static final int[] PERCENTAGES_L3_VIBRATION = {100};
    public static final int[] PERCENTAGES_L3_BRIGHTNESS = {100};

    public static final int FOREGROUND_LIMIT_DANGEROUS_APP = 20 * 60 * 1000;

    public static final int MAX_APPS_SUGGESTED = 3;
    public static final int PLURAL = 2;

    public static final String[] VIBRATION_APPS = {"Telegram", "WhatsApp", "Signal", "Gmail", "WeChat", "Libero Mail", "Microsoft Outlook"};
    public static final long FORWARDING_DELAY = 3000;
    public static final long RESTORE_BRIGHTNESS_DELAY = 2000;
    public static final long SEND_AGAIN_TO_CHATBOT_DELAY = 5000;

    public static final String[] usesOrNot = {"uses", "doesn't use"};
    public static final String[] hasOrHave = {"has", "have"};
    public static final String[] itOrThem = {"it", "them"};
    public static final String[] pathOrPaths = {"path", "paths"};
    public static final String[] brightnessOrVibration = {"brightness", "vibration", "brightness and vibration"};


}
