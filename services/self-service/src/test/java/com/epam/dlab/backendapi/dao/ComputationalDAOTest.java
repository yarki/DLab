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
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.exploratoryCondition;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_INSTANCES;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.backendapi.core.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.core.UserInstanceDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.UpdateResult;

@Ignore
public class ComputationalDAOTest extends DAOTestBase {
    private ExploratoryDAO infExpDAO;
    private ComputationalDAO infCompDAO;
    
    public ComputationalDAOTest() {
        super(USER_INSTANCES);
    }

    @Before
    public void setup() {
        cleanup();

        mongoService.createCollection(USER_INSTANCES);
        mongoService.getCollection(USER_INSTANCES).createIndex(new BasicDBObject(USER, 1).append(EXPLORATORY_NAME, 2),
                new IndexOptions().unique(true));

        infExpDAO = new ExploratoryDAO();
        testInjector.injectMembers(infExpDAO);
        infCompDAO = new ComputationalDAO();
        testInjector.injectMembers(infCompDAO);
    }

    @BeforeClass
    public static void setupAll() {
        DAOTestBase.setupAll();
    }

    @AfterClass
    public static void teardownAll() {
        //DAOTestBase.teardownAll();
    }

    @Test
    public void addComputationalSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        UserInstanceDTO testInstance = infExpDAO.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class).get();
        assertTrue(testInstance.getResources() != null && testInstance.getResources().size() == 1);

        UserComputationalResourceDTO testComp = testInstance.getResources().get(0);
        assertEquals(comp1.getComputationalName(), testComp.getComputationalName());
        assertEquals(comp1.getComputationalId(), testComp.getComputationalId());
        assertEquals(comp1.getStatus(), testComp.getStatus());
        assertEquals(comp1.getUptime(), testComp.getUptime());
    }

    @Test
    public void addComputationalAlreadyExists() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        boolean insertFail = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertFalse(insertFail);
    }

    @Test
    public void fetchComputationalIdSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        UserComputationalResourceDTO comp2 = new UserComputationalResourceDTO()
                .withComputationalName("comp2")
                .withComputationalId("c2")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted2 = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp2);
        assertTrue(inserted2);

        String testId = infCompDAO.fetchComputationalId(instance1.getUser(),
                instance1.getExploratoryName(), comp2.getComputationalName());
        assertEquals(comp2.getComputationalId(), testId);
    }

    @Test
    public void updateComputationalStatusSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        UserComputationalResourceDTO comp2 = new UserComputationalResourceDTO()
                .withComputationalName("comp2")
                .withComputationalId("c2")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted2 = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp2);
        assertTrue(inserted2);

        UpdateResult testResult = infCompDAO.updateComputationalStatus(
                new ComputationalStatusDTO()
                        .withUser(instance1.getUser())
                        .withExploratoryName(instance1.getExploratoryName())
                        .withComputationalName(comp2.getComputationalName())
                        .withStatus(UserInstanceStatus.RUNNING));
        assertEquals(testResult.getModifiedCount(), 1);

        Optional<UserInstanceDTO> testInstance = infExpDAO.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance.isPresent());

        List<UserComputationalResourceDTO> list = testInstance.get().getResources();
        UserComputationalResourceDTO testComp1 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp1.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp1);

        UserComputationalResourceDTO testComp2 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp2.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp2);

        assertEquals(testComp1.getStatus(), comp1.getStatus());
        assertEquals(UserInstanceStatus.of(testComp2.getStatus()), UserInstanceStatus.RUNNING);
    }

    @Test
    public void updateComputationalStatusesForExploratorySuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        UserComputationalResourceDTO comp2 = new UserComputationalResourceDTO()
                .withComputationalName("comp2")
                .withComputationalId("c2")
                .withStatus("created")
                .withUptime(new Date(100));
        boolean inserted2 = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp2);
        assertTrue(inserted2);

        UserComputationalResourceDTO comp3 = new UserComputationalResourceDTO()
                .withComputationalName("comp3")
                .withComputationalId("c3")
                .withStatus("terminated")
                .withUptime(new Date(100));
        boolean inserted3 = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp3);
        assertTrue(inserted3);

        int testResult = infCompDAO.updateComputationalStatusesForExploratory(new ComputationalStatusDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withStatus(UserInstanceStatus.STOPPED));
        assertEquals(2, testResult);

        Optional<UserInstanceDTO> testInstance = infExpDAO.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance.isPresent());

        List<UserComputationalResourceDTO> list = testInstance.get().getResources();
        UserComputationalResourceDTO testComp1 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp1.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp1);

        UserComputationalResourceDTO testComp2 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp2.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp2);

        UserComputationalResourceDTO testComp3 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp3.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp3);

        assertEquals(UserInstanceStatus.STOPPED, UserInstanceStatus.of(testComp1.getStatus()));
        assertEquals(UserInstanceStatus.STOPPED, UserInstanceStatus.of(testComp2.getStatus()));
        assertEquals(testComp3.getStatus(), comp3.getStatus());
    }

    @Test
    public void updateComputationalFieldsSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO()
                .withUser("user1")
                .withExploratoryName("exp_name_1")
                .withExploratoryId("exp1")
                .withStatus("created")
                .withUptime(new Date(100));

        infExpDAO.insertOne(USER_INSTANCES, instance1);

        UserComputationalResourceDTO comp1 = new UserComputationalResourceDTO()
                .withComputationalName("comp1")
                .withComputationalId("c1")
                .withStatus("created")
                .withUptime(new Date(100))
                .withVersion("version1");
        boolean inserted = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp1);
        assertTrue(inserted);

        UserComputationalResourceDTO comp2 = new UserComputationalResourceDTO()
                .withComputationalName("comp2")
                .withComputationalId("c2")
                .withStatus("created")
                .withUptime(new Date(100))
                .withVersion("version2");
        boolean inserted2 = infCompDAO.addComputational(instance1.getUser(), instance1.getExploratoryName(), comp2);
        assertTrue(inserted2);

        ComputationalStatusDTO status = new ComputationalStatusDTO()
                .withUser(instance1.getUser())
                .withExploratoryName(instance1.getExploratoryName())
                .withComputationalName(comp2.getComputationalName())
                .withComputationalId("c3")
                .withStatus("running")
                .withUptime(new Date(200));
        UpdateResult result = infCompDAO.updateComputationalFields(status);
        assertEquals(1, result.getModifiedCount());

        Optional<UserInstanceDTO> testInstance = infExpDAO.findOne(USER_INSTANCES,
                exploratoryCondition(instance1.getUser(), instance1.getExploratoryName()),
                UserInstanceDTO.class);
        assertTrue(testInstance.isPresent());

        List<UserComputationalResourceDTO> list = testInstance.get().getResources();
        UserComputationalResourceDTO testComp2 = list.stream()
                .filter(r -> r.getComputationalName().equals(comp2.getComputationalName()))
                .findFirst()
                .orElse(null);
        assertNotNull(testComp2);

        assertEquals(status.getComputationalId(), testComp2.getComputationalId());
        assertEquals(status.getStatus(), testComp2.getStatus());
        assertEquals(status.getUptime(), testComp2.getUptime());
        
        testComp2 = infCompDAO.fetchComputationalFields(instance1.getUser(), instance1.getExploratoryName(), comp2.getComputationalName());
        assertNotNull(testComp2);
        assertEquals(status.getComputationalId(), testComp2.getComputationalId());
        assertEquals(comp2.getComputationalName(), testComp2.getComputationalName());
        assertEquals(comp2.getMasterShape(), testComp2.getMasterShape());
        assertEquals(comp2.getSlaveNumber(), testComp2.getSlaveShape());
        assertEquals(comp2.getSlaveShape(), testComp2.getSlaveShape());
        assertEquals(status.getStatus(), testComp2.getStatus());
        assertEquals(status.getUptime(), testComp2.getUptime());
        assertEquals(comp2.getVersion(), testComp2.getVersion());
    }
}
