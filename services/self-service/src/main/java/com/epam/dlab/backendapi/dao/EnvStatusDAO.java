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
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.epam.dlab.dto.status.EnvResource;
import com.epam.dlab.dto.status.EnvResourceList;

public class EnvStatusDAO extends BaseDAO {
	private static final Bson INCLUDE_EDGE_FIELDS = include(INSTANCE_ID, STATUS);
	private static final Bson INCLUDE_EXP_FIELDS = include(INSTANCE_ID, STATUS, COMPUTATIONAL_RESOURCES + "." + INSTANCE_ID, COMPUTATIONAL_RESOURCES + "." + STATUS);

	/** Add the resource to list if it have instance_id.
	 * @param list the list to add.
	 * @param document document with resource.
	 * @param includeStatus include status or not to the list.
	 */
	private void addResource(List<EnvResource> list, Document document, boolean includeStatus) {
		String instanceId = document.getString(INSTANCE_ID);
		if (instanceId == null) {
			return;
		}
		
		EnvResource host = new EnvResource().withId(instanceId);
		if (includeStatus) {
    		host.withStatus(document.getString(STATUS));
		}
		
		list.add(host);
	}
	
    /** Finds and returns the list of user resources. 
     * @param user name.
     */
    public EnvResourceList findEnvResources(String user, boolean includeStatus) {
    	List<EnvResource> hostList = new ArrayList<EnvResource>();
    	List<EnvResource> clusterList = new ArrayList<EnvResource>();
    	
    	// Add EDGE
    	Document edge = findOne(USER_AWS_CREDENTIALS,
    			eq(ID, user),
    			fields(INCLUDE_EDGE_FIELDS, excludeId())).orElse(null);
    	if (edge != null) {
    		addResource(hostList, edge, includeStatus);
    	}

    	// Add exploratory
    	Iterable<Document> expList = find(USER_INSTANCES,
    			eq(USER, user),
    			fields(INCLUDE_EXP_FIELDS, excludeId()));

    	for (Document exp : expList) {
    		addResource(hostList, exp, includeStatus);
    		
    		// Add computational
			@SuppressWarnings("unchecked")
			List<Document> compList = (List<Document>) exp.get(COMPUTATIONAL_RESOURCES);
			if (compList == null) {
				continue;
			}
			for (Document comp : compList) {
				addResource(clusterList, comp, includeStatus);
			}
		}
    	
    	return (hostList.size() == 0 ? null :
    		new EnvResourceList()
    			.withHostList(hostList)
    			.withClusterList(clusterList.size() > 0 ? clusterList : null));
    }

}