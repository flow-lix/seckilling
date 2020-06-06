package org.fancy.seckilling.vo;

import lombok.Data;
import org.fancy.seckilling.enums.ResultStatus;

@Data
public class ResponseVo<T> {

    private int code;
    private String msg;
    private T body;

    private ResponseVo(ResultStatus resultStatus) {
        this.code = resultStatus.getCode();
        this.msg = resultStatus.getMessage();
    }

    public static <T> ResponseVo<T> failOf(ResultStatus resultStatus) {
        return new ResponseVo<>(resultStatus);
    }
}
