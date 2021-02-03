package com.medolia.secondkill.result;

import lombok.Data;

@Data
public class Result<T> {
    private static final int SUCCESS_CODE = 0;

    private int code;
    private CodeMsg msg;
    private T data;

    public Result(int code, CodeMsg msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    // 成功获得结果时
    public static <T> Result<T> success(T data) {
        return new Result<T>(SUCCESS_CODE, data);
    }

    // 出现错误时
    public static <T> Result<T> error(CodeMsg msg) {
        return new Result<T>(msg.getCode(), msg);
    }
}
