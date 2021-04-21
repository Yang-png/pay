package com.demo.paywei.contorller;

import com.demo.paywei.entity.HandCar;
import com.demo.paywei.entity.PayLog;
import com.demo.paywei.mapper.LogicMapper;
import com.demo.paywei.unit.Md5Util;
import com.demo.paywei.unit.RedisUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

@Controller
public class ReturnContorller {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LogicMapper logicMapper;

    @Autowired
    private RedisUtil redisUtil;


    //获取从post传过来的数据
    public static Map<String, Object> getPost() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 解析结果存储在HashMap
        Map<String, Object> map = new HashMap<String, Object>();
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元du素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        for (Element e : elementList)
            map.put(e.getName(), e.getText());

        // 释放资源
        inputStream.close();
        inputStream = null;

        return map;
    }


    /**
     * 1.支付完成后获取通知数据 -> 转换为数组
     * 2.验证签名
     * 3.验证业务结构（retrun_code 和 result_code）
     * 4.验证订单号和金额（out_trade_no total_fee）
     * 5.记录日志 修改订单状态
     */
    @RequestMapping(value = "paySuccess")
    @ResponseBody
    public Object paySuccess() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        PayContorller wp = new PayContorller();
        Map<String, Object> params = new HashMap<>();
        params.put("return_code", "SUCCESS");
        params.put("return_masg", "OK");
        Map<String, Object> map = getPost(); //获取支付后的接收数据

        Iterator<String> it = map.keySet().iterator();  //map.keySet()得到的是set集合，可以使用迭代器遍历
        while (it.hasNext()) {
            String key = it.next();
            System.out.println("key值：" + key + " value值：" + map.get(key));
        }
        String sign = (String) map.get("sign");

        ServletContext application = request.getServletContext();
        if (sign.equals(wp.getSign(map))) { //验证签名
            if ("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))) {
                //获取订单号
                Map<String, Object> payMap = (Map) application.getAttribute((String) map.get("out_trade_no"));
                System.err.println("金额:" + payMap.get("small_amount") + "--微信返回金额:" + map.get("total_fee"));
                if (map.get("total_fee").equals((String) payMap.get("small_amountStr"))) {
                    //对比支付金额是否一致 生产环境下需要获取相应价格  单位为分
                    System.err.println("交易成功");
                    Date date = new Date();
                    PayLog payLog = new PayLog();
                    payLog.setLogUuid((String) payMap.get("logUuid"));
                    payLog.setLicense((String) payMap.get("license"));
                    payLog.setParkUuid((String) payMap.get("parkUuid"));
                    payLog.setOrgUuid((String) payMap.get("orgUuid"));
                    payLog.setAmount((int) payMap.get("small_amount"));
                    long payTime = date.getTime() / 1000; //获取当前时间戳/1000换算时不要转int 换算完后再转
                    payLog.setPayTime((int) payTime);
                    payLog.setUuid(UUID.randomUUID().toString().replace("-", "").toUpperCase());
                    payLog.setPayMethod(6);


                    //更新进出记录
                    logicMapper.updateCarLog(payLog);

                    //更新在场车辆线上支付
                    logicMapper.updateCarLogPresent(payLog);

                    //添加付款记录
                    logicMapper.addPayLog(payLog);
                    System.err.println("logUuid：" + payMap.get("logUuid") + ("--license") + "license：" + payMap.get("license"));

                    //追加缓存付款信息
                    try {
                        if (redisUtil.get((String) payMap.get("parkUuid") + payMap.get("licenseUuid")) != null) {

                            //获取缓存
                            HandCar handCar= (HandCar)JSONObject.toBean(JSONObject.fromObject(redisUtil.get((String) payMap.get("parkUuid") + payMap.get("licenseUuid"))),HandCar.class);
                            handCar.setPayTime((int)payTime);

                            //更新付款时间重新插入缓存
                            JSONObject handCarJson = JSONObject.fromObject(handCar);
                            redisUtil.set((String) payMap.get("parkUuid") + payMap.get("licenseUuid"),handCarJson.toString(),604800);
                            logger.info(payMap.get("license")+"微信支付存储Redis -> "+payMap.get("parkUuid") + payMap.get("licenseUuid"));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //删除订单号对应的数据map集合
                    application.removeAttribute((String) map.get("result_code"));
                } else {
                    System.err.println("金额有误");
                    System.err.println("logUuid：" + payMap.get("logUuid") + ("--license") + "license：" + payMap.get("license"));
                }
            } else {
                System.err.println("业务结果不正确");
            }
        } else {
            System.err.println("签名失败！");
        }

        return Md5Util.getXml(params);
    }


    /**
     * 1.支付完成后获取通知数据 -> 转换为数组
     * 2.验证签名
     * 3.验证业务结构（retrun_code 和 result_code）
     * 4.验证订单号和金额（out_trade_no total_fee）
     * 5.记录日志 修改订单状态
     */
    @RequestMapping(value = "rentCarSuccess")
    @ResponseBody
    public Object rentCarSuccess() throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        Map<String, Object> params = new HashMap<>();
        PayContorller wp = new PayContorller();

        params.put("return_code", "SUCCESS");
        params.put("return_masg", "OK");
        Map<String, Object> map = getPost(); //获取支付后的接收数据

        Iterator<String> it = map.keySet().iterator();  //map.keySet()得到的是set集合，可以使用迭代器遍历
        while (it.hasNext()) {
            String key = it.next();
            System.out.println("key值：" + key + " value值：" + map.get(key));
        }
        String sign = (String) map.get("sign");

        ServletContext application = request.getServletContext();
        if (sign.equals(wp.getSign(map))) { //验证签名
            if ("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))) {
                //获取订单号
                Map<String, Object> payMap = (Map) application.getAttribute((String) map.get("out_trade_no"));
                System.err.println("月租车支付：金额:" + payMap.get("small_amount") + "--微信返回金额:" + map.get("total_fee"));
                if (map.get("total_fee").equals((String) payMap.get("small_amountStr"))) {
                    //对比支付金额是否一致 生产环境下需要获取相应价格  单位为分
                    System.err.println("月租支付交易成功");
                    Date date = new Date();
                    PayLog payLog = new PayLog();
                    payLog.setUuid((String) payMap.get("uuid"));
                    payLog.setCarUuid(UUID.randomUUID().toString().replace("-", "").toUpperCase());
                    payLog.setStartDate((Date) payMap.get("startDate"));
                    payLog.setEndDate((Date) payMap.get("newEndDate"));
                    payLog.setLicense((String) payMap.get("license"));
                    payLog.setParkUuid((String) payMap.get("parkUuid"));
                    payLog.setOrgUuid((String) payMap.get("orgUuid"));
                    payLog.setAmount((int) payMap.get("small_amount"));
                    payLog.setPayMethod(3);
                    payLog.setWorkerUuid("dxy"); //自助
                    long payTime = date.getTime() / 1000; //获取当前时间戳/1000换算时不要转int 换算完后再转
                    payLog.setPayTime((int) payTime);


                    //添加月租车续约付款记录
                    logicMapper.addRentCar(payLog);

                    //更新月租车充值记录
                    logicMapper.updateRentCar(payLog);

                    application.removeAttribute((String) map.get("result_code")); //删除订单号对应的数据map集合
                } else {
                    System.err.println("金额有误");
                }
            } else {
                System.err.println("业务结果不正确");
            }
        } else {
            System.err.println("签名失败！");
        }

        return Md5Util.getXml(params);
    }


    /**
     * 支付宝支付后验签
     *
     * @return
     */
    @RequestMapping(value = "payAli")
    public String payAli() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        ServletContext application = request.getServletContext();
        System.err.println("进入验签");
        HttpSession session = request.getSession();
        //从支付宝回调的request域中取值
        Map<String, String[]> aliParams = request.getParameterMap();
        //用以存放转化后的参数集合
        Map<String, String> paramMap = new HashMap<String, String>();
        System.err.println("支付宝支付后返回参数：");
        for (Iterator<String> iter = aliParams.keySet().iterator(); iter.hasNext(); ) {
            String key = iter.next();
            String[] values = aliParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 解决乱码
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "uft-8");
            paramMap.put(key, valueStr);
            System.err.println(key + " : " + valueStr);
        }

        Cookie cookie;
        /* try {
         *//* //拼接待加签的字符串
            String content = AlipaySignature.getSignContent(paramMap);
            System.out.println("拼接待加签的字符串 : " + content);
            //私钥-对数据加签
            String sign = AlipaySignature.rsaSign(content, AlipayConfig.APP_PRIVATE_KEY, "UTF-8", "RSA2");*//*

            //公钥-对数据验签
            boolean result = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.ALIPAY_PUBLIC_KEY, "UTF-8", "RSA2");
            System.out.println("验签结果 : " + result);


            if (result) {*/
        //订单金额:本次交易支付的订单金额，单位为人民币（元）
        String totalAmountStr = paramMap.get("total_amount");
        BigDecimal totalAmountBig = new BigDecimal(String.valueOf(totalAmountStr));
        float totalAmount = totalAmountBig.floatValue();
        System.err.println("支付宝付款返回的金额转换为float:" + totalAmount);
        //商户系统的唯一订单号
        String out_trade_no = paramMap.get("out_trade_no");
        System.err.println(application.getAttribute(out_trade_no));
        //获取map集合
        Map<String, Object> payMap = (Map) application.getAttribute(out_trade_no);
        if (out_trade_no.equals((String) payMap.get("out_trade_no")) && totalAmount == (float) payMap.get("small_amount")) {
            cookie = new Cookie("ins", "支付成功");
            //1.更新付款时间  2.添加付款记录
            Date date = new Date();
            PayLog payLog = new PayLog();

            //普通支付回调
            if (null == payMap.get("newEndDate") && null == payMap.get("startDate")) {
                payLog.setLogUuid((String) payMap.get("logUuid"));
                payLog.setLicense((String) payMap.get("license"));
                payLog.setParkUuid((String) payMap.get("parkUuid"));
                payLog.setOrgUuid((String) payMap.get("orgUuid"));
                float amountx = (float) payMap.get("small_amount");
                payLog.setAmount((int) (amountx * 100));
                long payTime = date.getTime() / 1000; //获取当前时间戳/1000换算时不要转int 换算完后再转
                payLog.setPayTime((int) payTime);
                payLog.setUuid(UUID.randomUUID().toString().replace("-", "").toUpperCase());
                System.err.println("uuid:" + payLog.getUuid() + "logUuid:" + payLog.getLogUuid() + "license:" + payLog.getLicense() + "orgUuid:" + payLog.getOrgUuid() + "amount:" + payLog.getAmount());


                payLog.setTradeNo("支付宝");
                payLog.setPayResp("支付宝");

                payLog.setPayMethod(3);

                //更新进出记录
                logicMapper.updateCarLog(payLog);

                //更新在场车辆线上支付
                logicMapper.updateCarLogPresent(payLog);

                //添加付款记录
                logicMapper.addPayLog(payLog);

                //追加缓存付款信息
                try {
                    if (redisUtil.get((String) payMap.get("parkUuid") + payMap.get("licenseUuid")) != null) {
                        //获取缓存
                        HandCar handCar= (HandCar)JSONObject.toBean(JSONObject.fromObject(redisUtil.get((String) payMap.get("parkUuid") + payMap.get("licenseUuid"))),HandCar.class);
                        handCar.setPayTime((int)payTime);

                        //更新付款时间重新插入缓存
                        JSONObject handCarJson = JSONObject.fromObject(handCar);
                        redisUtil.set((String) payMap.get("parkUuid") + payMap.get("licenseUuid"),handCarJson.toString(),604800);
                        logger.info(payMap.get("license")+"支付宝支付存储Redis -> "+payMap.get("parkUuid") + payMap.get("licenseUuid"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else { //月租回调
                System.err.println("月租支付交易成功");
                payLog.setUuid((String) payMap.get("uuid"));
                payLog.setCarUuid(UUID.randomUUID().toString().replace("-", "").toUpperCase());
                payLog.setStartDate((Date) payMap.get("startDate"));
                payLog.setEndDate((Date) payMap.get("newEndDate"));
                payLog.setLicense((String) payMap.get("license"));
                payLog.setParkUuid((String) payMap.get("parkUuid"));
                payLog.setOrgUuid((String) payMap.get("orgUuid"));
                float amountx = (float) payMap.get("small_amount");
                payLog.setAmount((int) (amountx * 100));
                payLog.setPayMethod(6);
                payLog.setWorkerUuid("dxy"); //自助
                long payTime = date.getTime() / 1000; //获取当前时间戳/1000换算时不要转int 换算完后再转
                payLog.setPayTime((int) payTime);

                //添加月租车续约付款记录
                logicMapper.addRentCar(payLog);

                //更新月租车充值记录
                logicMapper.updateRentCar(payLog);

            }
            //回调关闭删除订单号
            application.removeAttribute((String) payMap.get("out_trade_no")); //删除订单号对应的数据map集合
        } else {
            System.err.println("业务匹配错误");
            cookie = new Cookie("ins", "业务匹配错误");
        }
          /*  } else {
                cookie = new Cookie("ins", "支付失败");
            }*/
        cookie.setPath("/");
        cookie.setMaxAge(3);
        response.addCookie(cookie);

       /* } catch (AlipayApiException e) {
            e.printStackTrace();
        }
*/
        String parkId = (String) session.getAttribute("parkId");
        return "redirect:http://www.shmuyun.cn/payweiNew/?parkId=" + parkId;
    }

}
