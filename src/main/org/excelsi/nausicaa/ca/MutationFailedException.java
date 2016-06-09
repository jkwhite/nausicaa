package org.excelsi.nausicaa.ca;


public class MutationFailedException extends RuntimeException {
    public MutationFailedException(String msg, Throwable t) {
        super(msg, t);
    }

    public MutationFailedException(String msg) {
        super(msg);
    }
}
