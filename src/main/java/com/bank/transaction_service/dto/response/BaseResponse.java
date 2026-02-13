package com.bank.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private T data;
    private ResultInfo resultInfo;

    public static <T> BaseResponse<T> success(T data, String msg) {
        return new BaseResponse<>(data, new ResultInfo(msg));
    }

    public static BaseResponse<Void> error(String msg, String errorCode) {
        return new BaseResponse<>(null, new ResultInfo(msg));
    }

    public static BaseResponse<Void> error(String msg) {
        return new BaseResponse<>(null, new ResultInfo(msg));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultInfo {
        private String resultMsg;

    }
}