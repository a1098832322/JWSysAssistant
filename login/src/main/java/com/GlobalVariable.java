package com;

/**
 * Created by 10988 on 2017/4/25.
 */

public class GlobalVariable {
    //屏幕高度
    public static int height = 720;

    //教务管理系统相关
    public static final String OneKeyUrl = "http://blog.summerpro.cn/jwsystem/GetPingJiao.php";
    public static final String ExamPicUrl = "http://blog.summerpro.cn/jwsystem/GetExamination.php?";
    public static final String GradePicUrl = "http://blog.summerpro.cn/jwsystem/GetGrade.php?";
    public static final String LoginUrl = "http://blog.summerpro.cn/jwsystem/Menu.php";
    public static final String VerificationCodeUrl = "http://blog.summerpro.cn/jwsystem/ValidateCode.php";

    //体侧相关

    // 身高体重等详细信息网址  直接跟学号
    public static final String URL_DETAIL = "http://1.jdwzshou.sinaapp.com/jdt/wx/tice/13/13_2.php?xuehao=";

    //体育成绩等  直接跟学号
    public static final String URL_DETAIL1 = "http://1.jdwzshou.sinaapp.com/jdt/wx/tice/13/13_3.php?xuehao=";

    // 体侧URL 需要POST数据，直接返回13_1.php，可直接解析
    public static final String URL_SPORT_SCOLLER = "http://1.jdwzshou.sinaapp.com/jdt/wx/tice/13/test.php";
}
