package com.epam.dlab.utils;

import java.text.SimpleDateFormat;

public class Utils {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");


    public static String removeDomain(String username) {
        return username.replaceAll("@.*", "");
    }
}
