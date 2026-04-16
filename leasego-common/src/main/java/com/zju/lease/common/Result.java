package com.zju.lease.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return new Result<T>().setCode(200).setMessage("操作成功");
    }

    public static <T> Result<T> ok(T data) {
        return new Result<T>().setCode(200).setMessage("操作成功").setData(data);
    }

    public static <T> Result<T> fail() {
        return new Result<T>().setCode(500).setMessage("操作失败");
    }

    public static <T> Result<T> fail(String message) {
        return new Result<T>().setCode(500).setMessage(message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<T>().setCode(code).setMessage(message);
    }
}
