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
import static com.epam.dlab.backendapi.dao.ExploratoryDAO.exploratoryCondition;
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
import com.mongodb.client.model.Updates;

public class EnvStatusDAO extends BaseDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvStatusDAO.class);

	private static final String EDGE_STATUS = "edge_status";

    private static final Bson INCLUDE_EDGE_FIELDS = include(INSTANCE_ID, EDGE_STATUS);
	private static final Bson INCLUDE_EXP_FIELDS = include(INSTANCE_ID, STATUS,
								COMPUTATIONAL_RESOURCES + "." + INSTANCE_ID, COMPUTATIONAL_RESOURCES + "." + STATUS);
	private static final Bson INCLUDE_EXP_UPDATE_FIELDS = include(EXPLORATORY_NAME, INSTANCE_ID, STATUS,
								COMPUTATIONAL_RESOURCES + "." + INSTANCE_ID, COMPUTATIONAL_RESOURCES + "." + STATUS);

	/** Add the resource to list if it have instance_id.
	 * @param list the list to add.
	 * @param document document with resource.
	 * @param includeStatus include status or not to the list.
	 */
	private void addResource(List<EnvResource> list, Document document, String statusFieldName) {
		LOGGER.debug("Add resource from {}", document);
		String instanceId = document.getString(INSTANCE_ID);
		if (instanceId != null) {
			UserInstanceStatus status = UserInstanceStatus.of(document.getString(statusFieldName));
			if (status == null) {
				LOGGER.error("Unknown status {} from field {}, content is {}", document.getString(statusFieldName), statusFieldName, document);
				return;
			}
			switch (status) {
			case CONFIGURING:
			case CREATING:
			case RUNNING:
			case STARTING:
			case STOPPED:
			case STOPPING:
			case TERMINATING:
				EnvResource host = new EnvResource().withId(instanceId);
				list.add(host);
				break;
			case FAILED:
			case TERMINATED:
			default:
				break;
			}
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
    		addResource(hostList, edge, EDGE_STATUS);
    	}

    	// Add exploratory
    	Iterable<Document> expList = find(USER_INSTANCES,
    			eq(USER, user),
    			fields(INCLUDE_EXP_FIELDS, excludeId()));

    	for (Document exp : expList) {
    		addResource(hostList, exp, STATUS);
    		
    		// Add computational
			@SuppressWarnings("unchecked")
			List<Document> compList = (List<Document>) exp.get(COMPUTATIONAL_RESOURCES);
			if (compList == null) {
				continue;
			}
			for (Document comp : compList) {
				addResource(clusterList, comp, STATUS);
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
    
    
    private UserInstanceStatus getExploratoryNewStatus(UserInstanceStatus oldStatus, String newStatus) {
    	/*
    	pending
    	running
    	shutting-down
    	terminated
    	stopping
    	stopped
    	*/
    	
    	UserInstanceStatus status;
    	if ("pending".equalsIgnoreCase(newStatus) || "stopping".equalsIgnoreCase(newStatus)) {
    		return oldStatus;
    	} else if ("shutting-down".equalsIgnoreCase(newStatus)) {
    		status = UserInstanceStatus.TERMINATING;
    	} else {
    		status = UserInstanceStatus.of(newStatus);
    	}
    	
    	switch (oldStatus) {
			case CONFIGURING:
			case CREATED:
			case CREATING:
				return (status.in(UserInstanceStatus.TERMINATED, UserInstanceStatus.STOPPED) ? status : oldStatus);
			case RUNNING:
			case STARTING:
			case STOPPING:
				return (status.in(UserInstanceStatus.TERMINATING, UserInstanceStatus.TERMINATED,
	                              UserInstanceStatus.STOPPING, UserInstanceStatus.STOPPED) ? status : oldStatus);
			case STOPPED:
				return (status.in(UserInstanceStatus.TERMINATING, UserInstanceStatus.TERMINATED,
	                    UserInstanceStatus.RUNNING) ? status : oldStatus);
			case TERMINATING:
				return (status.in(UserInstanceStatus.TERMINATED) ? status : oldStatus);
			case FAILED:
			case TERMINATED:
			default:
				return oldStatus;
    	}
    }
    
    /** Update the status of exploratory if it needed.
     * @param user the user name
     * @param instanceId the id of instance
     * @param oldStatus old status
     * @param newStatus new status
     */
    private void updateExploratoryStatus(String user, String exploratoryName,
    		String oldStatus, String newStatus) {
    	LOGGER.debug("Update exploratory status for user with exploratory {} from {} to {}", user, exploratoryName, oldStatus, newStatus);
    	UserInstanceStatus oStatus = UserInstanceStatus.of(oldStatus);
    	UserInstanceStatus status = getExploratoryNewStatus(oStatus, newStatus);
    	LOGGER.debug("Translate exploratory status for user with exploratory {} from {} to {}", user, exploratoryName, newStatus, status);

    	if (oStatus != status) {
        	LOGGER.debug("Exploratory status will be updated from {} to {}", oldStatus, status);
        	updateOne(USER_INSTANCES,
        			exploratoryCondition(user, exploratoryName),
        			Updates.set(STATUS, status.name()));
    	}
    }

    /** Update the status of exploratory if it needed.
     * @param user the user name
     * @param instanceId the id of instance
     * @param oldStatus old status
     * @param newStatus new status
     */
	private void updateComputationalStatus(String user, String exploratoryId, String clusterId,
			String oldStatus, String newStatus) {
    	LOGGER.debug("Update computational status for user with instance_id {} from {} to {}", user, clusterId, oldStatus, newStatus);
    	UserInstanceStatus oStatus = UserInstanceStatus.of(oldStatus);
    	
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
    				updateExploratoryStatus(user, exploratoryName, exp.getString(STATUS), r.getStatus());
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
	    						comp.getString(STATUS), r.getStatus());
	    			}
	    		}
			}
		}
    }
}