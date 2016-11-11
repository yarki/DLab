package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.SmartLabel;

import java.util.function.BiConsumer;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public enum LoginStep implements SmartLabel<UserInfoBuilder> {

    USER_INFO(UserInfoBuilder::userInfo),
    GROUP_INFO(UserInfoBuilder::groupInfo),
    AWS_INFO(UserInfoBuilder::awsInfo),

    MERGE_USER_INFO(UserInfoBuilder::mergeUserInfo),
    MERGE_GROUP_INFO(UserInfoBuilder::mergeGroupInfo),
    MERGE_AWS_INFO(UserInfoBuilder::mergeAwsUser),

    REMOTE_IP(UserInfoBuilder::remoteIp),
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
