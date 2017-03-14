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

import static com.epam.dlab.UserInstanceStatus.TERMINATED;
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.COMPUTATIONAL_RESOURCES;
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.UPTIME;
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.exploratoryCondition;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Projections.elemMatch;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.push;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.epam.dlab.backendapi.core.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.core.UserInstanceDTO;
import com.epam.dlab.dto.StatusEnvBaseDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.mongodb.client.result.UpdateResult;

/** DAO for user computational resources.
 */
public class ComputationalDAO extends BaseDAO {
    private static final String COMPUTATIONAL_NAME = "computational_name";
    private static final String COMPUTATIONAL_ID = "computational_id";

    private static String computationalFieldFilter(String fieldName) {
        return COMPUTATIONAL_RESOURCES + FIELD_SET_DELIMETER + fieldName;
    }

    /** Add the user's computational resource for notebook into database.
     * @param user user name.
     * @param exploratoryName name of exploratory.
     * @param computationalDTO object of computational resource.
     * @return <b>true</b> if operation was successful, otherwise <b>false</b>.
     * @exception DlabException
     */
    public boolean addComputational(String user, String exploratoryName, UserComputationalResourceDTO computationalDTO) throws DlabException {
        Optional<Document> optional = findOne(USER_INSTANCES,
        		and(exploratoryCondition(user, exploratoryName),
                        elemMatch(COMPUTATIONAL_RESOURCES,
                        		eq(COMPUTATIONAL_NAME, computationalDTO.getComputationalName())))
                );
        if (!optional.isPresent()) {
            updateOne(USER_INSTANCES,
            		exploratoryCondition(user, exploratoryName),
                    push(COMPUTATIONAL_RESOURCES, convertToBson(computationalDTO)));
            return true;
        } else {
            return false;
        }
    }

    /** Finds and returns the unique id for computational resource.
     * @param user user name.
     * @param exploratoryName the name of exploratory.
     * @param computationalName name of computational resource.
     * @exception DlabException
     */
    public String fetchComputationalId(String user, String exploratoryName, String computationalName) throws DlabException {
        Document doc = findOne(USER_INSTANCES,
        		exploratoryCondition(user, exploratoryName),
                elemMatch(COMPUTATIONAL_RESOURCES, eq(COMPUTATIONAL_NAME, computationalName)))
                .orElse(new Document());
        return getDottedOrDefault(doc,
                computationalFieldFilter(COMPUTATIONAL_ID), EMPTY).toString();
    }

    /** Finds and returns the of computational resource.
     * @param user user name.
     * @param exploratoryName the name of exploratory.
     * @param computationalName name of computational resource.
     * @exception DlabException
     */
    public UserComputationalResourceDTO fetchComputationalFields(String user, String exploratoryName, String computationalName) throws DlabException {
    	Optional<UserInstanceDTO> opt = findOne(USER_INSTANCES,
                and(exploratoryCondition(user, exploratoryName),
                		elemMatch(COMPUTATIONAL_RESOURCES, eq(COMPUTATIONAL_NAME, computationalName))),
                fields(include(COMPUTATIONAL_RESOURCES), excludeId()),
                UserInstanceDTO.class);
        if( opt.isPresent() ) {
        	List<UserComputationalResourceDTO> list = opt.get().getResources();
        	UserComputationalResourceDTO comp = list.stream()
        			.filter(r -> r.getComputationalName().equals(computationalName))
        			.findFirst()
        			.orElse(null);
        	if (comp != null) {
        		return comp;
        	}
        }
        throw new DlabException("Computational resource " + computationalName + " for user " + user + " with exploratory name " +
        		exploratoryName + " not found.");
    }

    /** Updates the status of computational resource in Mongo database.
     * @param dto object of computational resource status.
     * @return The result of an update operation.
     * @exception DlabException
     */
    public UpdateResult updateComputationalStatus(ComputationalStatusDTO dto) throws DlabException {
        try {
            Document values = new Document(computationalFieldFilter(STATUS), dto.getStatus());
        	return updateOne(USER_INSTANCES,
                    and(exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                            elemMatch(COMPUTATIONAL_RESOURCES,
                            		and(eq(COMPUTATIONAL_NAME, dto.getComputationalName()),
                            				not(eq(STATUS, TERMINATED.toString()))))),
                    new Document(SET, values));
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }

    /** Updates the status of exploratory notebooks in Mongo database.
     * @param dto object of exploratory status info.
     * @return The result of an update operation.
     * @exception DlabException
     */
    public int updateComputationalStatusesForExploratory(StatusEnvBaseDTO<?> dto) throws DlabException {
        Document values = new Document(computationalFieldFilter(STATUS), dto.getStatus());
        values.append(computationalFieldFilter(UPTIME), null);
        int count = 0;
        UpdateResult result = null;
        do {
            result = updateOne(USER_INSTANCES,
                    and(exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                            elemMatch(COMPUTATIONAL_RESOURCES,
                                    and(not(eq(STATUS, TERMINATED.toString())),
                                            not(eq(STATUS, dto.getStatus()))))),
                    new Document(SET, values));
            count += result.getModifiedCount();
        }
        while (result.getModifiedCount() > 0);
        
        return count;
    }

    /** Updates the info of computational resource in Mongo database.
     * @param dto object of computational resource status.
     * @return The result of an update operation.
     * @exception DlabException
     */
    public UpdateResult updateComputationalFields(ComputationalStatusDTO dto) throws DlabException {
        try {
            Document values = new Document(computationalFieldFilter(STATUS), dto.getStatus());
        	if (dto.getUptime() != null) {
        		values.append(computationalFieldFilter(UPTIME), dto.getUptime());
        	}
        	if (dto.getInstanceId() != null) {
        		values.append(computationalFieldFilter(INSTANCE_ID), dto.getInstanceId());
        	}
        	if (dto.getComputationalId() != null) {
                values.append(computationalFieldFilter(COMPUTATIONAL_ID), dto.getComputationalId());
            }
            return updateOne(USER_INSTANCES, and(exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                    elemMatch(COMPUTATIONAL_RESOURCES,
                            and(eq(COMPUTATIONAL_NAME, dto.getComputationalName()),
                                    not(eq(STATUS, TERMINATED.toString()))))),
                    new Document(SET, values));
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }
}