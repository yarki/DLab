#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============================================================================
from ConfigParser import SafeConfigParser
import os
import base64
import json
from fabric.api import local


def create_shadow_config():
    sections = ['creds', 'conf', 'ssn', 'notebook']
    shadow_overwrite_config = SafeConfigParser()
    shadow_config_file = os.environ['PROVISION_CONFIG_DIR'] + 'shadow_overwrite.ini'
    for key in os.environ:
        transitional_key = key.lower()
        for section in sections:
            if transitional_key.startswith(section):
                if not shadow_overwrite_config.has_section(section):
                    shadow_overwrite_config.add_section(section)
                shadow_overwrite_config.set(section, transitional_key[len(section) + 1:], os.environ[key])

                print transitional_key[len(section) + 1:] + ' : ' + os.environ[key]
    with open(shadow_config_file, 'w') as shadow_config:
        shadow_overwrite_config.write(shadow_config)


if __name__ == "__main__":
    create_shadow_config()
    log = local("cd /root; fab run")

    reply = dict()
    reply['request_id'] = os.environ['request_id']
    reply['status'] = 'ok'
    if len(log.stderr) > 0:
        reply['status'] = 'fail'
    reply['response'] = dict()
    reply['response']['log_out'] = base64.b64encode(log)
    reply['response']['log_err'] = base64.b64encode(log.stderr)
    with open("/tmp/%s.json" % os.environ['request_id'], 'w') as responce_file:
        responce_file(json.dumps(reply))

