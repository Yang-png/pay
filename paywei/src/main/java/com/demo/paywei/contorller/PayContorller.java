package com.demo.paywei.contorller;

import com.demo.paywei.entity.Park;
import com.demo.paywei.entity.WinXin;
import com.demo.paywei.mapper.LogicMapper;
import com.demo.paywei.unit.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Controller
public class PayContorller {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LogicMapper logicMapper;


    Md5Util md5 = new Md5Util();

    @RequestMapping(value = "payWin.html")
    public String payHtml() {
        return "payWin";
    }

    @RequestMapping(value = "rentCarIndex.html")
    public String rentCarIndex() {
        userAgent();
        return "rentCarIndex";
    }

    public String userAgent(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        String userAgent = request.getHeader("user-agent");
        if (userAgent != null && userAgent.contains("MicroMessenger")){
            session.setAttribute("merchants","微信");
            System.out.println("微信支付");
        }else if (userAgent !=null && userAgent.contains("AlipayClient")){
            System.out.println("支付宝支付");
            session.setAttribute("merchants","支付宝");
        }
        return null;
    }

    @RequestMapping(value = "rentCar.html")
    public String rentCar() {
        return "rentCar";
    }

    @RequestMapping(value = "xx")
    @ResponseBody
    public boolean xx() {
        String a = "3.00";
        float b = 3.0f;
        BigDecimal a1 = new BigDecimal(String.valueOf(a));
        float a2 = a1.floatValue();
        return b==a2;
    }



    @RequestMapping(value = "login")
    public String cs() {
        return "login";
    }

    @RequestMapping(value = "couponsIndex")
    public String couponsIndex() {
        return "couponsIndex";
    }

    @RequestMapping(value = "notfiy.html")
    public String notfiy() {
        return "notfiy";
    }



    @RequestMapping(value = "/")
    public String index() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        String userAgent = request.getHeader("user-agent");
        if (userAgent != null && userAgent.contains("MicroMessenger")){
            session.setAttribute("merchants","微信");
            System.out.println("微信支付");
        }else if (userAgent !=null && userAgent.contains("AlipayClient")){
            System.out.println("支付宝支付");
            session.setAttribute("merchants","支付宝");
        }
        System.out.println(userAgent);
        return "index";
    }

    //生成签名
    @RequestMapping(value = "getSign")
    @ResponseBody
    public String getSign(Map<String, Object> arr) {

       /* arr.put("appid", "dfggg");
        arr.put("mch_id", "sdgfgd");
        arr.put("body", "88333黎明互联");*/
        //去除空值
        arr = md5.removeMapEmptyValue(arr);


        //按照key排序
        TreeMap<String, Object> map = new TreeMap<>(arr);

        //若存在签名则删除签名再生成签名
        if (map.get("sign") != null) {
            map.remove("sign");
        }

        //转换url
        String url = md5.asUrlParams(map);

        //拼接商户key
        url += "&key=" + WinXin.getKEY();
        System.err.println("url:"+url);
        //生成签名
        System.err.println("签名:" + md5.encode(url));
        return md5.encode(url);
    }

    //验证签名
    @RequestMapping(value = "chekSign")
    @ResponseBody
    public Boolean chekSign(Map<String, Object> arr) {
        String sign = getSign(arr); //获取传入的数据生成签名
        //再用生成的签名与传入的数据中的签名做比较 判断是否一致
        if (sign.equals(arr.get("sign"))) {
            return true;
        }
        return false;
    }

    //传入待签名的集合ch,生成签名后返回
    @RequestMapping(value = "setSign")
    @ResponseBody
    public Map<String, Object> setSign(Map<String, Object> arr) {
        arr.put("sign", getSign(arr));
        return arr;
    }


    /**
     * 获取用户openid
     * 构建原始数据
     * 加入签名
     * 调用用一下单API
     * 获取到prepay_id
     */
    @RequestMapping(value = "pay.html")
    public Object test() throws Exception {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        HttpSession session = request.getSession();


        String parkId = (String) session.getAttribute("parkId"); //获取停车场parkId
        Park park = logicMapper.getParkId(parkId); //根据传入的停车场parkId获取商户号与公众号信息

        //动态传入配置参数
        WinXin.setAPPID(park.getWpAppid()); //公众号id
        WinXin.setSECRET(park.getWpAppsecret()); //公众号秘钥
        WinXin.setMCHID(park.getWpMrchid()); //商户号id
        WinXin.setKEY(park.getWpApicert()); //商户号秘钥

        //访问统一下单方法得到prepayid,传入自定义订单号
        String prepay_id = (String) md5.getPrePayId(request, response, UUID.randomUUID().toString().replace("-", "").toUpperCase());
        return md5.getJsParams(prepay_id);

    }



}
