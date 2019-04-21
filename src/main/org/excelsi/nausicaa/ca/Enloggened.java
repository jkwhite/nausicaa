package org.excelsi.nausicaa.ca;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class Enloggened {
    private final Logger _log = LoggerFactory.getLogger(getClass());
    private static final Logger LOG = LoggerFactory.getLogger(Enloggened.class);


    protected static Logger log() {
        //return _log;
        return LOG;
    }
}
