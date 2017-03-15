package com.epam.dlab.backendapi.util;

/**
 * Created on 3/15/2017.
 */
public class DateRemoverUtil {

    public static final String ERROR_DATE_FORMAT = "-\\d\\d\\d\\d-\\d\\d\\-\\d\\d\\ \\d\\d\\:\\d\\d\\:\\d\\d";

    public static String removeDateFormErrorMessage(String errorMessage, String errorDateFormat) {
        return errorMessage.replaceAll(errorDateFormat, "");
    }
}
