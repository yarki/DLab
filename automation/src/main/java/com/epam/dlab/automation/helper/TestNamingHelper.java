package com.epam.dlab.automation.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestNamingHelper {

    public static String generateRandomValue(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddhmmss");
        return "AutoTest" + simpleDateFormat.format(new Date());
    }
}
