package com.epam.dlab.automation.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestNamingHelper {

    public static String generateRandomValue(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddhmmss");
        return "AutoTest" + simpleDateFormat.format(new Date());
    }

    public static String generateRandomValue(String notebokTemplateName){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMddhmmss");
        return "AutoTest_" + notebokTemplateName.split(" ")[0]+"_"+  simpleDateFormat.format(new Date());
    }
}
