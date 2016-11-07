package com.epam.dlab.utils;

public class UsernameUtils {

    public static String removeDomain(String username) {
        return username.replaceAll("@.*", "");
    }
}
