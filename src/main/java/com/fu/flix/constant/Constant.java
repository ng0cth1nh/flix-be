package com.fu.flix.constant;

import java.util.HashMap;
import java.util.Map;

public interface Constant {
    String USER_ID = "userId";
    String ROLES = "roles";
    String BEARER = "Bearer ";
    String INVALID_REFRESH_TOKEN = "INVALID_REFRESH_TOKEN";
    String REFRESH_TOKEN_MISSING = "REFRESH_TOKEN_MISSING";
    String NEW_ACCOUNT_VALID = "NEW_ACCOUNT_VALID";
    String ACCOUNT_EXISTED = "ACCOUNT_EXISTED";
    String INVALID_PASSWORD = "INVALID_PASSWORD";
    String REDIS_KEY_OTP_PREFIX = "OTP_";
    String INVALID_EXPERIENCE_DESCRIPTION = "INVALID_EXPERIENCE_DESCRIPTION";
    String INVALID_PHONE_NUMBER = "INVALID_PHONE_NUMBER";
    String INVALID_NAME = "INVALID_NAME";
    String INVALID_FEEDBACK_TYPE = "INVALID_FEEDBACK_TYPE";
    String INVALID_IMAGE = "INVALID_IMAGE";
    String INVALID_OTP = "INVALID_OTP";
    String INVALID_CITY = "INVALID_CITY";
    String USER_NOT_FOUND = "USER_NOT_FOUND";
    String BAN_USER_SUCCESS = "BAN_USER_SUCCESS";
    String INVALID_BAN_REASON = "INVALID_BAN_REASON";
    String THIS_ACCOUNT_HAS_BEEN_BANNED = "THIS_ACCOUNT_HAS_BEEN_BANNED";
    String INVALID_DISTRICT = "INVALID_DISTRICT";
    String JUST_CAN_BAN_USER_ROLE_ARE_CUSTOMER_OR_REPAIRER_OR_PENDING_REPAIRER = "JUST_CAN_BAN_USER_ROLE_ARE_CUSTOMER_OR_REPAIRER_OR_PENDING_REPAIRER";
    String INVALID_COMMUNE = "INVALID_COMMUNE";
    String STREET_ADDRESS_IS_REQUIRED = "STREET_ADDRESS_IS_REQUIRED";
    String WRONG_LOCAL_DATE_TIME_FORMAT = "WRONG_LOCAL_DATE_TIME_FORMAT";
    String EXPECT_FIXING_DAY_IS_REQUIRED = "EXPECT_FIXING_DAY_IS_REQUIRED";
    String WRONG_LOCAL_DATE_FORMAT = "WRONG_LOCAL_DATE_FORMAT";
    String INVALID_FULL_NAME = "INVALID_FULL_NAME";
    String DOB_MUST_BE_LESS_THAN_OR_EQUAL_TODAY = "DOB_MUST_BE_LESS_THAN_OR_EQUAL_TODAY";
    String INVALID_STREET_ADDRESS = "INVALID_STREET_ADDRESS";
    String USER_NOT_HOLD_VOUCHER = "USER_NOT_HOLD_VOUCHER";
    String CREATE_REPAIR_REQUEST_SUCCESSFUL = "CREATE_REPAIR_REQUEST_SUCCESSFUL";
    String INVALID_PAYMENT_METHOD = "INVALID_PAYMENT_METHOD";
    String VOUCHER_EXPIRED = "VOUCHER_EXPIRED";
    String UPDATE_ADMIN_PROFILE_SUCCESS = "UPDATE_ADMIN_PROFILE_SUCCESS";
    String PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_1 = "PAGE_SIZE_MUST_BE_GREATER_OR_EQUAL_1";
    String PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0 = "PAGE_NUMBER_MUST_BE_GREATER_OR_EQUAL_0";
    String CAN_NOT_CREATE_NEW_REQUEST_WHEN_HAVE_OTHER_PAYMENT_WAITING_REQUEST = "CAN_NOT_CREATE_NEW_REQUEST_WHEN_HAVE_OTHER_PAYMENT_WAITING_REQUEST";
    String VOUCHER_BEFORE_EFFECTIVE_DATE = "VOUCHER_BEFORE_EFFECTIVE_DATE";
    String EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS = "EXPECT_FIXING_DAY_MUST_START_AFTER_1_HOURS_AND_BEFORE_30_DAYS";
    String INVALID_REQUEST_CODE = "INVALID_REQUEST_CODE";
    String INVALID_TITLE = "INVALID_TITLE";
    String FEEDBACK_ID_IS_REQUIRED = "FEEDBACK_ID_IS_REQUIRED";
    String INVALID_FEEDBACK_ID = "INVALID_FEEDBACK_ID";
    String FEEDBACK_NOT_FOUND = "FEEDBACK_NOT_FOUND";
    String RESPONSE_FEEDBACK_SUCCESS = "RESPONSE_FEEDBACK_SUCCESS";
    String CREATE_FEEDBACK_SUCCESS = "CREATE_FEEDBACK_SUCCESS";
    String INVALID_REPAIRER = "INVALID_REPAIRER";
    String INVALID_DESCRIPTION = "INVALID_DESCRIPTION";
    String USER_ID_IS_REQUIRED = "USER_ID_IS_REQUIRED";
    String INVALID_ACCESSORY = "INVALID_ACCESSORY";
    String ACCEPT_CV_SUCCESS = "ACCEPT_CV_SUCCESS";
    String REPAIRER_ID_IS_REQUIRED = "REPAIRER_ID_IS_REQUIRED";
    String OFFSET_MUST_BE_GREATER_OR_EQUAL_0 = "OFFSET_MUST_BE_GREATER_OR_EQUAL_0";
    String LIMIT_MUST_BE_GREATER_OR_EQUAL_0 = "LIMIT_MUST_BE_GREATER_OR_EQUAL_0";
    String SUB_SERVICE_ID_IS_REQUIRED = "SUB_SERVICE_ID_IS_REQUIRED";
    String ACCESSORY_ID_IS_REQUIRED = "ACCESSORY_ID_IS_REQUIRED";
    String EXTRA_SERVICE_IS_REQUIRED = "EXTRA_SERVICE_IS_REQUIRED";
    String LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT = "LIST_EXTRA_SERVICES_CONTAIN_INVALID_ELEMENT";
    String SUB_SERVICE_NOT_FOUND = "SUB_SERVICE_NOT_FOUND";
    String ACCESSORY_NOT_FOUND = "ACCESSORY_NOT_FOUND";
    String PUT_SUB_SERVICE_TO_INVOICE_SUCCESS = "PUT_SUB_SERVICE_TO_INVOICE_SUCCESS";
    String PUT_ACCESSORIES_TO_INVOICE_SUCCESS = "PUT_ACCESSORIES_TO_INVOICE_SUCCESS";
    String PUT_EXTRA_SERVICE_TO_INVOICE_SUCCESS = "PUT_EXTRA_SERVICE_TO_INVOICE_SUCCESS";
    String EXCEEDED_COMMENT_LENGTH_ALLOWED = "EXCEEDED_COMMENT_LENGTH_ALLOWED";
    String EXCEEDED_DESCRIPTION_LENGTH_ALLOWED = "EXCEEDED_DESCRIPTION_LENGTH_ALLOWED";
    String INVALID_EXPERIENCE_YEARS = "INVALID_EXPERIENCE_YEARS";
    String FRONT_IMAGE_MUST_BE_IMAGE_OR_PDF = "FRONT_IMAGE_MUST_BE_IMAGE_OR_PDF";
    String BACK_SIDE_IMAGE_MUST_BE_IMAGE_OR_PDF = "BACK_SIDE_IMAGE_MUST_BE_IMAGE_OR_PDF";
    String CERTIFICATE_IS_REQUIRED = "CERTIFICATE_IS_REQUIRED";
    String CERTIFICATE_FILE_MUST_BE_IMAGE_OR_PDF = "CERTIFICATE_FILE_MUST_BE_IMAGE_OR_PDF";
    String INVALID_IDENTITY_CARD_NUMBER = "INVALID_IDENTITY_CARD_NUMBER";
    String GENDER_IS_REQUIRED = "GENDER_IS_REQUIRED";
    String INVALID_DATE_OF_BIRTH = "INVALID_DATE_OF_BIRTH";
    String FRONT_IMAGE = "FRONT_IMAGE";
    String INVALID_CATEGORY_NAME = "INVALID_CATEGORY_NAME";
    String INVALID_SERVICE_NAME = "INVALID_SERVICE_NAME";
    String CREATE_CATEGORY_SUCCESS = "CREATE_CATEGORY_SUCCESS";
    String BACK_SIDE_IMAGE = "BACK_SIDE_IMAGE";
    String INVALID_INSPECTION_PRICE = "INVALID_INSPECTION_PRICE";
    String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    String UPDATE_SERVICE_SUCCESS = "UPDATE_SERVICE_SUCCESS";
    String CREATE_SERVICE_SUCCESS = "CREATE_SERVICE_SUCCESS";
    String UPDATE_CATEGORY_SUCCESS = "UPDATE_CATEGORY_SUCCESS";
    String CATEGORY_ID_IS_REQUIRED = "CATEGORY_ID_IS_REQUIRED";
    String IDENTITY_CARD_NUMBER_EXISTED = "IDENTITY_CARD_NUMBER_EXISTED";
    String IDENTITY_CARD_TYPE_MUST_BE_CCCD_OR_CMND = "IDENTITY_CARD_TYPE_MUST_BE_CCCD_OR_CMND";
    String JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED = "JUST_CAN_CONFIRM_FIXING_WHEN_REQUEST_STATUS_APPROVED";
    String USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST = "USER_DOES_NOT_HAVE_PERMISSION_TO_CONFIRM_FIXING_THIS_REQUEST";
    String CREATE_PAYMENT_URL_SUCCESS = "CREATE_PAYMENT_URL_SUCCESS";
    String PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    String JUST_CAN_GET_FIXED_SERVICES_WHEN_REQUEST_STATUS_IS_PAYMENT_WAITING_OR_DONE_OR_FIXING = "JUST_CAN_GET_FIXED_SERVICES_WHEN_REQUEST_STATUS_IS_PAYMENT_WAITING_OR_DONE_OR_FIXING";
    String CUSTOMER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE = "CUSTOMER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_GET_FIXED_SERVICES_FOR_THIS_INVOICE";
    String PAYMENT_FAILED = "PAYMENT_FAILED";
    String CONFIRM_FIXING_SUCCESS = "CONFIRM_FIXING_SUCCESS";
    String INVALID_REPAIRER_SUGGESTION_TYPE = "INVALID_REPAIRER_SUGGESTION_TYPE";
    String SERVICE_IDS_ARE_REQUIRED = "SERVICE_IDS_ARE_REQUIRED";
    String SERVICE_ID_IS_REQUIRED = "SERVICE_ID_IS_REQUIRED";
    String SERVICE_NOT_FOUND = "SERVICE_NOT_FOUND";
    String INVALID_LOCATION_TYPE = "INVALID_LOCATION_TYPE";
    String START_DATE_AND_END_DATE_ARE_REQUIRED = "START_DATE_AND_END_DATE_ARE_REQUIRED";
    String VNP_TXN_REF_IS_REQUIRED = "VNP_TXN_REF_IS_REQUIRED";
    String VNP_TXN_REF_EXISTED_IN_DATABASE = "VNP_TXN_REF_EXISTED_IN_DATABASE";
    String REPAIR_REQUEST_NOT_FOUND = "REPAIR_REQUEST_NOT_FOUND";
    String AMOUNT_DOES_NOT_MATCH_TO_INVOICE = "AMOUNT_DOES_NOT_MATCH_TO_INVOICE";
    String INVALID_CHECKSUM = "INVALID_CHECKSUM";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_EXTRA_SERVICE_FOR_THIS_INVOICE = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_EXTRA_SERVICE_FOR_THIS_INVOICE";
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
    String INVALID_VOUCHER = "INVALID_VOUCHER";
    String INVALID_FEEDBACK_STATUS = "INVALID_FEEDBACK_STATUS";
    String INVALID_RESPONSE = "INVALID_RESPONSE";
    String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    String INVALID_SUB_SERVICE = "INVALID_SUB_SERVICE";
    String INVALID_ADDRESS = "INVALID_ADDRESS";
    String ADDRESS_ID_IS_REQUIRED = "ADDRESS_ID_IS_REQUIRED";
    String CHOOSING_MAIN_ADDRESS_SUCCESS = "CHOOSING_MAIN_ADDRESS_SUCCESS";
    String INVALID_STATUS = "INVALID_STATUS";
    String FILE_MUST_BE_IMAGE = "FILE_MUST_BE_IMAGE";
    String FILE_MUST_BE_IMAGE_OR_PDF = "FILE_MUST_BE_IMAGE_OR_PDF";
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
    String SEND_FORGOT_PASSWORD_OTP_SUCCESS = "SEND_FORGOT_PASSWORD_OTP_SUCCESS";
    String SEND_SMS_OTP_FAILED = "SEND_SMS_OTP_FAILED";
    String RESET_PASSWORD_SUCCESS = "RESET_PASSWORD_SUCCESS";
    String SAVE_OTP_FAILED = "SAVE_OTP_FAILED";
    String PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER = "PAYMENT_METHOD_NOT_VALID_FOR_THIS_VOUCHER";
    String INVALID_KEY_WORD = "INVALID_KEY_WORD";
    String COMMENT_EXISTED = "COMMENT_EXISTED";
    String CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE = "CAN_NOT_COMMENT_WHEN_STATUS_NOT_DONE";
    String COMMENT_SUCCESS = "COMMENT_SUCCESS";
    String RATING_IS_REQUIRED = "RATING_IS_REQUIRED";
    String RATING_MUST_IN_RANGE_1_TO_5 = "RATING_MUST_IN_RANGE_1_TO_5";
    String USER_AND_REQUEST_CODE_DOES_NOT_MATCH = "USER_AND_REQUEST_CODE_DOES_NOT_MATCH";
    String APPROVAL_REQUEST_SUCCESS = "APPROVAL_REQUEST_SUCCESS";
    String JUST_CAN_ACCEPT_PENDING_REQUEST = "JUST_CAN_ACCEPT_PENDING_REQUEST";
    String CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING = "CAN_NOT_ACCEPT_REQUEST_WHEN_ON_ANOTHER_FIXING";
    String CAN_NOT_CONFIRM_FIXING_WHEN_ON_ANOTHER_FIXING = "CAN_NOT_CONFIRM_FIXING_WHEN_ON_ANOTHER_FIXING";
    String ACCESS_DENIED = "ACCESS_DENIED";
    String CONFIRM_REGISTER_SUCCESS = "CONFIRM_REGISTER_SUCCESS";
    String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    String WRONG_DATA_TYPE = "WRONG_DATA_TYPE";
    String BALANCE_MUST_GREATER_THAN_OR_EQUAL_ = "BALANCE_MUST_GREATER_THAN_OR_EQUAL_";
    String USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST = "USER_DOES_NOT_HAVE_PERMISSION_TO_CANCEL_THIS_REQUEST";
    String INVALID_CATEGORY_ID = "INVALID_CATEGORY_ID";
    String INVALID_CATEGORY = "INVALID_CATEGORY";
    String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    String INVALID_SUB_SERVICE_NAME = "INVALID_SUB_SERVICE_NAME";
    String INVALID_PRICE = "INVALID_PRICE";
    String CREATE_ACCESSORY_SUCCESS = "CREATE_ACCESSORY_SUCCESS";
    String UPDATE_ACCESSORY_SUCCESS = "UPDATE_ACCESSORY_SUCCESS";
    String INVALID_INSURANCE = "INVALID_INSURANCE";
    String INVALID_ACCESSORY_NAME = "INVALID_ACCESSORY_NAME";
    String UPDATE_SUB_SERVICE_SUCCESS = "UPDATE_SUB_SERVICE_SUCCESS";
    String CREATE_SUB_SERVICE_SUCCESS = "CREATE_SUB_SERVICE_SUCCESS";
    String JUST_CAN_ADD_SUB_SERVICES_WHEN_REQUEST_STATUS_IS_FIXING = "JUST_CAN_ADD_SUB_SERVICES_WHEN_REQUEST_STATUS_IS_FIXING";
    String JUST_CAN_ADD_ACCESSORIES_WHEN_REQUEST_STATUS_IS_FIXING = "JUST_CAN_ADD_ACCESSORIES_WHEN_REQUEST_STATUS_IS_FIXING";
    String JUST_CAN_ADD_EXTRA_SERVICE_WHEN_REQUEST_STATUS_IS_FIXING = "JUST_CAN_ADD_EXTRA_SERVICE_WHEN_REQUEST_STATUS_IS_FIXING";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_CREATE_INVOICE_FOR_THIS_REQUEST = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_CREATE_INVOICE_FOR_THIS_REQUEST";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_SUB_SERVICES_FOR_THIS_INVOICE = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_SUB_SERVICES_FOR_THIS_INVOICE";
    String REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_ACCESSORIES_FOR_THIS_INVOICE = "REPAIRER_DOES_NOT_HAVE_PERMISSION_TO_ADD_ACCESSORIES_FOR_THIS_INVOICE";
    String CREATE_INVOICE_SUCCESS = "CREATE_INVOICE_SUCCESS";
    String INVALID_IP_ADDRESS = "INVALID_IP_ADDRESS";
    String KEY_AND_DATA_FOR_HMAC_SHA512_IS_REQUIRED = "KEY_AND_DATA_FOR_HMAC_SHA512_IS_REQUIRED";
    String REQUEST_CODE_IS_REQUIRED = "REQUEST_CODE_IS_REQUIRED";
    String ORDER_INFO_IS_REQUIRED = "ORDER_INFO_IS_REQUIRED";
    String BANK_CODE_IS_REQUIRED = "BANK_CODE_IS_REQUIRED";
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
