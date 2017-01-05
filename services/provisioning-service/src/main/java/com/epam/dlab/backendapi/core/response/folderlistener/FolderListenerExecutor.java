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

package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.dropwizard.util.Duration;

/** Starts asynchronously of waiting for file creation and processing this file.
 */
@Singleton
public class FolderListenerExecutor {
    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;

    /** Starts asynchronously of waiting for file creation and processes this file if the timeout
     * has not expired. If timeout has been expired then writes warning message to log file and
     * finishes waiting. This method is <b>non-blocking</b>.  
     * @param directory name of directory for waiting for the file creation. 
     * @param timeout timeout for waiting.
     * @param fileHandlerCallback handler for the file processing.
     */
    public void start(String directory, Duration timeout, FileHandlerCallback fileHandlerCallback) {
    	FolderListener.listen(directory, fileHandlerCallback, timeout.toMilliseconds(),
    			configuration.getFileLengthCheckDelay().toMilliseconds());
    }
}
