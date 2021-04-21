package com.demo.paywei.entity;

public class Park {

    private int id;
    private String uuid;
    private String parkId;
    private String name;
    private String orguuid;
    private String address;
    private String baseTempTollSolutionUuid;
    private String baseRentTollSolutionUuid;
    private String wpAppid; //公众号
    private String wpAppsecret; //公众号密码
    private String wpMrchid; //商户号
    private String wpApicert; //商户秘钥
    private String alipayAppid;
    private String alipayPublicKey;
    private String alipayPrivateKey;
    private String sellerId;


    private int time; //暂做车辆离场时间存储


    public String getBaseRentTollSolutionUuid() {
        return baseRentTollSolutionUuid;
    }

    public void setBaseRentTollSolutionUuid(String baseRentTollSolutionUuid) {
        this.baseRentTollSolutionUuid = baseRentTollSolutionUuid;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBaseTempTollSolutionUuid() {
        return baseTempTollSolutionUuid;
    }

    public void setBaseTempTollSolutionUuid(String baseTempTollSolutionUuid) {
        this.baseTempTollSolutionUuid = baseTempTollSolutionUuid;
    }

    public String getAlipayAppid() {
        return alipayAppid;
    }

    public void setAlipayAppid(String alipayAppid) {
        this.alipayAppid = alipayAppid;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public String getAlipayPrivateKey() {
        return alipayPrivateKey;
    }

    public void setAlipayPrivateKey(String alipayPrivateKey) {
        this.alipayPrivateKey = alipayPrivateKey;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getParkId() {
        return parkId;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrguuid() {
        return orguuid;
    }

    public void setOrguuid(String orguuid) {
        this.orguuid = orguuid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWpAppid() {
        return wpAppid;
    }

    public void setWpAppid(String wpAppid) {
        this.wpAppid = wpAppid;
    }

    public String getWpAppsecret() {
        return wpAppsecret;
    }

    public void setWpAppsecret(String wpAppsecret) {
        this.wpAppsecret = wpAppsecret;
    }

    public String getWpMrchid() {
        return wpMrchid;
    }

    public void setWpMrchid(String wpMrchid) {
        this.wpMrchid = wpMrchid;
    }

    public String getWpApicert() {
        return wpApicert;
    }

    public void setWpApicert(String wpApicert) {
        this.wpApicert = wpApicert;
    }
}
