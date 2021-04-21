package com.demo.paywei.unit;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.demo.paywei.entity.AlipayConfig;
import com.demo.paywei.entity.Park;
import com.demo.paywei.entity.WinXin;
import com.demo.paywei.mapper.LogicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付宝
 */
@Controller
@RequestMapping("/alipay")
public class AlipayUtil {

    @Autowired
    private LogicMapper logicMapper;

    /**
     * web支付下单并支付(web支付在安卓中是可以直接唤醒支付宝APP的)
     *
     * @return web支付的表单
     */
    @RequestMapping("/webPay")
    @ResponseBody
    public String TradeWapPayRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        ServletContext application = request.getServletContext();
        String parkId = (String) session.getAttribute("parkId"); //获取停车场parkId
        Park park = logicMapper.getParkId(parkId); //根据传入的停车场parkId获取商户号与公众号信息
        //动态传入配置参数
        AlipayConfig.setAppId(park.getAlipayAppid()); //appid
        AlipayConfig.setAlipayPublicKey(park.getAlipayPublicKey()); //应用公钥
        AlipayConfig.setAppPrivateKey(park.getAlipayPrivateKey()); //应用私钥
        AlipayConfig.setSellerId(park.getSellerId()); //收款方账号
        System.err.println("appid:"+AlipayConfig.APP_ID);
        System.err.println("getAlipayPublicKey:"+AlipayConfig.ALIPAY_PUBLIC_KEY);
        System.err.println("getAlipayPrivateKey:"+AlipayConfig.APP_PRIVATE_KEY);
        System.err.println("getSellerId:"+AlipayConfig.SELLER_ID);


        float amount = (float)session.getAttribute("small_amount"); //(float)session.getAttribute("small_amount")
        System.err.println("转换后amount："+amount);
        String out_trade_no = getOrderIdByTime(); //生成订单号
        Map<String,Object> payMap = new HashMap<>();
        payMap.put("out_trade_no",out_trade_no);
        System.err.println("out_trade_no:"+out_trade_no);
        payMap.put("licenseUuid",session.getAttribute("licenseUuid"));
        payMap.put("license",session.getAttribute("license"));
        payMap.put("logUuid",session.getAttribute("logUuid"));
        payMap.put("parkUuid",session.getAttribute("parkUuid"));
        payMap.put("orgUuid",session.getAttribute("orgUuid"));
        payMap.put("small_amount",amount);

        //from支付表单
        Map<String, String> sParaTemp = new HashMap<>();
        //月租付款需要信息
        if(null != session.getAttribute("notify") && ("rentCar").equals(session.getAttribute("notify"))){
            payMap.put("uuid",session.getAttribute("uuid"));
            payMap.put("startDate", session.getAttribute("startDate"));
            payMap.put("newEndDate", session.getAttribute("newEndDate"));
            sParaTemp.put("subject", "月租费用支付");//订单标题
            sParaTemp.put("body", "月租费用");//没看到在哪显示了，搞了再说。
        }else{
            sParaTemp.put("subject", "停车费用支付");//订单标题
            // sParaTemp.put("product_code", "QUICK_WAP_PAY");//手机网页支付
            sParaTemp.put("body", "停车费用");//没看到在哪显示了，搞了再说。
        }
        application.setAttribute(out_trade_no, payMap);


        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.RETURN_URL);//前台回调地址
        alipayRequest.setNotifyUrl(AlipayConfig.PAY_NOTIFY);//成功付款回调
        // 待请求参数数组
        System.err.println("收款方账号:" + AlipayConfig.SELLER_ID);
        sParaTemp.put("seller_id", AlipayConfig.SELLER_ID);//收款方账号
        sParaTemp.put("out_trade_no", out_trade_no);//商家订单号(唯一）注意（Test）：这一单已付款，再掉起支付时会报此订单已支付。那么就得换个订单号，索性搞个生成订单号方法函数
        sParaTemp.put("total_amount",amount+"");//订单金额，精准到分

        alipayRequest.setBizContent(JSON.toJSONString(sParaTemp));
        System.err.println("alipayRequest:"+alipayRequest.toString());
        String form = "";
        try {
            form = AlipayConfig.getInstance().pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            System.err.println("支付宝构造表单失败" + e);
        }
        System.err.println("支付宝支付表单构造:" + form);
        return form;
    }

    /**
     * 生成订单号，高并发情况下可加入订单ID
     *
     * @return
     */
    public static String getOrderIdByTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String newDate = sdf.format(new Date());
        String result = "";
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            result += random.nextInt(10);
        }
        return newDate + result;
    }



}


