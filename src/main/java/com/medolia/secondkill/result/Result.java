package com.medolia.secondkill.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private CodeMsg msg;
    private T data;

    public Result(T data) {
        this.data = data;
    }

    public Result(CodeMsg msg) {
        this.msg = msg;
    }

    public Result(int code, CodeMsg msg) {
        this.code = code;
        this.msg = msg;
    }

    // 成功获得结果时
    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    // 出现错误时
    public static <T> Result<T> error(CodeMsg msg) {
        return new Result<T>(msg);
    }
}
