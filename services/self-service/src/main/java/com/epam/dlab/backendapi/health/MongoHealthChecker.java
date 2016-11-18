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

package com.epam.dlab.backendapi.health;

import com.epam.dlab.client.mongo.MongoService;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoHealthChecker implements HealthChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoHealthChecker.class);

    @Inject
    private MongoService mongoService;

    @Override
    public boolean isAlive() {
        try {
            mongoService.ping();
            return true;
        } catch (MongoException e) {
            LOGGER.error("Mongo is not available");
            return false;
        }
    }
}
