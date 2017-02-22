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

How to run locally the self service or provisioning service on development mode.


	Install Node.js
1. Install Node.js from https://nodejs.org/en/
2. Add Node.js installation folder to environment variable PATH
3. Execute command:
	npm install npm@latest -g
4. Change folder to ..\dlab\services\self-service\src\main\resources\webapp
   Execute command:
	npm i


	Install Mongo database


	Environment options
1. Set configuration file ..\..\infrastructure-provisioning\aws\src\ssn\templates\ssn.yml
# DEV_MODE="true"
2. Add system environment variable
DLAB_CONF_DIR_NAME=..\..\infrastructure-provisioning\aws\src\ssn\templates
or create two symlinks to service\provisioning-service and service\self-service for
..\..\infrastructure-provisioning\aws\src\ssn\templates\ssn.yml
Unix
  ln -s ssn.yml ../../infrastructure-provisioning/src/ssn/templates/ssn.yml
Windows
  mklink ssn.yml ..\..\infrastructure-provisioning\src\ssn\templates\ssn.yml


