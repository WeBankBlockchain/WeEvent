package com.webank.weevent.governance.common;

public enum ErrorCode {

    SUCCESS(0, "success"),
    BROKER_CONNECT_ERROR(100100, "broker connect error!"),
    SEND_EMAIL_ERROR(100102, "send email failed"),
    ACCESS_DENIED(100103, "access denied"),
    BUILD_URL_METHOD(100104, "build url method fail"),
    ILLEGAL_INPUT(100105, "illegal input"),
    PROCESS_CONNECT_ERROR(100106, "process connect error!"),
    NO_MAILBOX_CONFIGURED(100107, "no mailbox configured"),
    BROKER_REPEAT(100108, "the brokerUrl already has a record in the database"),
    TOPIC_EXISTS(100109, "topic already exists"),
    HTTP_REQUEST_EXECUTE_ERROR(100110, "http request execute failed"),

    TRANSPORT_NOT_EXISTS(100120, "the file transport not exists"),
    TRANSPORT_ALREADY_EXISTS(100121, "the file transport already exists"),
    TRANSPORT_ROLE_INVALID(100122, "the file transport role should be 0 or 1"),
    TRANSPORT_OVERWRITE_INVALID(100123, "the file transport overwrite should be 0 or 1"),
    TRANSPORT_NAME_IS_NULL(100124, "the file transport name is null"),

    FILE_NAME_IS_NULL(102000, "the upload file name is null"),
    FILE_ID_IS_NULL(102001, "the upload fileId is null"),
    FILE_ID_ILLEGAL(102002, "the upload fileId is illegal"),
    FILE_SIZE_ILLEGAL(102003, "the upload file size is illegal"),
    CHUNK_IDX_ILLEGAL(102005, "the upload file chunk index is illegal"),
    CHUNK_DATA_ILLEGAL(102006, "the upload file chunk data is illegal"),
    FILE_CHUNK_INDEX_ILLEGAL(102007, "the upload file chunk idx is illegal"),
    FILE_CHUNK_DATA_IS_NULL(102008, "the upload file chunk data is null"),
    FILE_UPLOAD_FAILED(102009, "file upload failed"),
    FILE_DOWNLOAD_ERROR(102011, "file download failed"),

    PARSE_CHUNK_REQUEST_ERROR(102020, "parse chunk request error"),
    ;


    /**
     * error code
     */
    private int code;

    /**
     * error message
     */
    private String codeDesc;

    /**
     * Error Code Constructor.
     *
     * @param code The ErrorCode
     * @param codeDesc The ErrorCode Description
     */
    ErrorCode(int code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    /**
     * Get the Error Code.
     *
     * @return the ErrorCode
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the ErrorCode Description.
     *
     * @return the ErrorCode Description
     */
    public String getCodeDesc() {
        return codeDesc;
    }
}
