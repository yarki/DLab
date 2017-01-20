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

package com.epam.dlab.backendapi.dao.databind;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Date;

/** Registers the deserializer for date type {@link IsoDateDeSerializer}.
 */
public class IsoDateModule extends SimpleModule {
	
	private static final long serialVersionUID = -2103066255354028256L;

	/** Registers the deserializer for date type {@link IsoDateDeSerializer}.
	 */
    public IsoDateModule() {
        super();
        addDeserializer(Date.class, new IsoDateDeSerializer());
    }
}
