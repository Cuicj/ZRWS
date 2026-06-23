package com.zrws.common.core.controller;

import com.zrws.common.core.domain.PageResult;
import com.zrws.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 基础Controller
 * 提供通用方法
 */
public class BaseController {

    /**
     * 返回成功（无数据）
     */
    protected R<Void> success() {
        return R.ok();
    }

    /**
     * 返回成功（带数据）
     */
    protected <T> R<T> success(T data) {
        return R.ok(data);
    }

    /**
     * 返回成功（带消息和数据）
     */
    protected <T> R<T> success(String msg, T data) {
        return R.ok(msg, data);
    }

    /**
     * 返回失败
     */
    protected R<Void> fail() {
        return R.fail();
    }

    /**
     * 返回失败（带消息）
     */
    protected R<Void> fail(String msg) {
        return R.fail(msg);
    }

    /**
     * 返回分页结果
     */
    protected <T> R<PageResult<T>> getDataTable(List<T> list, long total) {
        return R.ok(PageResult.build(list, total));
    }
}