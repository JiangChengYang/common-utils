package com.common.utils.result;


import lombok.Data;

@Data
public class Result {
    public static final Result SUCCESS = Result.build("SUCCESS");
    private String code;
    private String message;
    private Object data;

    public Result() {
    }

    public Result(String code) {
        this.code = code;
    }

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(String code, Object data) {
        this.code = code;
        this.data = data;
    }

    public static Result success(Object data) {
        return new Result("SUCCESS", data);
    }

    public static Result fail(String message, Object data) {
        return new Result("FAIL", message, data);
    }

    public static Result fail(String message) {
        return new Result("FAIL", message, null);
    }

    public static Result build(String code) {
        return new Result(code);
    }

    public static Result build(String code, String message) {
        return new Result(code, message);
    }

    public static Result build(String code, String message, Object data) {
        return new Result(code, message, data);
    }

    public static Result build(String code, Object data) {
        return new Result(code, data);
    }
}

