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

/** Name of fields in the Mongo collection {@link MongoCollections#SETTINGS}. */
public enum MongoSetting {
	/** Base name of service. */
    SERIVICE_BASE_NAME("service_base_name"),
	/** Name of AWS region. */
    CREDS_REGION("creds_region"),
	/** Id of security group. */
    SECURITY_GROUPS("security_groups_ids"),
	/** OS user name. */
    EXPLORATORY_SSH_USER("notebook_ssh_user"),
	/** Name of directory for user key. */
    CREDS_KEY_DIRECTORY("creds_key_dir"),
	/** Id of virtual private cloud for AWS account. */
    CREDS_VPC_ID("creds_vpc_id"),
	/** Id of virtual private cloud subnet for AWS account. */
    CREDS_SUBNET_ID("creds_subnet_id");

    private String id;

    MongoSetting(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
