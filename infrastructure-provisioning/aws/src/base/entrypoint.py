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
import argparse
from fabric.api import local
import json

parser = argparse.ArgumentParser()
parser.add_argument('--action', type=str, default='describe')
args = parser.parse_args()

if __name__ == "__main__":
    request_id = 'generic'
    try:
        request_id = os.environ['request_id']
    except:
        pass

    if args.action == 'create':
        out = local("/bin/create.py", capture=True)
        response = json.loads(out)
        response['request_id'] = request_id
        with open('/tmp/%s.json', 'w') as response_file:
            response_file.write(json.dumps(response))

    elif args.action == 'status':
        out = local("/bin/status.py", capture=True)
        response = json.loads(out)
        response['request_id'] = request_id
        with open('/tmp/%s.json', 'w') as response_file:
            response_file.write(json.dumps(response))

    elif args.action == 'describe':
        with open('/root/description.json') as json_file:
            description = json.load(json_file)
            description['request_id'] = request_id
            with open('/tmp/%s.json', 'w') as response_file:
                response_file.write(json.dumps(description))

    elif args.action == 'stop':
        out = local("/bin/stop.py", capture=True)
        response = json.loads(out)
        response['request_id'] = request_id
        with open('/tmp/%s.json', 'w') as response_file:
            response_file.write(json.dumps(response))

    elif args.action == 'start':
        out = local("/bin/start.py", capture=True)
        response = json.loads(out)
        response['request_id'] = request_id
        with open('/tmp/%s.json', 'w') as response_file:
            response_file.write(json.dumps(response))
