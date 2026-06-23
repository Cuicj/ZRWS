package com.zrws.common.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应结果
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String msg;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    /** 操作是否成功 */
    private boolean success;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        r.setSuccess(true);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static <T> R<T> ok(String msg, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        r.setSuccess(true);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        r.setSuccess(false);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setSuccess(false);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }

    public R<T> message(String msg) {
        this.setMsg(msg);
        return this;
    }

    public R<T> code(int code) {
        this.setCode(code);
        return this;
    }
}