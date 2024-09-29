package com.skydevs.tgdrive.exception;

public class FileNotFoundException extends RuntimeException{
    public FileNotFoundException() {

    }

    public FileNotFoundException(String msg) {
        super(msg);
    }
}
