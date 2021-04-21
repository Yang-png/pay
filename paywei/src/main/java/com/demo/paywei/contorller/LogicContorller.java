package com.demo.paywei.contorller;

import com.demo.paywei.entity.*;
import com.demo.paywei.mapper.LogicMapper;
import com.demo.paywei.service.LogicService;
import com.demo.paywei.unit.LoginUtil;
import com.demo.paywei.unit.Md5Util;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
public class LogicContorller {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LogicService logicService;

    @Autowired
    private LogicMapper logicMapper;

    public static String parkId = "";

    @RequestMapping(value = "getParkId")
    @ResponseBody
    public Park getParkId(String parkId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        Park park = logicService.getParkId(parkId);
        String soluUuid = park.getBaseTempTollSolutionUuid();
        park.setTime(logicMapper.getFeesolution(soluUuid).getLeaveAfterPaymentInMins());
        session.setAttribute("orgUuid", park.getUuid()); //添加付款记录时需用
        session.setAttribute("parkId", parkId); //添加记录后 带参跳转页面需用
        LogicContorller.parkId = parkId;
        return park;
    }

    @RequestMapping(value = "theLogin.htmls")
    public String theLogin(Worker worker){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        LoginUtil md5 = new LoginUtil();
        Worker us = logicMapper.selectUserLogin(worker);
        Cookie cookie;
        if(us != null){
            if (md5.MD5Encode(worker.getPassWord()).equals(us.getPassWord())) {
                HttpSession session = request.getSession();
                session.setAttribute("workerCou", us);
                session.setMaxInactiveInterval(28800);
            }else{
                cookie = new Cookie("worker", "密码有误");
                cookie.setPath("/");
                cookie.setMaxAge(3);
                response.addCookie(cookie);
                return "login";
            }
        }else{
            cookie = new Cookie("worker", "账号不存在");
            cookie.setPath("/");
            cookie.setMaxAge(3);
            response.addCookie(cookie);
            return "login";
        }
        return "redirect:couponsIndex?parkId="+us.getParkId();
    }

    @RequestMapping(value = "couponsIndex/getParkId")
    @ResponseBody
    public Park getCouParkId(String parkId) {
        return getParkId(parkId);
    }


