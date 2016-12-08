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

import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.constants.UserInstanceStatus;
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.mongodb.client.result.UpdateResult;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.epam.dlab.backendapi.dao.BaseDAO.USER;
import static com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO.EXPLORATORY_NAME;
import static com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO.exploratoryCondition;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_INSTANCES;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.mongodb.client.model.Filters.*;

public class InfrastructureProvisionDAOTest extends DAOTestBase {
    private InfrastructureProvisionDAO dao;

    public InfrastructureProvisionDAOTest() {
        super(Collections.singletonList(USER_INSTANCES));
    }

    @Before
    public void setup() {
        cleanup();
        dao = new InfrastructureProvisionDAO();
        testInjector.injectMembers(dao);
    }

    @BeforeClass
    public static void setupAll() {
        DAOTestBase.setupAll();
    }

    @AfterClass
    public static void teardownAll() {
        DAOTestBase.teardownAll();
    }

    @Test
    public void fetchExploratoryIdSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_2")
                .withExploratoryId("exp2");

        UserInstanceDTO instance3 = new UserInstanceDTO()
                .withUser("user2")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp21");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);
        dao.insertOne(USER_INSTANCES, instance3);

        String testId = dao.fetchExploratoryId("user1", "exp_name_1");
        assertEquals(testId, "exp1");
    }

    @Test
    public void fetchExploratoryIdNotFound() {
        String testId = dao.fetchExploratoryId("user1", "exp_name_1");
        assertEquals(testId, EMPTY);
    }

    @Test(expected = DlabException.class)
    public void fetchExploratoryIdFailWhenMany() throws DlabException {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp2");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        dao.fetchExploratoryId("user1", "exp_name_1");
    }

    @Test
    public void fetchExploratoryStatusSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_2")
                .withStatus("failed");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        UserInstanceStatus status = dao.fetchExploratoryStatus("user1", "exp_name_2");
        assertEquals(status, UserInstanceStatus.FAILED);
    }

    @Test
    public void fetchExploratoryStatusBadValue() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withStatus("creat");

        dao.insertOne(USER_INSTANCES, instance1);
        UserInstanceStatus status = dao.fetchExploratoryStatus("user1", "exp_name_1");
        assertEquals(status, null);
    }

    @Test
    public void fetchExploratoryStatusNotFound() {
        UserInstanceStatus status = dao.fetchExploratoryStatus("user1", "exp_name_1");
        assertEquals(status, null);
    }

    @Test(expected = DlabException.class)
    public void fetchExploratoryStatusFailWhenMany() throws DlabException {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withStatus("created");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        dao.fetchExploratoryStatus("user1", "exp_name_1");
    }

    @Test
    public void insertExploratorySuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created");

        Boolean isInserted = dao.insertExploratory(instance1);
        assertTrue(isInserted);

        long insertedCount = mongoService.getCollection(USER_INSTANCES).count();
        assertEquals(insertedCount, 1L);

        Optional<UserInstanceDTO> testInstance = dao.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance.isPresent());
        assertEquals(instance1.getExploratoryId(), testInstance.get().getExploratoryId());
    }

    @Test
    public void updateExploratoryStatusSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_2")
                .withExploratoryId("exp2")
                .withStatus("created");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        StatusBaseDTO newStatus = new StatusBaseDTO();
        newStatus.setUser("user1");
        newStatus.setExploratoryName("exp_name_1");
        newStatus.setStatus("running");
        UpdateResult result = dao.updateExploratoryStatus(newStatus);

        assertEquals(result.getModifiedCount(), 1);

        Optional<UserInstanceDTO> testInstance = dao.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance.isPresent());
        assertEquals(UserInstanceStatus.of(testInstance.get().getStatus()), UserInstanceStatus.RUNNING);

        Optional<UserInstanceDTO> testInstance2 = dao.findOne(USER_INSTANCES,
                exploratoryCondition(instance2.getUser(), instance2.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance2.isPresent());
        assertEquals(UserInstanceStatus.of(testInstance2.get().getStatus()), UserInstanceStatus.CREATED);
    }

    @Test
    public void updateExploratoryFieldsSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_2")
                .withExploratoryId("exp2")
                .withStatus("created");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        ExploratoryStatusDTO status = new ExploratoryStatusDTO();
        status.setUser("user1");
        status.setExploratoryName("exp_name_1");
        status.setExploratoryId("exp2");
        status.setExploratoryUrl("www.exp2.com");
        status.setStatus("running");
        status.setUptime(new Date(100));

        UpdateResult result = dao.updateExploratoryFields(status);
        assertEquals(result.getModifiedCount(), 1);

        Optional<UserInstanceDTO> testInstance1 = dao.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance1.isPresent());

        UserInstanceDTO instance = testInstance1.get();
        assertEquals(instance.getExploratoryId(), status.getExploratoryId());
        assertEquals(instance.getUrl(), status.getExploratoryUrl());
        assertEquals(instance.getStatus(), status.getStatus());
        assertEquals(instance.getUptime(), status.getUptime());
    }

    @Test
    public void addComputationalSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        dao.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        dao.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);

        UserInstanceDTO testInstance = dao.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class).get();
        assertTrue(testInstance.getResources() != null && testInstance.getResources().size() == 1);

        UserComputationalResourceDTO testComp = testInstance.getResources().get(0);
        assertEquals(comp1.getComputationalName(), testComp.getComputationalName());
        assertEquals(comp1.getComputationalId(), testComp.getComputationalId());
        assertEquals(comp1.getStatus(), testComp.getStatus());
        assertEquals(comp1.getUptime(), testComp.getUptime());
    }
}
