package com.foodshare.entity;

import lombok.Data;
import java.util.Date;

/**
 * 商家资质信息实体类
 * 对应数据库表: merchant_info
 * 存储商家的法人姓名、身份证、营业执照等认证材料
 */
@Data
public class MerchantInfo {

    /** 主键ID */
    private Long id;

    /** 用户ID，关联user表（唯一索引，一个用户只有一份资质） */
    private Long userId;

    /** 法人姓名 */
    private String legalName;

    /** 身份证号码 */
    private String idCard;

    /** 营业执照编号 */
    private String licenseNumber;

    /** 营业执照图片（base64编码） */
    private String licenseImage;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