    /**
     * @param uuid     停车场编码
     * @param license  车牌信息
     * @param soluUuid 收费方案uuid
     * @return
     */
    @RequestMapping(value = "getAmount")
    @ResponseBody
    public String getAmount(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "license") String license, @RequestParam(value = "soluUuid") String soluUuid) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.setAttribute("notify","pay"); //回调判断
        //1.获取车辆入场的数据信息  2.获取收费方案
        if (logicMapper.getCarLogPresent(uuid, license).size() <= 0 || logicMapper.getCarLogPresent(uuid, license) == null) {
            return "FALSE";
        }
        CarLogPresent carlog = logicMapper.getCarLogPresent(uuid, license).get(0);

        Feesolution feesolution = logicMapper.getFeesolution(soluUuid);
        feesolution.setFeeplanList(logicMapper.selectFeeplan(soluUuid));
        feesolution.setLadderPriceList(logicMapper.selectLadder(soluUuid));

        float small_amount = billing(feesolution, carlog);

        float dayAmount = (float) session.getAttribute("dayAmount");  //超过整天的金钱与多余分钟的钱总和
        float minutesAmount = (float) session.getAttribute("minutesAmount");  //不超过一小时的计费总和
        small_amount += minutesAmount;//总金额
        System.err.println("总金额:" + small_amount + "不超出一天的金额:" + (small_amount - dayAmount) + "不超过一小时的金额:" + minutesAmount);
        if (small_amount - dayAmount > feesolution.getDailyLimitAmount().floatValue()) {  //当日费用超出时 按日最高相加
            System.err.println("日封顶:" + feesolution.getDailyLimitAmount().floatValue());
            small_amount = dayAmount + feesolution.getDailyLimitAmount().floatValue();
            int wtAmount = (int)session.getAttribute("wtAmount");
            if(wtAmount != 0){
                small_amount = small_amount-wtAmount;
            }
        }

        if (small_amount > feesolution.getMaxAmount().floatValue()) {  //超过收费封顶时按封顶价格计费
            small_amount = feesolution.getMaxAmount().floatValue();
        }
        small_amount = small_amount / 100;

        System.err.println("合计金额:" + small_amount);
        System.err.println("最高收费:" + feesolution.getMaxAmount().floatValue() / 100);


        SimpleDateFormat dateForm24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long enterTime = carlog.getEnterTime();

        String enTime = dateForm24.format(new Date(enterTime * 1000));
        System.err.println("进场时间:" + enTime);
        int exitTime = (int) (new Date().getTime() / 1000);
        session.setAttribute("stayTime", carlog.getEnterTime());
        double stay = (double) (exitTime - carlog.getEnterTime()) / 3600; //当前时间戳减去车辆进场时间  得到车辆停车时间戳 除以一小时毫秒数得到停车小时 秒级
        int day = 0;
        if (stay >= 24) {
            day = (int) stay / 24;
            stay = stay % 24; //小时
        }


        String stays = String.format("%.2f", stay); //截取后两位


        session.setAttribute("licenseUuid", carlog.getUuid());
        session.setAttribute("license", license);
        session.setAttribute("enterTime", enTime);
        session.setAttribute("day", day);
        session.setAttribute("stay", stays);
        session.setAttribute("small_amount", small_amount);
        session.setAttribute("logUuid", carlog.getUuid());
        session.setAttribute("parkUuid", carlog.getParkUuid());

        return "SUCCESS";
    }

    @RequestMapping(value = "httpGetAmount")
    @ResponseBody
    public Object httpGetAmount (CarLogPresent carlog,String soluUuid) throws Exception{
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        Feesolution feesolution = logicMapper.getFeesolution(soluUuid);
        feesolution.setFeeplanList(logicMapper.selectFeeplan(soluUuid));
        feesolution.setLadderPriceList(logicMapper.selectLadder(soluUuid));

        float small_amount = billing(feesolution, carlog);

        float dayAmount = (float) session.getAttribute("dayAmount");  //超过整天的金钱与多余分钟的钱总和
        float minutesAmount = (float) session.getAttribute("minutesAmount");  //不超过一小时的计费总和
        small_amount += minutesAmount;//总金额
        System.err.println("总金额:" + small_amount + "不超出一天的金额:" + (small_amount - dayAmount) + "不超过一小时的金额:" + minutesAmount);
        if (small_amount - dayAmount > feesolution.getDailyLimitAmount().floatValue()) {  //当日费用超出时 按日最高相加
            small_amount = dayAmount + feesolution.getDailyLimitAmount().floatValue();
            int wtAmount = (int)session.getAttribute("wtAmount");
            if(wtAmount != 0){
                small_amount = small_amount-wtAmount;
            }
        }

        if (small_amount > feesolution.getMaxAmount().floatValue()) {  //超过收费封顶时按封顶价格计费
            small_amount = feesolution.getMaxAmount().floatValue();
        }
        small_amount = small_amount / 100;

        System.err.println("合计金额:" + small_amount);
        System.err.println("最高收费:" + feesolution.getMaxAmount().floatValue() / 100);


        SimpleDateFormat dateForm24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long enterTime = carlog.getEnterTime();

        String enTime = dateForm24.format(new Date(enterTime * 1000));
        System.err.println("进场时间:" + enTime);
        int exitTime = (int) (new Date().getTime() / 1000);
        session.setAttribute("stayTime", carlog.getEnterTime());
        double stay = (double) (exitTime - carlog.getEnterTime()) / 3600; //当前时间戳减去车辆进场时间  得到车辆停车时间戳 除以一小时毫秒数得到停车小时 秒级
        int day = 0;
        if (stay >= 24) {
            day = (int) stay / 24;
            stay = stay % 24; //小时
        }


        String stays = String.format("%.2f", stay); //截取后两位

        Map<String,Object> map = new HashMap<>();
        map.put("enterTime", enTime);
        map.put("amount", small_amount);

        return map;
    }

    @RequestMapping(value = "getRentCar")
    @ResponseBody
    public Object getRentCar(@RequestParam(value = "uuid") String uuid, @RequestParam(value = "license") String license) throws Exception{
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        Carowner carowner =logicMapper.getRentCar(license, uuid);
        //查询此车辆是否为月租车
        if (null == carowner || carowner.getUuid() == null) {
            return "FALSE";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(format.format(carowner.getEndDate()));
        session.setAttribute("endDate", format.format(carowner.getEndDate()));
        session.setAttribute("parkUuid", carowner.getParkuuid());
        session.setAttribute("license", carowner.getLicense());
        session.setAttribute("orgUuid", carowner.getOrguuid());
        session.setAttribute("uuid", carowner.getUuid());
        session.setAttribute("startDate", carowner.getStartDate());
        session.setAttribute("logUuid", "-1");

        return "true";
    }

    @RequestMapping(value = "getFeesolutionUuid.htmls")
    @ResponseBody
    public Feesolution getFeesolutionUuid(String uuid){
        return logicMapper.getFeesoluUuid(uuid);
    }

    @RequestMapping(value = "addRnetCar")
    public void addRnetCar(float small_amount,String endDate) throws Exception{
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.setAttribute("small_amount",small_amount); //金额
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println("endDate:"+endDate);
        System.err.println(format.parse(endDate));
        session.setAttribute("newEndDate",format.parse(endDate)); //续约时间  parse format
        session.setAttribute("notify","rentCar"); //回调判断
        String userAgent = (String) session.getAttribute("merchants");
        if (userAgent != null && "支付宝" == userAgent){
            response.sendRedirect("http://www.shmuyun.cn/payweiNew/alipay/webPay");
        }else{
            response.sendRedirect("pay.html");
        }
    }

    @RequestMapping(value = "getCarlogPresent.html")
    @ResponseBody
    public String getCarlogPresent(Coupon coupon) {
        String uuid = coupon.getUuid(); //停车场编码
        String license = coupon.getLicense();//车牌
        String indent = coupon.getIndent();//订单号
        String couponUuid = coupon.getCouponUuid();//操作员uuid

        if (logicMapper.getCarLogPresent(uuid, license).size() <= 0 || logicMapper.getCarLogPresent(uuid, license) == null) {
            return "FALSE"; //在场车辆不存在
        }

        if (logicMapper.getCoupon(indent) != null) {
            return "exist"; //单号重复
        }

        CarLogPresent carlog = logicMapper.getCarLogPresent(uuid, license).get(0);
        if (carlog.getWtDiscount() == 1) {
            return "FALSEE"; //未出场但已经领取优惠券
        }
        long time = new Date().getTime()/1000;

        Coupon cou = new Coupon(UUID.randomUUID().toString().replace("-", "").toUpperCase(), indent, license,couponUuid,(int) time);
        logicMapper.insCoupon(cou); //保存优惠券有车牌领取信息
        logicMapper.updateCarLogWtDiscount(carlog); //更新车辆表优惠券字段
        logicMapper.updateCarLogPerWtDiscount(carlog); //更新在场车辆表优惠券字段
        return "SUCCESS";
    }

    @RequestMapping(value = "couponsIndex/getCarlogPresent.html")
    @ResponseBody
    public String getCouCarlogPresent(Coupon coupon) {
        return getCarlogPresent(coupon);
    }

    @RequestMapping(value = "payOk.html")
    public String payOk() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        String carUuid = (String) session.getAttribute("logUuid");
        CarLog carLog = logicMapper.getcarlog(carUuid);
        String parkId = (String) session.getAttribute("parkId");

        try {
            if(carLog != null && carLog.getPaid() == 1 ){
                return "redirect:http://www.shmuyun.cn/payweiNew/?parkId=" + parkId;
            }else{
                //1.更新付款时间  2.添加付款记录
                Date date = new Date();
                PayLog payLog = new PayLog();

                payLog.setLogUuid((String) session.getAttribute("logUuid"));
                payLog.setLicense((String) session.getAttribute("license"));
                payLog.setParkUuid((String) session.getAttribute("parkUuid"));
                payLog.setOrgUuid((String) session.getAttribute("orgUuid"));
                float amountx = (float) session.getAttribute("small_amount");
                payLog.setAmount((int) (amountx * 100));
                long payTime = date.getTime() / 1000; //获取当前时间戳/1000换算时不要转int 换算完后再转
                payLog.setPayTime((int) payTime);
                payLog.setUuid(UUID.randomUUID().toString().replace("-", "").toUpperCase());
                System.err.println("uuid:" + payLog.getUuid() + "logUuid:" + payLog.getLogUuid() + "license:" + payLog.getLicense() + "orgUuid:" + payLog.getOrgUuid() + "amount:" + payLog.getAmount());


                Map<String, Object> payResp = (Map<String, Object>) session.getAttribute("payResp");
                Map<String, Object> att = new HashMap<>();
                att.put("logUuid", payLog.getLogUuid());
                att.put("status", 0);
                payResp.put("attach", att);
                payResp.put("time_end", "20200708112001");

                payLog.setTradeNo((String) payResp.get("out_trade_no"));
                payLog.setPayResp(payResp.toString());

                payLog.setPayMethod(6);

                //更新进出记录
                logicMapper.updateCarLog(payLog);

                //更新在场车辆线上支付
                logicMapper.updateCarLogPresent(payLog);


                //添加付款记录
                logicMapper.addPayLog(payLog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:http://www.shmuyun.cn/payweiNew/?parkId=" + parkId;
    }

    /**
     * 根据方案和进场时间计算价格
     *
     * @param feesolution
     * @param carlog
     * @return
     */
    public float billing(Feesolution feesolution, CarLogPresent carlog) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();

        int dayStart = feesolution.getDayTimeStart();  //日间开始时间
        int dayEnd = feesolution.getDayTimeEnd();  //日间结束时间
        //获取当前时间戳 计算停车时间 按小时计算；进场小时单位对比开始时间 来选择收费规则
        int exitTime = (int) (new Date().getTime() / 1000);
        //获取车辆记录中是否可以优惠一小时  如可以则进场时间往后拉一小时
        int wtDiscount = logicMapper.selectCaownerLogUuid(carlog.getUuid()).getWtDiscount(); //获取是否优惠
        double timeMinus = (double) (exitTime - carlog.getEnterTime()) / 3600;
        double timeNums; //停车秒级时间单位
        int wtAmount = 0;
        if (wtDiscount == 1) {

            System.err.println("[paywei-billing]-获取优惠金额存入数据库");

            Feeplan feeplan = null;
            for (Feeplan f : feesolution.getFeeplanList()) {
                if (f.getNighttime() == 0) { //日间方案
                    feeplan = f;
                }
            }
            if (feeplan.getUnitType() == 0) { //按次收费直接返回费用
                return feeplan.getAmount().floatValue();
            }

            feeplan = feeSwitch(feeplan); //转换时间金额
            double discountDouble = feeplan.getAmount().doubleValue();//数据库中关于金额全部乘以了100所以不需要再次乘以100
            int discount = (int) discountDouble;
            logicMapper.updateCarLogDis(carlog.getUuid(), discount); //更新车辆优惠金额
            wtAmount = discount;
            session.setAttribute("discount", discount / 100); //存入会话


            if (exitTime - (carlog.getEnterTime() + 3600) <= 0) {
                timeNums = 0;
            } else {
                timeNums = (double) (exitTime - (carlog.getEnterTime() + 3600)) / 3600; //如果有优惠一小时权利则将停车时间缩短一小时
            }
        } else {
            timeNums = (double) (exitTime - carlog.getEnterTime()) / 3600; //当前时间戳减去车辆进场时间  得到车辆停车时间戳 除以一小时毫秒数得到停车小时 秒级
            session.setAttribute("discount", 0); //存入会话
        }
        session.setAttribute("wtAmount",wtAmount); //优惠金额
        System.err.println("总停车时间：" + timeNums);
       /* String s = xx + "";
        int length = s.length() - s.indexOf(".") + 1;  //判断小时是否多出 如有则+1小时*/


        Float CLOUD_KEY_SMALL_AMOUNT = 0f;  //float金额
        int freeTimeUnit = 0; //免费时长 分钟
        switch (feesolution.getFreeTimeUnitType()) {
            case 0:
                freeTimeUnit = feesolution.getFreeTimeUnit() * 60; //小时换算分钟
                break;
            case 1:
                freeTimeUnit = feesolution.getFreeTimeUnit(); //分钟
                break;
        }

        int nums = 0;
        int xxInt = 0;
        if (timeNums * 60 > freeTimeUnit) {  //当停车时间大于免费时间并且为小数则加1小时
            nums = (int) timeNums;
            xxInt = (int) timeNums;
        } else {
            System.err.println("未超过免费时间");
            session.setAttribute("dayAmount", 0f);
            session.setAttribute("minutesAmount", 0f);
            return 0f;  //小于时间则不收取费用
        }


        System.err.println("余小时：" + nums);


        //停车时间超过一天时余出天数
        int day = 0;
        if (nums >= 24) {
            day = nums / 24; //天数
            nums = nums % 24; //余下小时
        }
        float amount = day * feesolution.getDailyLimitAmount().floatValue(); //天数计费 不超过一天则为0
        System.err.println("最高收费：" + feesolution.getDailyLimitAmount().floatValue() + "天数金额:" + amount);

        float minutesAmount = minutesAmount(timeNums, xxInt, feesolution,timeMinus); //获取多余分钟的计费 不存在多余分钟则为0
        System.err.println("多余分钟按日间或夜间金额:" + minutesAmount);
        session.setAttribute("dayAmount", amount);
        session.setAttribute("minutesAmount", minutesAmount);


        //是否开启日夜间模式
        if (feesolution.getWholeDay() == 0) {
            System.err.println("进场时间：" + carlog.getEnterTime());
            System.err.println("出场时间：" + exitTime);
            System.err.println("余下小时：" + nums);
            System.err.println("日开始：" + dayStart);
            System.err.println("日结束：" + dayEnd);
            System.err.println("免费时间：" + freeTimeUnit);
            //获取进场时间的时段
            long enterTime = (long) carlog.getEnterTime();

            //获取小时段  yyyy-MM-dd HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("HH");
            int HH = Integer.parseInt(sdf.format(enterTime * 1000));  //毫秒级转换小时 由于数据库存储为10位所以需要乘以1000
            System.err.println("进场小时单位：" + HH);

            //如果进场小时段在日间区间按日间计费   开始时间  停车时间：nums 进场小时时间: HH  日间开始：dayStart  结束：dayEnd
            if (HH >= dayStart && HH < dayEnd) {
                //细分日夜区间计费
                if (nums > (dayEnd - HH)) {
                    int nums1 = dayEnd - HH;
                    CLOUD_KEY_SMALL_AMOUNT = sun(feesolution, nums1, amount); //首先计算进场至日间结束费用
                    if (nums - (dayEnd - HH) > dayStart + 24 - dayEnd) {  //剩余时长是否超出夜间区间
                        //计算整个夜间费用
                        CLOUD_KEY_SMALL_AMOUNT += moon(feesolution, (dayStart + 24 - dayEnd), amount);

                        //计算超出夜间费用按日间收费
                        CLOUD_KEY_SMALL_AMOUNT += sun(feesolution, (nums - nums1) - (dayStart + 24 - dayEnd), amount);
                    } else {  //根据剩余小时按夜间方案计费
                        CLOUD_KEY_SMALL_AMOUNT += moon(feesolution, nums - nums1, amount);
                    }
                    return CLOUD_KEY_SMALL_AMOUNT;
                } else { //计费情况类别：时间不超出日间区间
                    return sun(feesolution, nums, amount);
                }
            } else {  //进场时间不在日间区间则先按夜晚计费
                int interval = 0;  //夜晚区间
                if (HH < dayStart) {  //进场时间在时间左还是右
                    interval = dayStart - HH;
                } else {
                    interval = HH - dayStart;
                }
                //细分日夜区间计费
                if (nums > interval) {
                    //先计算进场夜间计费
                    CLOUD_KEY_SMALL_AMOUNT = moon(feesolution, interval, amount); //首先计算进场至夜间间结束费用
                    if (nums - interval > dayEnd - dayStart) { //剩余时长是否超出日间区间
                        //超出则先计算整个日间费用
                        CLOUD_KEY_SMALL_AMOUNT += sun(feesolution, dayEnd - dayStart, amount);
                        //超出部分再由日间计费
                        CLOUD_KEY_SMALL_AMOUNT += moon(feesolution, (nums - interval) - (dayEnd - dayStart), amount);

                    } else {//不超出则根据剩余小时按日间方案计费
                        CLOUD_KEY_SMALL_AMOUNT += sun(feesolution, nums - interval, amount);
                    }
                    return CLOUD_KEY_SMALL_AMOUNT;
                } else { //计费情况类别：时间不超出夜间区间
                    return moon(feesolution, nums, amount);
                }
            }
        } else {  //未开启日夜收费 直接日间计费方式收费
            return sun(feesolution, nums, amount);
        }
    }


    /**
     * 计算余下分钟费用
     * @param xx 总停车时长
     * @param xxInt 总小时
     * @param feesolution 方案
     * @param timeMinus 未经过优惠券的总停车时长
     * @return
     */
    public float minutesAmount(double xx, int xxInt, Feesolution feesolution,double timeMinus) {
        double minutes = (xx - xxInt) * 60; //余下不足一小时的时间  /分钟
        System.err.println("计算分钟:" + minutes);
        double feeMinutes = 0.0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int HH = Integer.parseInt(sdf.format(new Date().getTime()));  //毫秒级转换小时 由于数据库存储为10位所以需要乘以1000
        System.err.println("出场小时单位：" + HH);
        if (minutes != 0) { //如果存在多余分钟
            Feeplan feeplan = new Feeplan();
            if (feesolution.getWholeDay() == 1 || (HH >= feesolution.getDayTimeStart() && HH < feesolution.getDayTimeEnd())) { //0为开启日夜,如果未开启日夜分时 或 开启后出场小时也在日间范围
                if (feesolution.getLadderPriceList().size() > 0) { //是否开启阶梯
                    LadderPrice ladder = feesolution.getLadderPriceList().get(0); //转换阶梯小时做判断是否达到进入阶梯条件
                    switch (ladder.getLadderAfterTimeUnitType()) { //修改超出时间类型单位
                        case 1: //收费分钟
                            ladder.setLadderAfterTimeUnit(ladder.getLadderAfterTimeUnit() / 60);  //超出规则换算成小时制 用于比较
                            break;
                        case 2: //小时
                            break;
                    }
                    if (timeMinus > ladder.getLadderAfterTimeUnit()) { //满足条件进入阶梯计费
                        switch (ladder.getLadderUnitType()){
                            case 1: //阶梯分钟
                                feeMinutes =  ladder.getLadderTimeUnit();
                                break;
                            case 2: //阶梯小时
                                feeMinutes = ladder.getLadderTimeUnit() * 60;
                                break;
                        }

                        if (feeMinutes > minutes) { //不超过设定计费时间标准 返回指定费用
                            return ladder.getLadderAmount().floatValue();
                        } else {
                            int x = (int) (minutes / feeMinutes); //余下分钟多出方案一次收费时间几倍
                            double y = minutes % feeMinutes; //余数
                            if (y != 0) {
                                x = x + 1;
                            }
                            return ladder.getLadderAmount().multiply(new BigDecimal(x)).floatValue();
                        }
                    }
                }

                //余下分钟按日间收费标准计费
                for (Feeplan f : feesolution.getFeeplanList()) {
                    if (f.getNighttime() == 0) { //日间方案
                        feeplan = f;
                    }
                }

                feeplan = logicMapper.getfeeplan(feeplan); //重新获取基本收费覆盖之前声明的变量
                //小时转分钟
                switch (feeplan.getUnitType()) {
                    case 1:  //分钟
                        break;
                    case 2: //小时
                        feeMinutes = feeplan.getTimeUnit() * 60;
                        break;
                    case 3://分钟
                        feeMinutes = feeplan.getTimeUnit();
                        break;
                    case 4:  //天
                        break;
                }

                System.err.println("方案分钟：" + feeMinutes + "余下分钟：" + minutes);
                if (feeMinutes > minutes) { //不超过设定计费时间标准 返回指定费用
                    System.err.println("feeplanUuid:" + feeplan.getUuid() + "amount:" + feeplan.getAmount().floatValue());
                    return feeplan.getAmount().floatValue();
                } else {
                    int x = (int) (minutes / feeMinutes); //余下分钟多出方案一次收费时间几倍
                    double y = minutes % feeMinutes; //余数
                    if (y != 0) {
                        x = x + 1;
                    }
                    return feeplan.getAmount().multiply(new BigDecimal(x)).floatValue();
                }
            } else {
                //余下分钟按日间收费标准计费
                //余下分钟按日间收费标准计费
                for (Feeplan f : feesolution.getFeeplanList()) {
                    if (f.getNighttime() == 1) { //夜间方案
                        feeplan = f;
                    }
                }
                feeplan = logicMapper.getfeeplan(feeplan); //重新获取基本收费覆盖之前声明的变量
                switch (feeplan.getUnitType()) {
                    case 1:  //次
                        break;
                    case 2: //小时
                        feeMinutes = feeplan.getTimeUnit() * 60;
                        break;
                    case 3://分钟
                        feeMinutes = feeplan.getTimeUnit();
                        break;
                    case 4:  //天
                        break;
                }

                if (feeMinutes > minutes) { //不超过不同计费时间标准
                    return feeplan.getAmount().floatValue();
                } else {
                    int x = (int) (minutes / feeMinutes); //余下分钟多出方案一次收费时间几倍
                    double y = minutes % feeMinutes; //余数
                    if (y != 0) {
                        x = x + 1;
                    }
                    return feeplan.getAmount().multiply(new BigDecimal(x)).floatValue();
                }

            }
        } else {//不存在多余分钟
            return 0;
        }
    }


    /**
     * 日间方案计费
     *
     * @param feesolution
     * @return CLOUD_KEY_SMALL_AMOUNT
     */
    public float sun(Feesolution feesolution, int nums, float amount) {
        System.err.println("余下小时:" + nums);
        Feeplan feeplan = null;
        for (Feeplan f : feesolution.getFeeplanList()) {
            if (f.getNighttime() == 0) { //日间方案
                feeplan = f;
            }
        }
        feeplan = logicMapper.getfeeplan(feeplan); //重新获取基本收费覆盖之前声明的变量

        if (feeplan.getUnitType() == 1) { //按次收费直接返回费用
            return feeplan.getAmount().floatValue();
        }

        feeplan = feeSwitch(feeplan); //转换时间金额

        List<LadderPrice> ladderPriceList = new ArrayList<>(); //阶梯集合
        //此方案是否有阶梯计费
        if (feeplan.getLadderPricing() == 1) {
            for (LadderPrice l : feesolution.getLadderPriceList()) {
                if (l.getPlanUuid().equals(feeplan.getUuid())) { //查出日间收费对应的阶梯集合
                    ladderPriceList.add(l);
                }
            }
            amount = sunLadder(ladderPriceList, feeplan, nums, amount);  //阶梯计费

        } else {  //未开启阶梯计费
            amount = feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue() + amount; //金额结算   转换成float
        }

        return amount;

    }


    /**
     * 夜间方案计费
     *
     * @param feesolution
     * @param nums
     * @return
     */
    public float moon(Feesolution feesolution, int nums, float amount) {
        Feeplan feeplan = null;
        for (Feeplan f : feesolution.getFeeplanList()) {
            if (f.getNighttime() == 1) { //夜间方案
                feeplan = f;
            }
        }

        feeplan = logicMapper.getfeeplan(feeplan); //重新获取基本收费覆盖之前声明的变量

        if (feeplan.getUnitType() == 1) { //按次收费直接返回费用
            amount = feeplan.getAmount().floatValue();
            return amount;
        }

        feeplan = feeSwitch(feeplan);

        List<LadderPrice> ladderPriceList = new ArrayList<>(); //夜间阶梯集合
        //此方案是否有阶梯计费
        if (feeplan.getLadderPricing() == 1) {
            for (LadderPrice l : feesolution.getLadderPriceList()) {
                if (l.getPlanUuid().equals(feeplan.getUuid())) { //查出夜间间收费对应的阶梯集合
                    ladderPriceList.add(l);
                }
            }
            int i; //注释
            amount = sunLadder(ladderPriceList, feeplan, nums, amount);  //阶梯计费
        } else {
            amount = feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue() + amount; //小方案结算   转换成float
        }

        return amount;

    }


    /**
     * 收费基础设置转换
     *
     * @param feeplan
     * @return
     */
    public Feeplan feeSwitch(Feeplan feeplan) {
        switch (feeplan.getUnitType()) {
            case 1:  //次
                break;
            case 2:  //小时
                break;
            case 3:  //分钟
                feeplan.setAmount(feeplan.getAmount().multiply(new BigDecimal(60 / feeplan.getTimeUnit())));  //转换小时收费多少钱  60/分钟时间*金额
                break;
            case 4:  //天
                break;
        }
        return feeplan;
    }

    /**
     * 收费阶梯转换
     *
     * @param ladder
     * @return
     */
    public LadderPrice laddSwitch(LadderPrice ladder) {
        switch (ladder.getLadderAfterTimeUnitType()) { //修改超出时间类型单位
            case 1: //收费分钟
                ladder.setLadderAfterTimeUnit(ladder.getLadderAfterTimeUnit() / 60);  //超出规则换算成小时制 用于比较
                break;
            case 2: //小时
                break;
        }

        switch (ladder.getLadderUnitType()) {
            case 1:
                ladder.setLadderAmount(ladder.getLadderAmount().multiply(new BigDecimal(60 / ladder.getLadderTimeUnit())));  //如果超出后计费类型为分钟 则将金钱换算成小时制
                break;
            case 2:
                break;
        }

        return ladder;
    }


    /**
     * 阶梯计费
     *
     * @param ladderPriceList 阶梯集合
     * @param feeplan         收费设置
     * @param nums            停车时间
     * @param amount          金额
     * @return
     */
    public float sunLadder(List<LadderPrice> ladderPriceList, Feeplan feeplan, int nums, float amount) {
        for (int i = 0; i < ladderPriceList.size(); i++) {
            LadderPrice ladder = ladderPriceList.get(i);
            ladder = laddSwitch(ladderPriceList.get(i));  //时间单位转换
            if (i + 1 < ladderPriceList.size()) { //下一个阶梯是否存在
                LadderPrice ladder2 = laddSwitch(ladderPriceList.get(i + 1));  //阶梯2时间单位转换
                if (amount == 0) { //如果日封顶为0  则未超过一天
                    if (nums > ladder.getLadderAfterTimeUnit() && nums <= ladder2.getLadderAfterTimeUnit()) { //如果剩余时间大于阶梯制度时间且小于第二个阶梯时间  则用上第二阶梯
                        amount += feeplan.getAmount().multiply(new BigDecimal(ladder.getLadderAfterTimeUnit())).floatValue(); //未超过首个阶梯时间则先按正常收费计算
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(nums - ladder.getLadderAfterTimeUnit())).floatValue(); //超过时间按阶梯计费
                        nums = 0;
                    } else if (nums > ladder.getLadderAfterTimeUnit() && nums > ladder2.getLadderAfterTimeUnit()) {  //同时使用两阶梯
                        amount += feeplan.getAmount().multiply(new BigDecimal(ladder.getLadderAfterTimeUnit())).floatValue(); //未超过首个阶梯时间则先按正常收费计算
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(ladder2.getLadderAfterTimeUnit() - ladder.getLadderAfterTimeUnit())).floatValue(); //超过时间先由第一阶梯计费  第二阶梯下一次for循环再计费
                        nums = nums - ladder2.getLadderAfterTimeUnit(); //剩余停车小时
                    } else {//剩余时间小于第一个阶梯时间
                        amount += feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue(); //将剩余时间按普通计费计算费用
                        nums = 0;
                    }
                } else { //超过一天则直接按照阶梯收费
                    if (nums > ladder.getLadderAfterTimeUnit() && nums <= ladder2.getLadderAfterTimeUnit()) { //如果剩余时间大于阶梯制度时间且小于第二个阶梯时间  则用上第二阶梯
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(nums)).floatValue(); //超过时间按阶梯计费
                        nums = 0;
                    } else if (nums > ladder.getLadderAfterTimeUnit() && nums > ladder2.getLadderAfterTimeUnit()) {  //同时使用两阶梯
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(nums - ladder2.getLadderAfterTimeUnit())).floatValue(); //超过时间先由第一阶梯计费  第二阶梯下一次for循环再计费
                        nums = nums - ladder2.getLadderAfterTimeUnit(); //剩余停车小时
                    } else {//剩余时间小于第一个阶梯时间
                        amount += feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue(); //将剩余时间按普通计费计算费用
                        nums = 0;
                    }
                }

            } else { //不存在下个阶梯
                if (amount == 0) { //如果日封顶为0  则未超过一天
                    if (nums > ladder.getLadderAfterTimeUnit()) {  //如果剩余时间大于阶梯制度时间
                        amount += feeplan.getAmount().multiply(new BigDecimal(ladder.getLadderAfterTimeUnit())).floatValue(); //未超过阶梯时间则先按正常收费计算
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(nums - ladder.getLadderAfterTimeUnit())).floatValue(); //超过时间按阶梯计费
                    } else { //剩余时间小于阶梯时间
                        amount += feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue(); //将剩余时间按普通计费计算费用
                    }
                    nums = 0;
                } else { //超过一天则直接按照阶梯收费
                    if (nums > ladder.getLadderAfterTimeUnit()) {
                        amount += ladder.getLadderAmount().multiply(new BigDecimal(nums)).floatValue(); //超过时间按阶梯计费
                    } else { //剩余时间小于阶梯时间
                        amount += feeplan.getAmount().multiply(new BigDecimal(nums)).floatValue(); //将剩余时间按普通计费计算费用
                    }
                    nums = 0;
                }
            }

        }
        return amount;
    }

    @RequestMapping(value = "getOpenId")
    public void getOpenId() {
        new Md5Util().getOpenId();
    }


    @RequestMapping(value = "getOpenIds")
    public Object getOpenIds() {
        return new Md5Util().getOpenId();
    }

}
