package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.SmartLabel;
import java.util.function.BiConsumer;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public enum LoginStep implements SmartLabel<UserInfoBuilder> {
    LDAP_USER_INFO(UserInfoBuilder::ldapUserInfo),
    AWS_USER(UserInfoBuilder::awsUser),
    AWS_KEYS(UserInfoBuilder::awsKeys),
    REMOTE_IP(UserInfoBuilder::remoteIp),
    USER_INFO(UserInfoBuilder::cloneUserInfo),
    ERROR(UserInfoBuilder::failed)
    ;
    BiConsumer<UserInfoBuilder, Object> setter;
    <T> LoginStep (BiConsumer<UserInfoBuilder,T> setter) {
        this.setter = (BiConsumer<UserInfoBuilder, Object>) setter;
    }
    @Override
    public BiConsumer<UserInfoBuilder, Object> get() {
        return setter;
    }
}
