package com.fpt.flix.flix_app.models.errors;


public class GeneralException extends RuntimeException{
    private String message;

    public GeneralException() {}

    public GeneralException(String msg)
    {
        super(msg);
        this.message = msg;
    }
}
