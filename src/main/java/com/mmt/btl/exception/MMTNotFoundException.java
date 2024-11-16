package com.mmt.btl.exception;

public class MMTNotFoundException extends RuntimeException {
    final private static String MESSAGE_DEFAULT = "User Not Found...!";

    public MMTNotFoundException(){
        super(MESSAGE_DEFAULT);
    }

    public MMTNotFoundException(String mes){
        super(mes);
    }
}