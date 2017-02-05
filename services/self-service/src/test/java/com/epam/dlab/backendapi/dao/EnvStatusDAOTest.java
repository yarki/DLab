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
package com.epam.dlab.backendapi.dao;

import static com.epam.dlab.backendapi.dao.BaseDAO.USER;
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.EXPLORATORY_NAME;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_AWS_CREDENTIALS;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_INSTANCES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.epam.dlab.backendapi.core.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.core.UserInstanceDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.dto.status.EnvResourceList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexOptions;

@Ignore
public class EnvStatusDAOTest extends DAOTestBase {
    private ExploratoryDAO infExpDAO;
    private ComputationalDAO infCompDAO;
    private EnvStatusDAO envDAO;
    
    public EnvStatusDAOTest() {
        super(Collections.singletonList(USER_INSTANCES));
    }

    @Before
    public void setup() {
        cleanup();

        mongoService.createCollection(USER_INSTANCES);
        mongoService.getCollection(USER_INSTANCES)
        		.createIndex(new BasicDBObject(USER, 1).append(EXPLORATORY_NAME, 2),
                new IndexOptions().unique(true));

        infExpDAO = new ExploratoryDAO();
        testInjector.injectMembers(infExpDAO);
        infCompDAO = new ComputationalDAO();
        testInjector.injectMembers(infCompDAO);
        envDAO = new EnvStatusDAO();
        testInjector.injectMembers(envDAO);
    }

    @BeforeClass
    public static void setupAll() {
        DAOTestBase.setupAll();
    }

    @AfterClass
    public static void teardownAll() {
        //DAOTestBase.teardownAll();
    }
    
    private static class EdgeInfo {
        @JsonProperty("instance_id")
        private String instanceId;
        @JsonProperty
        private String status;
        @JsonProperty("ip")
        private String privateIp;
    }

    @Test
    public void findEnvResources() {
    	final String user = "user1";
    	
    	// Add EDGE
    	EdgeInfo edge = new EdgeInfo();
    	edge.instanceId = "instance0";
    	edge.privateIp = "privIp";
    	edge.status = "stopped";
    	infExpDAO.insertOne(USER_AWS_CREDENTIALS, edge, user);
    	
    	// Add exploratory
        UserInstanceDTO exp1 = new UserInstanceDTO()
                .withUser(user)
                .withExploratoryName("exp1")
                .withUptime(new Date(100));
        infExpDAO.insertOne(USER_INSTANCES, exp1);
        // Set status and instance_id for exploratory
        ExploratoryStatusDTO expStatus = new ExploratoryStatusDTO()
        		.withUser(user)
        		.withExploratoryName("exp1")
        		.withInstanceId("instance1")
        		.withStatus("runnig");
        infExpDAO.updateExploratoryFields(expStatus);

        // Add computational
        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withInstanceId("instance11")
                .withStatus("creating")
                .withUptime(new Date(200));
        boolean inserted = infCompDAO.addComputational(exp1.getUser(), exp1.getExploratoryName(), comp1);
        assertTrue(inserted);

        // Check selected resources
        EnvResourceList resList = envDAO.findEnvResources(user, true);

        assertEquals(2, resList.getHostList().size());
        assertEquals(1, resList.getClusterList().size());

        assertEquals(edge.instanceId, resList.getHostList().get(0).getId());
        assertEquals(edge.status, resList.getHostList().get(0).getStatus());
        assertEquals(expStatus.getInstanceId(), resList.getHostList().get(1).getId());
        assertEquals(expStatus.getStatus(), resList.getHostList().get(1).getStatus());
        assertEquals(comp1.getInstanceId(), resList.getClusterList().get(0).getId());
        assertEquals(comp1.getStatus(), resList.getClusterList().get(0).getStatus());
    }

}
