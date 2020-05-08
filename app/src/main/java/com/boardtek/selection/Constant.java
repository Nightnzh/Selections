package com.boardtek.selection;


import com.boardtek.appcenter.NetworkInformation;

public class Constant{

    private static String TAG = Constant.class.getSimpleName();

    public static int mode = 2;
    public static final int MODE_TEST = 3;
    public static final int MODE_OFFICIAL = 2;

    public static String lang;

    //切換WIFI 101.168 boardtek
    //         50.98 小米
    public static final String IP_BOARDTEK = "192.168.101.168";
    public static final String IP_XIAOMI = "192.168.50.98";
    public static final String GET_PARAMS_MODE_TEST = "dev,007459,500,laminationProgram,pp_program&";
    public static final String GET_PARAMS_MODE_OFFICIAL = "modules_mvc,500,laminationProgram,pp_program&";

    public final static String ACTION_GET_ALL = "action=mobile_programData_all";
    public final static String ACTION_BY_PROGRAM_ID = "action=mobile_programData";

    public static String getUrl(int mode,String action){
        String urlTemp;
        if(mode == MODE_OFFICIAL){
            urlTemp = "http://"+ NetworkInformation.actionIP +"/system_mvc/controller.php?s="+GET_PARAMS_MODE_OFFICIAL;
            switch (action){
                 case(ACTION_GET_ALL) :
                     return urlTemp+ACTION_GET_ALL;
                     case(ACTION_BY_PROGRAM_ID) :
                        return urlTemp+ACTION_BY_PROGRAM_ID;
                 default: return "";
            }
        }else if(mode == MODE_TEST){
            urlTemp = "http://"+ NetworkInformation.actionIP +"/system_mvc/controller.php?s="+GET_PARAMS_MODE_TEST;
            switch (action){
                case(ACTION_GET_ALL) :
                    return urlTemp+ACTION_GET_ALL;
                case(ACTION_BY_PROGRAM_ID) :
                    return urlTemp+ACTION_BY_PROGRAM_ID;
                default: return "";
            }
        }
        return "";
    }
}
//http://retek-06/system_mvc/controller.php?s=dev,007459,500,laminationProgram,pp_program&action=mobile_programData_all

/*
測 192.168.50.98
正 192.168.101.168
* */
