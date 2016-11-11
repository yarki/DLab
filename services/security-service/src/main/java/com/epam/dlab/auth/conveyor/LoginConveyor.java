package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.AssemblingConveyor;
import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.parallel.LBalancedParallelConveyor;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.UserInfoDAO;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.epam.dlab.auth.conveyor.LoginStep.*;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public class LoginConveyor {

    private final static Logger LOG = LoggerFactory.getLogger(LoginConveyor.class);

    private final LBalancedParallelConveyor<String,LoginStep,UserInfo> conveyor;

    private UserInfoDAO userInfoDao;

    private final AssemblingConveyor<String,LoginStep,UserInfo> finalCollector;
    private final AssemblingConveyor<String,LoginStep,UserInfo> userInfoCollector;
    private final AssemblingConveyor<String,LoginStep,UserInfo> groupInfoCollector;
    private final AssemblingConveyor<String,LoginStep,UserInfo> awsInfoCollector;

    public LoginConveyor() {
        super();
        finalCollector = new AssemblingConveyor<>();
        finalCollector.setName("LoginConveyor final collector");
        finalCollector.setDefaultBuilderTimeout(5, TimeUnit.SECONDS);
        finalCollector.setIdleHeartBeat(1,TimeUnit.SECONDS);
        finalCollector.setResultConsumer(bin->{
            AuthorizedUsers.getInstance().addUserInfo(bin.key, bin.product);
            if(userInfoDao != null) {
                userInfoDao.saveUserInfo(bin.product);
            } else {
                LOG.warn("User Info is not saved for {}",bin.product);
            }
        });
        finalCollector.setScrapConsumer(bin->{
            LOG.error("Login Conveyor Error: {}",bin);
            if(bin.error != null) {
                bin.error.printStackTrace();
            }
        });
        finalCollector.setReadinessEvaluator(b->{
            return UserInfoBuilder.testMask(b,UserInfoBuilder.READYNESS_MASK);
        });

        finalCollector.acceptLabels(MERGE_USER_INFO, MERGE_GROUP_INFO, MERGE_AWS_INFO, REMOTE_IP);

        userInfoCollector = finalCollector.detach();
        userInfoCollector.setName("LoginConveyor user Info collector");
        userInfoCollector.acceptLabels(USER_INFO);
        userInfoCollector.forwardPartialResultTo(MERGE_USER_INFO,finalCollector);
        userInfoCollector.setReadinessEvaluator(b->{
            return UserInfoBuilder.testMask(b,UserInfoBuilder.FIRST_LAST_SET);
        });
        userInfoCollector.setScrapConsumer(bin->{
            LOG.error("userInfoCollector Conveyor Error: {}",bin);
            if(bin.error != null) {
                bin.error.printStackTrace();
            }
        });
        userInfoCollector.setReadinessEvaluator(b->{
            return UserInfoBuilder.testMask(b,UserInfoBuilder.FIRST_LAST_SET);
        });
        groupInfoCollector = finalCollector.detach();
        groupInfoCollector.setName("LoginConveyor group Info collector");
        groupInfoCollector.acceptLabels(GROUP_INFO);
        groupInfoCollector.forwardPartialResultTo(MERGE_GROUP_INFO,finalCollector);
        groupInfoCollector.setReadinessEvaluator(b->{
            return UserInfoBuilder.testMask(b,UserInfoBuilder.ROLE_SET);
        });
        groupInfoCollector.setScrapConsumer(bin->{
            LOG.error("groupInfoCollector Conveyor Error: {}",bin);
            if(bin.error != null) {
                bin.error.printStackTrace();
            }
        });
        awsInfoCollector = finalCollector.detach();
        awsInfoCollector.setName("LoginConveyor AWS Info collector");
        awsInfoCollector.acceptLabels(AWS_INFO);
        awsInfoCollector.forwardPartialResultTo(MERGE_AWS_INFO,finalCollector);
        awsInfoCollector.setReadinessEvaluator(b->{
            return UserInfoBuilder.testMask(b,UserInfoBuilder.AWS_USER_SET);
        });
        awsInfoCollector.setScrapConsumer(bin->{
            LOG.error("awsInfoCollector Conveyor Error: {}",bin);
            if(bin.error != null) {
                bin.error.printStackTrace();
            }
        });

        conveyor = new LBalancedParallelConveyor<>(finalCollector, userInfoCollector, groupInfoCollector, awsInfoCollector);

    }

    public void setUserInfoDao(UserInfoDAO userInfoDao) {
        this.userInfoDao = userInfoDao;
    }

    public CompletableFuture<UserInfo> startUserInfoBuild(String token, String username) {
        UserInfo ui = UserInfoBuilder.supplier(token,username).get().get();
        LOG.debug("startUserInfoBuild {} {} {}",token,username,ui);
        return conveyor.createBuildFuture(token,()->new UserInfoBuilder(token,username));
//        return conveyor.createBuildFuture(token,UserInfoBuilder.supplier(token,username));
    }

    public void add(String token, Object value, LoginStep step) {
        CompletableFuture<Boolean> msgFuture = conveyor.add(token,value,step);
        LOG.debug("value added {} {} {} {}",token,value,step,msgFuture);
    }

    public void addClosure(String token, Consumer<UserInfoBuilder> consumer, LoginStep step) {
        switch(step) {
            case USER_INFO:
            case GROUP_INFO:
            case AWS_INFO:
                conveyor.add(token,consumer,step);
                LOG.debug("closure added {} {} {} {}",token,consumer,step);
                break;
            default:
                throw new RuntimeException("Unsupported value for step "+step);
        }
    }


    public void cancel(String token) {
        conveyor.addCommand(new CancelCommand<String>(token));
    }
}
