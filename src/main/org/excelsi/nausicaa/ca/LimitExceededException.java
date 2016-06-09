package org.excelsi.nausicaa.ca;


public class LimitExceededException extends MutationFailedException {
    public LimitExceededException(String msg) {
        super(msg);
    }
}
