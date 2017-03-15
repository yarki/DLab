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

package com.epam.dlab.rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RESTServiceMock extends RESTService {

    private final static Logger LOG = LoggerFactory.getLogger(RESTService.class);

    @Override
    public <T> T post(String path, Object parameter, Class<T> clazz) {
        LOG.debug("REST POST {},\n object {},\n response class {}", path, parameter, clazz.getName());
        try {
			return (T) clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        return null;
    }
}
