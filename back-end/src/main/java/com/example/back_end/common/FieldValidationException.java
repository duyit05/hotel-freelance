package com.example.back_end.common;

import java.util.Map;

/** Lỗi kiểm tra dữ liệu theo từng ô nhập, trả về 400 với "errors" giống lỗi @Valid */
public class FieldValidationException extends RuntimeException {

    private final transient Map<String, String> errors;

    public FieldValidationException(Map<String, String> errors) {
        super("Dữ liệu chưa hợp lệ, vui lòng kiểm tra lại");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
