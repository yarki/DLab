package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.SmartLabel;

import java.util.function.BiConsumer;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public enum LoginStep implements SmartLabel<UserInfoBuilder> {
    ;

    @Override
    public BiConsumer<UserInfoBuilder, Object> get() {
        return null;
    }
}
