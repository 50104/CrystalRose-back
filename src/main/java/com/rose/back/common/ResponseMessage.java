package com.rose.back.common;

public interface ResponseMessage {
    
    String SUCCESS = "Success";

    String VALIDATION_FAIL = "Validation Failed";
    String DUPLICATE_ID = "Duplicate Id";

    String SIGN_IN_FAIL = "Login information mismatch";
    String CERTIFICATION_FAIL = "Certification Failed";

    String DATABASE_ERROR = "Database error";
}
