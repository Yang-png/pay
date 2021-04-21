package com.demo.paywei.entity;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;

public class AlipayConfig {

    /**
     * 应用ID号（）
     */
    public static String APP_ID = "2021001194640004";//你的应用appId

    /**
     * 你的私钥
     */
    public static String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcroFiFFFT9bRsmrTmJn8oQqsv1wjeO2unQnQuiQO9KQQFxYHkfxvb97W5T2wLrF+mhRddkYDUCmXS/UtNQkO2eTPjeE4rPYGteVoVVmUvcA5mxX+0w+rBAsttQTob1bHCM67MhNoNUKlYnHExIBUfD3auL5h3tVmkcYNR0+6WiOs98q/APhJRIMapMs3Nm8QevbsDdiCfB+M9XATCUqjVzSTHJtCbkXNqE8xiBJS9vppqOTqkHfaxqTMxcskJYfaQgWJR09GrPeP3kmIQSGXaeO0moLOuFwQu9vigC76nHUC/2PLsiLSSuEKgX1QBjyA0RmTluZ37dWX4139M80bbAgMBAAECggEAFJbJ4b2RwlqzTuI1i1jbtMqt/CdhvvJH/z64TbUrjJvrL2b43rHlU06FzsglBpOsLhGnNGB+cV68arFmR8zBYSUkMGwKuZvngohD0jH8MfOjsNWbJoHoQ1y+pqi/nhDCvyZTfgSnMsC6wC4uoan5nI9HpTQT8QcftIBGJWaYH6fAxM0LJDnYhqq3CMv+eGiiAYAxhj/20FW4og+1JTncQNa20Di1nZ95lgW/0g/6+4jZsl38sXnuLqNRGqXRWraXCSnHtReojty+nyuNXMXbE9sL+CI+xmv67eNHPSkuKVBNX4rf67IFKn8Kjk37MARRC2iYHL+aHlapfEb2cazTwQKBgQDyxzolkFJf7oUxbQWLDoJ+2dkm+i+8yc0M/l8b1YpA6aP2DGmPmTcPBBjpyA/MYYWIhQZ2FWqDNnHXA4VudzPiWWlGgJfc3BtdbKSA1FbWmyJqFmoZ4RxlrgHoQZCIc+sVD2iHri0vl3VTM1HwUc9ThnvpCoogGeVsAavEMJ7DeQKBgQClNu88vjghMDcPAdLqY5HEbszyBDybtXdy98Ef8x1CYzbd6A5KsKI7zsELCMfOUeIdVVWEAjKRyO9P37hxBy7b+yRy43ICK5p0lzsIWAc0Wp9nvWPvgrN5eBfAGI2pdmwdqcXtP30kxd4S0b92iGgixf+l6ILgg3x7PttFYHvT8wKBgEwQOVqNhhGCVH6wzpzLtfzyWgPJPO2kODdK9xdsZv2Bdhpfty8DbOmqefe8854wK7sjGmwit5VWmIIqG0MJ29qd8srg/TN4TqaNpFisrmxcfILKdQGYsuH0i6pt6E+oKc4WXsSrwIJ5rIudt51YtGFR8D6KS85U7rlRKLS2GvfZAoGBAJIvHVfd58RV6SG6N2l1WJE6iNTLsuUDmAF9/r/KWWVYbD/FCIBF8gaBVoXw6vo/sZbK3rPDTgN4i2ddSNGZzXiPqxyM61/IPQ5UkcM8QIOue8VK6pYOS8d632j1UMNVKSzF/3+A4bYb7fcw5/x1jc7W6vFd3g/Rx981KsckK+zJAoGAXFTCFD1aPHvWIO6hfEChKxNo6OLtrhy1FYU1v9mp4cO74FkV8xn5wghoog3WtsHKFK3qWIO9Jy0HbzcFcqog6jw8HdyEuw2D1V+aqkfLMx3CixS45E6+bocQGwNzn5FyRRUU/5FITPPgGewfrH66Bg9RF0UnYt+JXV7zOISexsY=";

