package com.demo.paywei.unit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.demo.paywei.contorller.PayContorller;
import com.demo.paywei.config.HttpsRequest;
import com.demo.paywei.config.XmlMap;
import com.demo.paywei.entity.WinXin;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.google.common.base.Joiner;
import org.apache.http.client.methods.HttpOptions;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;

public class Md5Util {

    Date date = new Date();

    public static String small_amountStr = "";
    public static String parkUuid = "";
    public static int small_amount = 0;


    //加密时间
    public static String MD5Time(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            char[] charArray = inStr.toCharArray();
            byte[] byteArray = new byte[charArray.length];

            for (int i = 0; i < charArray.length; i++)
                byteArray[i] = (byte) charArray[i];

            byte[] md5Bytes = md5.digest(byteArray);

            StringBuffer hexValue = new StringBuffer();

            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }

            return hexValue.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
    }


    //生成签名
    public final static String encode(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    //map转换url
    public static String asUrlParams(Map<String, Object> source) {
        // TODO 如果要编码的话自己加下编码逻辑
        return Joiner.on("&")
                // 用指定符号代替空值,key 或者value 为null都会被替换
                .useForNull("")
                .withKeyValueSeparator("=")
                .join(source);
    }

    //map生成xml
    public static String getXml(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (String k : map.keySet()) {
            Object value = map.get(k);
            sb.append("<" + k + ">" + value + "</" + k + ">");
        }
        sb.append("</xml>");
        try {
            return new String(sb.toString().getBytes(), "UTF-8");//ISO8859-1
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    //删除value为空的数据
    public Map<String, Object> removeMapEmptyValue(Map<String, Object> paramMap) {
        Set<String> set = paramMap.keySet();
        Iterator<String> it = set.iterator();
        List<String> listKey = new ArrayList<String>();
        while (it.hasNext()) {
            String str = it.next();
            if (paramMap.get(str) == null || "".equals(paramMap.get(str))) {
                listKey.add(str);
            }
        }
        for (String key : listKey) {
            paramMap.remove(key);
        }
        return paramMap;
    }


    //根据url获取json数据
    public String loadJson(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) urlObject
                    .openConnection();
            int contentLength = uc.getContentLength();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc
                    .getInputStream(), "utf-8"));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
            uc.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    //获取openid
    public Object getOpenId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        HttpSession session = request.getSession();
        //1.用户访问一个地址 先获取到code
        //2.根据code获取到openid
        if (request.getParameter("code") == null) {
            StringBuffer redUrl = request.getRequestURL(); //获取当前访问地址
            String encode = null;
            try {
                encode = URLEncoder.encode("http://www.shmuyun.cn/payweiNew/pay.html", "UTF-8"); //redirect_uri地址转码  pay.html
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = WinXin.CODEURL + "appid=" + WinXin.APPID + "&redirect_uri=" + encode + "&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
            System.err.println("url:" + url);
            System.err.println("redirect_url:" + encode);
            // /构建跳转地址 跳转
            try {
                response.sendRedirect(url); //访问地址获取code
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //调用接口获取openid
            String code = request.getParameter("code");
            System.err.println("code:" + code);
            String openidurl = WinXin.access_token + "appid=" + WinXin.APPID + "&secret=" + WinXin.SECRET + "&code=" + code + "&grant_type=authorization_code";
            String json = loadJson(openidurl); //请求地址获取json
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> m = new HashMap<>();
            try {
                m = mapper.readValue(json, Map.class); //json转换map
            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            session.setAttribute("openid", m.get("openid"));
            System.err.println("openid:" + m.get("openid"));

            return m.get("openid");
        }
        return 1;
    }

    //调用统一下单api
    public Map<String, Object> unifiedOrder(HttpServletRequest request, HttpServletResponse response, String out_trade_no) {

        HttpSession session = request.getSession();
        float amountx = (float) session.getAttribute("small_amount");
        int amount = (int) (amountx * 100);
        String timestamp = String.valueOf(date.getTime() / 1000); //当前时间戳

        //构建原始数据（需要openid） 加入签名  调用接口将数据转换成xml  发送xml的数据到接口地址
        System.err.println("appid:" + WinXin.getAPPID());
        System.err.println("SECRET:" + WinXin.getSECRET());
        System.err.println("MCHID:" + WinXin.getMCHID());
        System.err.println("key:" + WinXin.getKEY());

        Map<String, Object> params = new HashMap<>();
        params.put("appid", WinXin.getAPPID()); //公众号id
        params.put("mch_id", WinXin.getMCHID());//商户id
        params.put("nonce_str", WXPayUtil.generateNonceStr()); //加密

        params.put("out_trade_no", out_trade_no); //内部订单号
        params.put("total_fee", amount); //总价  单位为分
        params.put("spbill_create_ip", "223.72.97.105"); //客户端ip

        if (session.getAttribute("notify") != null && "pay".equals(session.getAttribute("notify"))) {  //付款回调
            params.put("body", "停车费"); //商品描述
            params.put("notify_url", "http://www.shmuyun.cn/payweiNew/paySuccess"); //支付完成后通知url
        } else if (session.getAttribute("notify") != null && "rentCar".equals(session.getAttribute("notify"))) {  //月租支付回调
            params.put("body", "月租费用"); //商品描述
            params.put("notify_url", "http://www.shmuyun.cn/payweiNew/rentCarSuccess"); //支付完成后通知月租url
        } else {
            params.put("body", "停车费"); //商品描述
            params.put("notify_url", "http://www.shmuyun.cn/payweiNew/paySuccess"); //支付完成后通知url
        }

        params.put("trade_type", "JSAPI"); //支付类型
        params.put("product_id", out_trade_no); //产品ID/订单号 "4200000576202007086639838888"
        params.put("openid", getOpenId()); //访问openid方法获取openid


        PayContorller wp = new PayContorller();


        //交易完成后需要添加的参数
        Map<String, Object> payResp = new HashMap<>();
        payResp.put("transaction_id", params.get("product_id"));
        payResp.put("nonce_str", params.get("nonce_str"));
        payResp.put("bank_type", "OTHERS");
        payResp.put("openid", params.get("openid"));
        payResp.put("sign", params.get("sign"));
        payResp.put("fee_type", "CNY");
        payResp.put("mch_id", params.get("mch_id"));
        payResp.put("cash_fee", params.get("total_fee"));
        payResp.put("out_trade_no", params.get("out_trade_no"));
        payResp.put("appid", params.get("appid"));
        payResp.put("total_fee", params.get("total_fee"));
        payResp.put("trade_type", "JSAPI");
        payResp.put("result_code", "SUCCESS");
        payResp.put("is_subscribe", "Y");
        payResp.put("return_code", "SUCCESS");
        session.setAttribute("payResp", payResp);

        params = wp.setSign(params); //加入签名

        String xmldata = getXml(params);  //将数据转换成xml
        System.err.println("统一下单发送xml数据:" + xmldata);
        Map<String, Object> map = new HashMap<>();
        try {
            String result = new HttpsRequest().sendPost(WinXin.UNURL, xmldata); //访问统一下单接口
            map = XmlMap.xml2Map(result);
            System.err.println("统一下单接口返回result:" + result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        return map;
    }

    //获取prepayid
    public Object getPrePayId(HttpServletRequest request, HttpServletResponse response, String out_trade_no) throws Exception {
        ServletContext application = request.getServletContext();
        HttpSession session = request.getSession();
        float amountx = (float) session.getAttribute("small_amount");
        small_amount = (int) (amountx * 100);
        Map<String, Object> payMap = new HashMap<>();
        payMap.put("licenseUuid", session.getAttribute("licenseUuid"));
        payMap.put("license", session.getAttribute("license"));
        payMap.put("logUuid", session.getAttribute("logUuid"));
        payMap.put("parkUuid", session.getAttribute("parkUuid"));
        payMap.put("orgUuid", session.getAttribute("orgUuid"));
        payMap.put("small_amount", small_amount);
        payMap.put("small_amountStr", small_amount + "");

        //月租付款需要信息
        if(null != session.getAttribute("notify") && ("rentCar").equals(session.getAttribute("notify"))){
            payMap.put("uuid", session.getAttribute("uuid"));
            payMap.put("startDate", session.getAttribute("startDate"));
            payMap.put("newEndDate", session.getAttribute("newEndDate"));
        }

        application.setAttribute(out_trade_no, payMap); //以订单号做标识存入application中 回调时拿出

        //得到访问统一下单后返回的map集合数据
        Map<String, Object> map = unifiedOrder(request, response, out_trade_no);
        return map.get("prepay_id");

    }

    //获取公众号支付所需要的的json数据
    public String getJsParams(String prepay_id) throws Exception {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        HttpSession session = request.getSession();

        PayContorller wp = new PayContorller();
        String timestamp = String.valueOf(date.getTime() / 1000); //当前时间戳

        Map<String, Object> params = new HashMap<>();
        params.put("appId", WinXin.APPID);
        params.put("timeStamp", timestamp); //时间
        params.put("nonceStr", WXPayUtil.generateNonceStr()); //随机字符串
        params.put("package", "prepay_id=" + prepay_id); //prepay_id
        params.put("signType", "MD5"); //签名方式

        String paySign = wp.getSign(params);
        System.err.println("公众号支付签名:" + paySign);
        params.put("paySign", paySign);//生成签名
        session.setAttribute("params", params);
        return "payWin";
    }


}