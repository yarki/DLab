#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
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
#
# ******************************************************************************

import json
import logging
import os
import sys
from fabric.api import *


def run():
    local_log_filename = "{}_{}.log".format(os.environ['resource'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    try:
        local("~/scripts/{}.py".format('prepare_ssn'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed preparing SSN node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)

    try:
        local("~/scripts/{}.py".format('configure_ssn'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed configuring SSN node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)


def terminate():
    local_log_filename = "{}_{}.log".format(os.environ['resource'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('terminate_ssn'))
    except:
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed terminating SSN node"}
            print json.dumps(res)
            result.write(json.dumps(res))
        sys.exit(1)