package com.demo.paywei.mapper;

import com.demo.paywei.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.validation.Payload;
import java.util.List;

@Mapper
public interface LogicMapper {

    //获取park信息
    public Park getParkId(String parkId);

    //获取车辆进场记录
    public List<CarLogPresent> getCarLogPresent(@Param(value = "uuid") String uuid,@Param(value = "license") String license);

    //获取方案信息
    public Feesolution getFeesolution(String soluUuid);

    //收费设置
    public List<Feeplan> selectFeeplan(String soluUuid);

    //阶梯
    public List<LadderPrice> selectLadder(String soluUuid);

    //JSAPI支付完成后在车辆进出信息中补全付款时间
    public int updateCarLog(PayLog payLog);

    //更新在场车辆
    public int updateCarLogPresent(PayLog payLog);

    //添加付款记录
    public int addPayLog(PayLog payLog);

    //根据进出记录uuid获取信息
    public CarLogPresent  selectCaownerLogUuid(String logUuid);

    //更新车辆进出表优惠价格
    public int updateCarLogDis(@Param(value = "uuid") String uuid,@Param(value = "discount") int discount);

    //根据uuid查询基本收费
    public Feeplan getfeeplan(Feeplan feeplan);

    //领取车辆优惠券 更新车辆表
    public int updateCarLogWtDiscount(CarLogPresent carLogPresent);

    //领取车辆优惠券 更新在场车辆表
    public int updateCarLogPerWtDiscount(CarLogPresent carLogPresent);

    //查询优惠券表是否存在相同单号
    public Coupon getCoupon(String indent);

    //插入单号
    public int insCoupon(Coupon coupon);

    //优惠券用户登录
    public Worker selectUserLogin(Worker worker);

    public CarLog getcarlog(String carUuid);

    //获取月租车信息
    public Carowner getRentCar(@Param(value = "license") String license,@Param(value = "uuid") String uuid);

    //获取月租收费方案
    public Feesolution getFeesoluUuid(String uuid);

    //月租车续租付款记录
    public int addRentCar(PayLog payLog);

    //更新月租车续约记录
    public int updateRentCar(PayLog payLog);

}
