package com.samratinfosys.myecollege.tools;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */
public final class GeneralConstants {
    public static final String SERVER="http://192.168.123.100/";
    public static final String API_HEAD=SERVER+"cpu/";
    public static final String API_COLLEGES=API_HEAD+"get-data/colleges";
    public static final String API_LOGIN=API_HEAD+"login";
    public static final String API_REQUESTCODE=API_HEAD+"activation/request-code";
    public static final String API_ACTIVATENOW=API_HEAD+"activation/activate-now";
    public static final String API_FORGOT_PASSWORD=API_HEAD+"account/forgot-password";
    public static final String API_NETWORK_PARAMETERS=API_HEAD+"get-data/network-parameters";

    public static final String API_VERIFY_DEVICE=API_HEAD+"login/verifyDevice";

    public static final String API_GET_COLLEGE_POSTERIMAGE=API_HEAD+"get-data/college-logo/poster-image";

    public static final String API_TIMELINE=API_HEAD+"get-data/timelines";

    //////////////////////////////  Result Codes

    public static final int RESULT_FROM_LOGIN=0;
    public static final int RESULT_FROM_MYFRIENDS=1;

    public static final String RQ_CODE_COLLEGESELECTION_IsFromBoot="IsItFromBootActivity";
    public static final int RQ_CODE_FOR_MYPROFILE=1;
    public static final int RQ_CODE_FOR_SELECTIMAGE=2;


    //////////////////////////////  Result Codes

    //********//

    public static final String API_IMAGES=API_HEAD+"get-data/images";
    public static final String API_PROFILE=API_HEAD+"get-data/profiles";
    public static final String API_UPDATE=API_HEAD+"update";
    public static final String API_LOADUSERS=API_HEAD+"get-data/users";

}
