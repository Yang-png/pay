package com.demo.paywei.entity;

public class Coupon {

    private int id;
    private String uuid;
    private String coupon;
    private String license;
    private String couponUuid;
    private int time;


    private String indent;

    public Coupon() {
    }

    public Coupon( String uuid, String coupon, String license, String couponUuid, int time) {
        this.uuid = uuid;
        this.coupon = coupon;
        this.license = license;
        this.couponUuid = couponUuid;
        this.time = time;
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public String getCouponUuid() {
        return couponUuid;
    }

    public void setCouponUuid(String couponUuid) {
        this.couponUuid = couponUuid;
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

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
