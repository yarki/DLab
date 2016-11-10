package com.epam.dlab.auth.conveyor;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public class LoginConveyor {

    private final static LoginConveyor loginConveyor = new LoginConveyor();

    public static LoginConveyor getInstance() {
        return loginConveyor;
    }

    private LoginConveyor() {

    }

}
