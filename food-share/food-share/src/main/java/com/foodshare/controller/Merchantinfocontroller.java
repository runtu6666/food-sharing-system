package com.foodshare.controller;

import com.foodshare.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家资质信息控制器
 * 商家提交/更新营业执照、身份证等认证资料
 * 管理员可以查看商家资质详情
 */
@RestController
@RequestMapping("/merchantInfo")
@CrossOrigin
public class MerchantInfoController {

    @Autowired
    private DataSource dataSource;

    /**
     * 查询某商家的资质信息
     * GET /merchantInfo/get?userId=1
     */
    @GetMapping("/get")
    public Result get(@RequestParam Long userId) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, user_id, legal_name, id_card, license_number, license_image, create_time, update_time " +
                            "FROM merchant_info WHERE user_id = ?");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", rs.getLong("id"));
                info.put("userId", rs.getLong("user_id"));
                info.put("legalName", rs.getString("legal_name"));
                // 身份证号码脱敏处理，只显示前4位和后4位
                String idCard = rs.getString("id_card");
                if (idCard != null && idCard.length() > 8) {
                    idCard = idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
                }
                info.put("idCard", idCard);
                info.put("licenseNumber", rs.getString("license_number"));
                info.put("licenseImage", rs.getString("license_image"));
                info.put("createTime", rs.getString("create_time"));
                info.put("updateTime", rs.getString("update_time"));
                return Result.success(info);
            }
            return Result.success(null); // 尚未提交资质
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 提交或更新商家资质信息（有则更新，无则新增）
     * POST /merchantInfo/save
     * 请求体: { userId, legalName, idCard, licenseNumber, licenseImage }
     */
    @PostMapping("/save")
    public Result save(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String legalName = (String) body.get("legalName");
        String idCard = (String) body.get("idCard");
        String licenseNumber = (String) body.get("licenseNumber");
        String licenseImage = (String) body.getOrDefault("licenseImage", "");

        // 基础校验
        if (legalName == null || legalName.trim().isEmpty()) return Result.error("法人姓名不能为空");
        if (idCard == null || idCard.trim().isEmpty()) return Result.error("身份证号不能为空");
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) return Result.error("营业执照编号不能为空");

        try (Connection conn = dataSource.getConnection()) {
            // 先查是否已有记录
            PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT id FROM merchant_info WHERE user_id = ?");
            checkPs.setLong(1, userId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // 已有记录 → 更新
                PreparedStatement updatePs = conn.prepareStatement(
                        "UPDATE merchant_info SET legal_name=?, id_card=?, license_number=?, " +
                                "license_image=?, update_time=NOW() WHERE user_id=?");
                updatePs.setString(1, legalName.trim());
                updatePs.setString(2, idCard.trim());
                updatePs.setString(3, licenseNumber.trim());
                updatePs.setString(4, licenseImage);
                updatePs.setLong(5, userId);
                updatePs.executeUpdate();
                return Result.success(null, "资质信息更新成功");
            } else {
                // 无记录 → 新增
                PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO merchant_info (user_id, legal_name, id_card, license_number, " +
                                "license_image, create_time, update_time) VALUES (?, ?, ?, ?, ?, NOW(), NOW())");
                insertPs.setLong(1, userId);
                insertPs.setString(2, legalName.trim());
                insertPs.setString(3, idCard.trim());
                insertPs.setString(4, licenseNumber.trim());
                insertPs.setString(5, licenseImage);
                insertPs.executeUpdate();
                return Result.success(null, "资质信息提交成功");
            }
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }
}