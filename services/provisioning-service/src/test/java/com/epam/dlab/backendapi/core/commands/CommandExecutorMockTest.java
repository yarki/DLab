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

package com.epam.dlab.backendapi.core.commands;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutorMockTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutorMock.class);

    private ICommandExecutor getCommandExecutor() {
    	return new CommandExecutorMock();
    }
    
    @Test
    public void describe()  {
    	String cmd =
    		"docker run " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    		"-e \"conf_resource=notebook\" " +
    		"-e \"request_id=28ba67a4-b2ee-4753-a406-892977089ad9\" " +
    		"docker.dlab-zeppelin:latest --action describe";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
    
    @Test
    public void edgeCreate()  {
    	String cmd =
    		"echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"user@epam.com\",\"edge_user_name\":\"user\"," +
    		"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    		"\"aws_vpc_id\":\"vpc-83c469e4\",\"aws_subnet_id\":\"subnet-22db937a\",\"aws_security_groups_ids\":\"sg-4d42dc35\"}' | " +
    		"docker run -i --name user_create_exploratory_edge_1487309918496 " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/edge:/logs/edge " +
    		"-e \"conf_resource=edge\" " +
    		"-e \"request_id=b8267ae6-07b0-44ef-a489-7714b20cf0a4\" " +
    		"-e \"conf_key_name=BDCC-DSS-POC\" " +
    		"docker.dlab-edge --action create";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
    
    @Test
    public void notebookCreate()  {
    	String cmd =
    		"echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"user@epam.com\",\"edge_user_name\":\"user\"," +
    		"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    		"\"exploratory_name\":\"useinxz1\",\"application\":\"zeppelin\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    		"\"aws_notebook_instance_type\":\"t2.medium\",\"aws_security_groups_ids\":\"sg-4d42dc35\"}' | " +
    		"docker run -i --name user_create_exploratory_useinxz1_1487312574572 " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    		"-e \"conf_resource=notebook\" " +
    		"-e \"request_id=f720f30b-5949-4919-a50b-ce7af58d6fe9\" " +
    		"-e \"conf_key_name=BDCC-DSS-POC\" " +
    		"docker.dlab-zeppelin --action create";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
    
    @Test
    public void notebookStop()  {
    	String cmd =
    		"echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"user@epam.com\",\"edge_user_name\":\"user\"," +
    		"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\"," +
    		"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    		"\"notebook_instance_name\":\"usein1120v13-user-nb-useinxz1-78af3\",\"conf_key_dir\":\"/root/keys\"}' | " +
    		"docker run -i --name user_stop_exploratory_useinxz1_1487315364165 " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    		"-e \"conf_resource=notebook\" " +
    		"-e \"request_id=33998e05-7781-432e-b748-bf3f0e7f9342\" " +
    		"-e \"conf_key_name=BDCC-DSS-POC\" " +
    		"docker.dlab-zeppelin --action stop";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
    
    @Test
    public void notebookStart()  {
    	String cmd =
    		"echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"user@epam.com\",\"edge_user_name\":\"user\"," +
    		"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    		"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    		"\"notebook_instance_name\":\"usein1120v13-user-nb-useinxz1-78af3\"}' | " +
    		"docker run -i --name user_start_exploratory_useinxz1_1487316756857 " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    		"-e \"conf_resource=notebook\" " +
    		"-e \"request_id=d50b9d20-1b1a-415f-8e47-ed0aca029e73\" " +
    		"-e \"conf_key_name=BDCC-DSS-POC\" " +
    		"docker.dlab-zeppelin --action start";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
    
    @Test
    public void notebookTerminate()  {
    	String cmd =
    		"echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"user@epam.com\",\"edge_user_name\":\"user\"," +
    		"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    		"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    		"\"notebook_instance_name\":\"usein1120v13-user-nb-useinxz1-78af3\"}' | " +
    		"docker run -i --name user_terminate_exploratory_useinxz1_1487318040180 " +
    		"-v /home/ubuntu/keys:/root/keys " +
    		"-v /opt/dlab/tmp/result:/response " +
    		"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    		"-e \"conf_resource=notebook\" " +
    		"-e \"request_id=de217441-9757-4c4e-b020-548f66b58e00\" " +
    		"-e \"conf_key_name=BDCC-DSS-POC\" " +
    		"docker.dlab-zeppelin --action terminate";
    	getCommandExecutor().executeAsync("user", "uuid", cmd);
    }
}
