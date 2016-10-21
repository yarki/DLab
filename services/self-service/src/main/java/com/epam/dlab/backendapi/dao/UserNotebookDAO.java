/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.dao;

import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import static com.epam.dlab.backendapi.dao.MongoCollections.SHAPES;
import static com.epam.dlab.backendapi.dao.MongoCollections.USER_NOTEBOOKS;
import static com.mongodb.client.model.Filters.eq;

public class UserNotebookDAO extends BaseDAO {
    public Iterable<Document> find(String user) {
        return mongoService.getCollection(USER_NOTEBOOKS).find(eq(USER, user));
    }

    public Iterable<Document> findShapes() {
        return mongoService.getCollection(SHAPES).find();
    }

    public void insert(String user, String image) {
        insertOne(USER_NOTEBOOKS, () -> new Document(USER, user).append("image", image));
    }

    public DeleteResult delete(String id) {
        return mongoService.getCollection(USER_NOTEBOOKS).deleteOne(eq(USER, id));
    }
}
