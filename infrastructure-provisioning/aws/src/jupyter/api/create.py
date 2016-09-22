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
import os
import base64
import json
from fabric.api import local


if __name__ == "__main__":
    notebook_name = os.environ['notebook_name']
    network_cidr = os.environ['network_cidr']
    log = local("/root/fabfile.py --notebook_name %s --subnet_cidr %s " % (notebook_name, network_cidr))

    reply = dict()
    reply['request_id'] = os.environ['request_id']
    reply['status'] = 'ok'
    if len(log.stderr) > 0:
        reply['status'] = 'fail'
    reply['response'] = dict()

    try:
        with open("/root/result.json") as f:
            reply['response']['result'] = json.loads(f.read())
    except:
        pass

    try:
        with open("/root/runlog.log") as f:
            reply['response']['log'] = base64.b64encode(f.read())
    except:
        pass

    with open("/response/%s.json" % os.environ['request_id'], 'w') as response_file:
        response_file.write(json.dumps(reply))
