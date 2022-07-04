package com.fu.flix.constant;

import java.util.HashMap;
import java.util.Map;

public interface Constant {
    String ROLES = "roles";
    String BEARER = "Bearer ";
    String INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN";
    String REFRESH_TOKEN_MISSING = "REFRESH_TOKEN_MISSING";
    String NEW_ACCOUNT_VALID = "NEW_ACCOUNT_VALID";
    String ACCOUNT_EXISTED = "ACCOUNT_EXISTED";
    String INVALID_PASSWORD = "INVALID_PASSWORD";
    String REDIS_KEY_OTP_PREFIX = "OTP_";
    String INVALID_PHONE_NUMBER = "INVALID_PHONE_NUMBER";
    String INVALID_OTP = "INVALID_OTP";
    String INVALID_CITY = "INVALID_CITY";
    String INVALID_DISTRICT = "INVALID_DISTRICT";
    String INVALID_COMMUNE = "INVALID_COMMUNE";
    String STREET_ADDRESS_IS_REQUIRED = "STREET_ADDRESS_IS_REQUIRED";
    String WRONG_LOCAL_DATE_TIME_FORMAT = "WRONG_LOCAL_DATE_TIME_FORMAT";
    String WRONG_LOCAL_DATE_FORMAT = "WRONG_LOCAL_DATE_FORMAT";
    String FULL_NAME_IS_REQUIRED = "FULL_NAME_IS_REQUIRED";
    String USER_NOT_HOLD_VOUCHER = "USER_NOT_HOLD_VOUCHER";
    String CREATE_REPAIR_REQUEST_SUCCESSFUL = "CREATE_REPAIR_REQUEST_SUCCESSFUL";
    String VOUCHER_EXPIRED = "VOUCHER_EXPIRED";
    String CAN_NOT_CREATE_NEW_REQUEST_WHEN_HAVE_OTHER_PAYMENT_WAITING_REQUEST = "CAN_NOT_CREATE_NEW_REQUEST_WHEN_HAVE_OTHER_PAYMENT_WAITING_REQUEST";
    String VOUCHER_BEFORE_EFFECTIVE_DATE = "VOUCHER_BEFORE_EFFECTIVE_DATE";
    String VOUCHER_MUST_BE_TYPE_INSPECTION = "VOUCHER_MUST_BE_TYPE_INSPECTION";
    String OUT_OF_VOUCHER = "OUT_OF_VOUCHER";
    String EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS = "EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS";
    String INVALID_REQUEST_CODE = "INVALID_REQUEST_CODE";
    String JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED = "JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED";
    String USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST = "USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST";
    String CREATE_PAYMENT_URL_SUCCESS = "CREATE_PAYMENT_URL_SUCCESS";
    String PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    String PAYMENT_FAILED = "PAYMENT_FAILED";
    String CONFIRM_FIXING_SUCCESS = "CONFIRM_FIXING_SUCCESS";
    String INVALID_REPAIRER_SUGGESTION_TYPE = "INVALID_REPAIRER_SUGGESTION_TYPE";
    String VNP_TXN_REF_IS_REQUIRED = "VNP_TXN_REF_IS_REQUIRED";

    String VNP_TXN_REF_EXISTED_IN_DATABASE = "VNP_TXN_REF_EXISTED_IN_DATABASE";
    String REPAIR_REQUEST_NOT_FOUND = "REPAIR_REQUEST_NOT_FOUND";
    String AMOUNT_DOES_NOT_MATCH_TO_INVOICE = "AMOUNT_DOES_NOT_MATCH_TO_INVOICE";
    String INVALID_CHECKSUM = "INVALID_CHECKSUM";
    String CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING = "CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING";

