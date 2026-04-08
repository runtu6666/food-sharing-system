package com.foodshare.common;

/**
 * 统一返回结果类
 * 用于API接口的统一响应格式
 */
public class Result {
    private Integer code;     // 状态码: 200成功, 其他失败
    private String message;   // 提示信息
    private Object data;      // 返回数据

    // 构造方法
    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getter和Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    // 成功(带数据和消息)
    public static Result success(Object data, String message) {
        return new Result(200, message, data);
    }

    // 成功(只带数据)
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }

    // 成功(只带消息)
    public static Result success(String message) {
        return new Result(200, message, null);
    }

    // 失败(默认500)
    public static Result error(String message) {
        return new Result(500, message, null);
    }

    // 失败(自定义状态码)
    public static Result error(String message, Integer code) {
        return new Result(code, message, null);
    }
}