    /**
     * 编码
     */
    public static String CHARSET = "UTF-8";

    /**
     * 支付宝公钥
     */
    public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnK6BYhRRU/W0bJq05iZ/KEKrL9cI3jtrp0J0LokDvSkEBcWB5H8b2/e1uU9sC6xfpoUXXZGA1Apl0v1LTUJDtnkz43hOKz2BrXlaFVZlL3AOZsV/tMPqwQLLbUE6G9WxwjOuzITaDVCpWJxxMSAVHw92ri+Yd7VZpHGDUdPulojrPfKvwD4SUSDGqTLNzZvEHr27A3YgnwfjPVwEwlKo1c0kxybQm5FzahPMYgSUvb6aajk6pB32sakzMXLJCWH2kIFiUdPRqz3j95JiEEhl2njtJqCzrhcELvb4oAu+px1Av9jy7Ii0krhCoF9UAY8gNEZk5bmd+3Vl+Nd/TPNG2wIDAQAB";

    /**
     * 支付宝网关地址
     */
    private static String GATEWAY = "https://openapi.alipay.com/gateway.do";//正式环境

    //private static String GATEWAY = "https://openapi.alipay.com/gateway.do";//沙箱环境

    /**
     * 成功付款回调
     */
    public static String PAY_NOTIFY = "http://www.shmuyun.cn/payweiNew/payAli";//验签

    /**
     * 支付成功回调
     */
    public static String REFUND_NOTIFY = "";//姑且没用到

    /**
     * 前台通知地址
     */
    public static String RETURN_URL = "";//完成
    /**
     * 参数类型
     */
    public static String PARAM_TYPE = "json";
    /**
     * 成功标识
     */
    public static final String SUCCESS_REQUEST = "TRADE_SUCCESS";
    /**
     * 交易关闭回调(当该笔订单全部退款完毕,则交易关闭)
     */
    public static final String TRADE_CLOSED = "TRADE_CLOSED";

    /**
     * 支付宝开发平台中的支付宝账号（企业）
     */
    public static String SELLER_ID = "yuefang1070@163.com"; //shmuyun

    //签名算法类型(根据生成私钥的算法,RSA2或RSA)
    public static final String SIGNTYPE = "RSA2";

    /**
     * 不可实例化
     */
    private AlipayConfig() {
    }

    ;

    public static String getAppId() {
        return APP_ID;
    }

    public static void setAppId(String appId) {
        APP_ID = appId;
    }

    public static String getAppPrivateKey() {
        return APP_PRIVATE_KEY;
    }

    public static void setAppPrivateKey(String appPrivateKey) {
        APP_PRIVATE_KEY = appPrivateKey;
    }

    public static String getAlipayPublicKey() {
        return ALIPAY_PUBLIC_KEY;
    }

    public static void setAlipayPublicKey(String alipayPublicKey) {
        ALIPAY_PUBLIC_KEY = alipayPublicKey;
    }

    public static String getSellerId() {
        return SELLER_ID;
    }

    public static void setSellerId(String sellerId) {
        SELLER_ID = sellerId;
    }




    /**
     * 双重锁单例
     *
     * @return 支付宝请求客户端实例
     */
    public static AlipayClient getInstance() {
         AlipayClient alipayClient = null;
            synchronized (AlipayConfig.class) {
                alipayClient  = new DefaultAlipayClient(GATEWAY, AlipayConfig.APP_ID, AlipayConfig.APP_PRIVATE_KEY, PARAM_TYPE, CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, SIGNTYPE);
            }
        return alipayClient;
    }

}

    /**
     * 支付宝请求客户端入口
     */
    /*  private  volatile static AlipayClient alipayClient = null;*/

 /*   public static AlipayClient getInstance() {
        if (alipayClient == null) {
            synchronized (AlipayConfig.class) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(GATEWAY, AlipayConfig.APP_ID, AlipayConfig.APP_PRIVATE_KEY, PARAM_TYPE, CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, SIGNTYPE);
                }
            }
        }
        return alipayClient;
    }*/

