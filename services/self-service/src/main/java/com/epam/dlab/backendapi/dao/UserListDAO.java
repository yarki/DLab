/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCallbackDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoWriteException;
import org.bson.Document;

import java.io.IOException;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;

public class UserListDAO extends BaseDAO {
    public static final String ENVIRONMENT_NAME = "environment_name";
    public static final String COMPUTATIONAL_RESOURCES = "computational_resources";

    public Iterable<Document> find(String user) {
        return mongoService.getCollection(USER_INSTANCES).find(eq(USER, user));
    }

    public Iterable<Document> findShapes() {
        return mongoService.getCollection(SHAPES).find();
    }

    public boolean insertExploratory(UserInstanceDTO dto) throws JsonProcessingException {
        try {
            insertOne(USER_INSTANCES, dto);
            return true;
        } catch (MongoWriteException e) {
            return false;
        }
    }

    public void updateExploratoryStatus(ExploratoryCallbackDTO dto) {
        update(USER_INSTANCES, and(eq(USER, dto.getUser()), eq(ENVIRONMENT_NAME, dto.getName())), set(STATUS, dto.getStatus()));
    }

    public boolean addComputational(String user, String name, UserComputationalResourceDTO resourceDTO) throws IOException {
        UserInstanceDTO dto = find(USER_INSTANCES, and(eq(USER, user), eq(ENVIRONMENT_NAME, name)), UserInstanceDTO.class);
        long count = dto.getResources().stream()
                .filter(i -> resourceDTO.getResourceName().equals(i.getResourceName()))
                .count();
        if (count == 0) {
            update(USER_INSTANCES, eq(ID, dto.getId()), push(COMPUTATIONAL_RESOURCES, convertToBson(resourceDTO)));
            return true;
        } else {
            return false;
        }
    }
}
