package com.fu.flix.dto.error;


public class GeneralException extends RuntimeException{
    private String message;

    public GeneralException() {}

    public GeneralException(String msg)
    {
        super(msg);
        this.message = msg;
    }
}
