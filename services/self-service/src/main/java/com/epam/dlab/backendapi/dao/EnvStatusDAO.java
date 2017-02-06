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

import static com.epam.dlab.backendapi.dao.ExploratoryDAO.COMPUTATIONAL_RESOURCES;
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.EXPLORATORY_NAME;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.dto.status.EnvResource;
import com.epam.dlab.dto.status.EnvResourceList;
import com.epam.dlab.exceptions.DlabException;

public class EnvStatusDAO extends BaseDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvStatusDAO.class);

    private static final Bson INCLUDE_EDGE_FIELDS = include(INSTANCE_ID, STATUS);
	private static final Bson INCLUDE_EXP_FIELDS = include(INSTANCE_ID, COMPUTATIONAL_RESOURCES + "." + INSTANCE_ID);
	private static final Bson INCLUDE_EXP_UPDATE_FIELDS = include(EXPLORATORY_NAME, INSTANCE_ID, STATUS,
								COMPUTATIONAL_RESOURCES + "." + INSTANCE_ID, COMPUTATIONAL_RESOURCES + "." + STATUS);

	/** Add the resource to list if it have instance_id.
	 * @param list the list to add.
	 * @param document document with resource.
	 * @param includeStatus include status or not to the list.
	 */
	private void addResource(List<EnvResource> list, Document document) {
		String instanceId = document.getString(INSTANCE_ID);
		if (instanceId != null) {
			EnvResource host = new EnvResource().withId(instanceId);
			list.add(host);
		}
	}
	
	private Document getEdgeNode(String user) {
		return findOne(USER_AWS_CREDENTIALS,
    			eq(ID, user),
    			fields(INCLUDE_EDGE_FIELDS, excludeId())).orElse(null);
	}
	
	private EnvResource getEnvResourceAndRemove(List<EnvResource> list, String id) {
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				EnvResource r = list.get(i);
				if (r.getId().equals(id)) {
					list.remove(i);
					return r;
				}
			}
		}
		return null;
	}
	
    /** Finds and returns the list of user resources. 
     * @param user name.
     */
    public EnvResourceList findEnvResources(String user) {
    	List<EnvResource> hostList = new ArrayList<EnvResource>();
    	List<EnvResource> clusterList = new ArrayList<EnvResource>();
    	
    	// Add EDGE
    	Document edge = getEdgeNode(user);
    	if (edge != null) {
    		addResource(hostList, edge);
    	}

    	// Add exploratory
    	Iterable<Document> expList = find(USER_INSTANCES,
    			eq(USER, user),
    			fields(INCLUDE_EXP_FIELDS, excludeId()));

    	for (Document exp : expList) {
    		addResource(hostList, exp);
    		
    		// Add computational
			@SuppressWarnings("unchecked")
			List<Document> compList = (List<Document>) exp.get(COMPUTATIONAL_RESOURCES);
			if (compList == null) {
				continue;
			}
			for (Document comp : compList) {
				addResource(clusterList, comp);
			}
		}
    	
    	return (hostList.size() == 0 ? null :
    		new EnvResourceList()
    			.withHostList(hostList)
    			.withClusterList(clusterList.size() > 0 ? clusterList : null));
    }

    /** Updates the status of EDGE node for user.
     * @param user the name of user.
     * @param status the status of node.
     * @exception DlabException
     */
    private void updateEdgeStatus(String user, List<EnvResource> hostList) throws DlabException {
    	LOGGER.debug("Update EDGE status for user ", user);
    	Document edge = getEdgeNode(user);
    	String instanceId;
    	if (edge == null ||
    		(instanceId = edge.getString(INSTANCE_ID)) == null) {
    		return;
    	}
		
    	EnvResource r = getEnvResourceAndRemove(hostList, instanceId);
    	if (r == null) {
    		return;
    	}
    	
    	LOGGER.debug("Update EDGE status for user with instance_id {} from {} to {}", user, instanceId, edge.getString(STATUS), r.getStatus());
    	if (!r.getStatus().equals(edge.getString(STATUS))) {
    		Document values = new Document(STATUS, r.getStatus());
    		updateOne(USER_AWS_CREDENTIALS,
        		eq(ID, user),
                new Document(SET, values));
    	}
    }
    
    /** Update the status of exploratory if it needed.
     * @param user the user name
     * @param instanceId the id of instance
     * @param oldState old state
     * @param newState new state
     */
    private void updateExploratoryStatus(String user, String instanceId, UserInstanceStatus oldState, UserInstanceStatus newState) {
    	LOGGER.debug("Update exploratory status for user with instance_id {} from {} to {}", user, instanceId, oldState, newState);
		/*updateOne(USER_INSTANCES,
        exploratoryCondition(user, exp.get),
        set(STATUS, r.getStatus()));*/
    }
    

    /** Update the status of exploratory if it needed.
     * @param user the user name
     * @param instanceId the id of instance
     * @param oldState old state
     * @param newState new state
     */
	private void updateComputationalStatus(String user, String exploratoryId, String clusterId, UserInstanceStatus oldState, UserInstanceStatus newState) {
    	LOGGER.debug("Update computational status for user with instance_id {} from {} to {}", user, clusterId, oldState, newState);
	}

	/** Updates the status of exploratory and computational for user.
     * @param user the name of user.
     * @param status the status of node.
     * @exception DlabException
     */
    public void updateEnvStatus(String user, EnvResourceList list) throws DlabException {
    	if (list.getHostList() == null || list.getHostList().size() == 0) {
    		return;
    	}
    	
    	// Update EDGE
		updateEdgeStatus(user, list.getHostList());
    	if (list.getHostList().size() == 0) {
    		return;
    	}
    	
    	// Update exploratory
    	Iterable<Document> expList = find(USER_INSTANCES,
    			eq(USER, user),
    			fields(INCLUDE_EXP_UPDATE_FIELDS, excludeId()));

    	EnvResource r;
    	for (Document exp : expList) {
    		final String exploratoryName = exp.getString(EXPLORATORY_NAME);
    		String instanceId = exp.getString(INSTANCE_ID);
    		if (instanceId != null) {
    			r = getEnvResourceAndRemove(list.getHostList(), instanceId);
    			if (r != null) {
    				updateExploratoryStatus(user, instanceId,
    						UserInstanceStatus.of(exp.getString(STATUS)), UserInstanceStatus.of(r.getStatus()));
    			}
    		}
    		
    		// Update computational
			@SuppressWarnings("unchecked")
			List<Document> compList = (List<Document>) exp.get(COMPUTATIONAL_RESOURCES);
			if (compList == null) {
				continue;
			}
			for (Document comp : compList) {
				instanceId = comp.getString(INSTANCE_ID);
	    		if (instanceId != null) {
	    			r = getEnvResourceAndRemove(list.getClusterList(), instanceId);
	    			if (r != null) {
	    				updateComputationalStatus(user, exploratoryName, instanceId,
	    						UserInstanceStatus.of(comp.getString(STATUS)), UserInstanceStatus.of(r.getStatus()));
	    			}
	    		}
			}
		}
    }
}