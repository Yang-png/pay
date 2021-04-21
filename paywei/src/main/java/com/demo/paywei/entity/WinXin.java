package com.demo.paywei.entity;

public class WinXin {

    public static String KEY = "06E86A1727F44449B8E4A81BFB63B36B"; //支付秘钥KEY

    public static String CODEURL = "https://open.weixin.qq.com/connect/oauth2/authorize?"; //获取code头部地址

    public static String APPID = "wx3ce43b03d856dc3f"; //开发者ID //wx3ce43b03d856dc3f

    public static String access_token = "https://api.weixin.qq.com/sns/oauth2/access_token?";

    public static String SECRET = "afa8911207002374ff234f3e4c370411"; //开发者密码

    public static String MCHID = "1565251541"; //商户号

    public static String UNURL = "https://api.mch.weixin.qq.com/pay/unifiedorder"; //统一下单API

    public static String getKEY() {
        return KEY;
    }

    public static void setKEY(String KEY) {
        WinXin.KEY = KEY;
    }

    public static String getCODEURL() {
        return CODEURL;
    }

    public static void setCODEURL(String CODEURL) {
        WinXin.CODEURL = CODEURL;
    }

    public static String getAPPID() {
        return APPID;
    }

    public static void setAPPID(String APPID) {
        WinXin.APPID = APPID;
    }

    public static String getAccess_token() {
        return access_token;
    }

    public static void setAccess_token(String access_token) {
        WinXin.access_token = access_token;
    }

    public static String getSECRET() {
        return SECRET;
    }

    public static void setSECRET(String SECRET) {
        WinXin.SECRET = SECRET;
    }

    public static String getMCHID() {
        return MCHID;
    }

    public static void setMCHID(String MCHID) {
        WinXin.MCHID = MCHID;
    }

    public static String getUNURL() {
        return UNURL;
    }

    public static void setUNURL(String UNURL) {
        WinXin.UNURL = UNURL;
    }
}
