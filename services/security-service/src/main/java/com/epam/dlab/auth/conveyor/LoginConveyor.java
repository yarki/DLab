package com.epam.dlab.auth.conveyor;

import com.aegisql.conveyor.cart.command.CancelCommand;
import com.aegisql.conveyor.utils.parallel.KBalancedParallelConveyor;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.auth.UserInfoDAO;
import com.epam.dlab.auth.rest.AuthorizedUsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by Mikhail_Teplitskiy on 11/10/2016.
 */
public class LoginConveyor extends KBalancedParallelConveyor<String,LoginStep,UserInfo>{

    private final static Logger LOG = LoggerFactory.getLogger(LoginConveyor.class);

    private UserInfoDAO userInfoDao;

    public LoginConveyor() {
        super(4);
        this.setIdleHeartBeat(1, TimeUnit.SECONDS);
        this.setDefaultBuilderTimeout(10,TimeUnit.SECONDS);
        this.setResultConsumer(res->{
            LOG.debug("UserInfo Build Success: {}",res);
            AuthorizedUsers.getInstance().addUserInfo(res.key, res.product);
            if(userInfoDao != null) {
                userInfoDao.saveUserInfo(res.product);
            } else {
                LOG.warn("UserInfo Build not saved: {}",res);
            }
        });
        this.setScrapConsumer(bin->{
            LOG.error("UserInfo Build Failed: {}",bin);
        });
        this.setBalancingCartAlgorithm(cart->{
            int index = Math.abs(cart.getKey().hashCode() % pf);
            return this.conveyors.subList(index, index+1);
        });
        this.setBalancingCommandAlgorithm(command->{
            int index = Math.abs(command.getKey().hashCode() % pf);
            return this.conveyors.subList(index, index+1);
        });
    }
/*		this.balancingCart = cart -> {
			int index = cart.getKey().hashCode() % pf;
			return this.conveyors.subList(index, index+1);
		};

		this.balancingCommand = command -> {
			int index = command.getKey().hashCode() % pf;
			return this.conveyors.subList(index, index+1);
		};*/
    public void setUserInfoDao(UserInfoDAO userInfoDao) {
        this.userInfoDao = userInfoDao;
    }

    public CompletableFuture<UserInfo> startUserInfoBuild(String token, String username) {
        LOG.debug("startUserInfoBuild {} {} {}",token,username);
        return this.createBuildFuture(token,UserInfoBuilder.supplier(token,username));
    }

    public void cancel(String token) {
        this.addCommand(new CancelCommand<String>(token));
    }
}
