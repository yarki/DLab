/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.util;

/**
 * Created on 3/15/2017.
 */
public class DateRemoverUtil {

    public static final String ERROR_DATE_FORMAT = "\\[Error-\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\]:";
    public static final String ERROR_WITHOUT_DATE_FORMAT = "\\[Error\\]:";

    public static String removeDateFormErrorMessage(String errorMessage, String errorDateFormat, String replaceWith) {
        return errorMessage.replaceAll(errorDateFormat, replaceWith);
    }
}
