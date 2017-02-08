package com.epam.dlab.automation.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created on 2/8/2017.
 */
public class TestNamingHelper {

    public static String generateRandomValue(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddmmss");
        return "AutoTest" + simpleDateFormat.format(new Date());
    }
}