    String PAYMENT_METHOD_MUST_BE_VN_PAY = "PAYMENT_METHOD_MUST_BE_VN_PAY";
    String CONFIRM_INVOICE_PAID_ONLY_USE_FOR_PAYMENT_IN_CASH = "CONFIRM_INVOICE_PAID_ONLY_USE_FOR_PAYMENT_IN_CASH";
    String CONFIRM_INVOICE_PAID_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING = "CONFIRM_INVOICE_PAID_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING";
    String TITLE_IS_REQUIRED = "TITLE_IS_REQUIRED";
    String USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_PAID_THIS_INVOICE = "USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_PAID_THIS_INVOICE";
    String DESCRIPTION_IS_REQUIRED = "DESCRIPTION_IS_REQUIRED";
    String CONFIRM_INVOICE_PAID_SUCCESS = "CONFIRM_INVOICE_PAID_SUCCESS";
    String CANCEL_REPAIR_REQUEST_SUCCESSFUL = "CANCEL_REPAIR_REQUEST_SUCCESSFUL";
    String ONLY_CAN_CANCEL_REQUEST_PENDING_OR_APPROVED = "ONLY_CAN_CANCEL_REQUEST_PENDING_OR_APPROVED";
    String ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED = "ONLY_CAN_CANCEL_REQUEST_FIXING_OR_APPROVED";
    String INVALID_SERVICE = "INVALID_SERVICE";
    String INVALID_ADDRESS = "INVALID_ADDRESS";
    String ADDRESS_ID_IS_REQUIRED = "ADDRESS_ID_IS_REQUIRED";
    String CHOOSING_MAIN_ADDRESS_SUCCESS = "CHOOSING_MAIN_ADDRESS_SUCCESS";
    String INSPECTION_PRICE_MUST_GREATER_OR_EQUAL_VOUCHER_MIN_PRICE = "INSPECTION_PRICE_MUST_GREATER_OR_EQUAL_VOUCHER_MIN_PRICE";
    String INVALID_STATUS = "INVALID_STATUS";
    String FILE_MUST_BE_IMAGE = "FILE_MUST_BE_IMAGE";
    String LOGIN_FAILED = "LOGIN_FAILED";
    String USER_IS_INACTIVE = "USER_IS_INACTIVE";
    String DELETE_ADDRESS_SUCCESS = "DELETE_ADDRESS_SUCCESS";
    String EDIT_ADDRESS_SUCCESS = "EDIT_ADDRESS_SUCCESS";
    String CREATE_ADDRESS_SUCCESS = "CREATE_ADDRESS_SUCCESS";
    String INVALID_EMAIL = "INVALID_EMAIL";
    String UPDATED_PROFILE_SUCCESS = "UPDATED_PROFILE_SUCCESS";
    String UPDATE_AVATAR_SUCCESS = "UPDATE_AVATAR_SUCCESS";
    String WRONG_PASSWORD = "WRONG_PASSWORD";
    String CHANGE_PASSWORD_SUCCESS = "CHANGE_PASSWORD_SUCCESS";
    String NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_OLD_PASSWORD = "NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_OLD_PASSWORD";
    String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    String USER_NAME_IS_REQUIRED = "USER_ID_IS_REQUIRED";
    String USER_ID_IS_REQUIRED = "USER_ID_IS_REQUIRED";
    String SEND_FORGOT_PASSWORD_OTP_SUCCESS = "SEND_FORGOT_PASSWORD_OTP_SUCCESS";
    String RESET_PASSWORD_SUCCESS = "RESET_PASSWORD_SUCCESS";
    String PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER = "PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER";
    String INVALID_FEEDBACK_TYPE = "INVALID_FEEDBACK_TYPE";
    String CREATE_FEEDBACK_SUCCESS = "CREATE_FEEDBACK_SUCCESS";
    String COMMENT_EXISTED = "COMMENT_EXISTED";
    String CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE = "CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE";
    String COMMENT_SUCCESS = "COMMENT_SUCCESS";
    String RATING_IS_REQUIRED = "RATING_IS_REQUIRED";
    String RATING_MUST_IN_RANGE_1_TO_5 = "RATING_MUST_IN_RANGE_1_TO_5";
    String USER_AND_REQUEST_CODE_DOES_NOT_MATCH = "USER_AND_REQUEST_CODE_DOES_NOT_MATCH";
    String APPROVAL_REQUEST_SUCCESS = "APPROVAL_REQUEST_SUCCESS";
    String JUST_CAN_ACCEPT_PENDING_REQUEST = "JUST_CAN_ACCEPT_PENDING_REQUEST";
    String CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING = "CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_THIS_REQUEST_DETAIL = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_THIS_REQUEST_DETAIL";
    String ACCESS_DENIED = "ACCESS_DENIED";
    String INVALID_ROLE_TYPE = "INVALID_ROLE_TYPE";
    String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    String WRONG_DATA_TYPE = "WRONG_DATA_TYPE";
    String BALANCE_MUST_GREATER_THAN_OR_EQUAL_ = "BALANCE_MUST_GREATER_THAN_OR_EQUAL_";
    String USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST = "USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST";
    String INVALID_CATEGORY_ID = "INVALID_CATEGORY_ID";
    String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    String JUST_CAN_CREATE_INVOICE_WHEN_REQUEST_STATUS_IS_FIXING = "JUST_CAN_CREATE_INVOICE_WHEN_REQUEST_STATUS_IS_FIXING";
    String CREATE_INVOICE_SUCCESS = "CREATE_INVOICE_SUCCESS";
    String INVALID_IP_ADDRESS = "INVALID_IP_ADDRESS";
    String KEY_AND_DATA_FOR_HMAC_SHA512_IS_REQUIRED = "KEY_AND_DATA_FOR_HMAC_SHA512_IS_REQUIRED";
    String REQUEST_CODE_IS_REQUIRED = "REQUEST_CODE_IS_REQUIRED";
    Map<String, String> VN_PAY_RESPONSE = new HashMap() {{
        put(INVALID_CHECKSUM, "97");
        put(PAYMENT_FAILED, "00");
        put(VNP_TXN_REF_IS_REQUIRED, "01");
        put(VNP_TXN_REF_EXISTED_IN_DATABASE, "02");
        put(REPAIR_REQUEST_NOT_FOUND, "99");
        put(AMOUNT_DOES_NOT_MATCH_TO_INVOICE, "04");
        put(CUSTOMER_PAYMENT_ONLY_USE_WHEN_STATUS_IS_PAYMENT_WAITING, "99");
        put(PAYMENT_SUCCESS, "00");
    }};
}
