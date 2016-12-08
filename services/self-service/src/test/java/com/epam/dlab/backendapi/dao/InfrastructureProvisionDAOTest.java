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

import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.constants.UserInstanceStatus;
import com.epam.dlab.exceptions.DlabException;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Optional;

import static com.epam.dlab.backendapi.dao.BaseDAO.USER;
import static com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO.EXPLORATORY_NAME;
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
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setExploratoryId("exp1");

        UserInstanceDTO instance2 = new UserInstanceDTO();
        instance2.setUser("user1");
        instance2.setExploratoryName("exp_name_2");
        instance2.setExploratoryId("exp2");

        UserInstanceDTO instance3 = new UserInstanceDTO();
        instance3.setUser("user2");
        instance3.setExploratoryName("exp_name_1");
        instance3.setExploratoryId("exp21");

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
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setExploratoryId("exp1");

        UserInstanceDTO instance2 = new UserInstanceDTO();
        instance2.setUser("user1");
        instance2.setExploratoryName("exp_name_1");
        instance2.setExploratoryId("exp2");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        dao.fetchExploratoryId("user1", "exp_name_1");
    }

    @Test
    public void fetchExploratoryStatusSuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO();
        instance2.setUser("user1");
        instance2.setExploratoryName("exp_name_2");
        instance2.setStatus("failed");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        UserInstanceStatus status = dao.fetchExploratoryStatus("user1", "exp_name_2");
        assertEquals(status, UserInstanceStatus.FAILED);
    }

    @Test
    public void fetchExploratoryStatusBadValue() {
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setStatus("creat");

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
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setStatus("created");

        UserInstanceDTO instance2 = new UserInstanceDTO();
        instance2.setUser("user1");
        instance2.setExploratoryName("exp_name_1");
        instance2.setStatus("created");

        dao.insertOne(USER_INSTANCES, instance1);
        dao.insertOne(USER_INSTANCES, instance2);

        dao.fetchExploratoryStatus("user1", "exp_name_1");
    }

    @Test
    public void insertExploratorySuccess() {
        UserInstanceDTO instance1 = new UserInstanceDTO();
        instance1.setUser("user1");
        instance1.setExploratoryName("exp_name_1");
        instance1.setExploratoryId("exp1");
        instance1.setStatus("created");

        Boolean isInserted = dao.insertExploratory(instance1);

        Optional<UserInstanceDTO> testInstance = dao.findOne(USER_INSTANCES,
                and(eq(USER, instance1.getUser()), eq(EXPLORATORY_NAME, instance1.getExploratoryName())),
                UserInstanceDTO.class);
        assertTrue(isInserted);
        assertTrue(testInstance.isPresent());
        assertEquals(instance1.getExploratoryId(), testInstance.get().getExploratoryId());
    }


}
