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
from dlab.fab import *
from dlab.meta_lib import *
import sys, time, os
from dlab.actions_lib import *


if __name__ == "__main__":
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/edge/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    create_aws_config_files()
    print 'Collecting names and tags'
    edge_conf = dict()
    # Base config
    edge_conf['service_base_name'] = os.environ['conf_service_base_name']
    edge_conf['user_name'] = os.environ['edge_user_name']

    try:
        logging.info('[COLLECT DATA]')
        print '[COLLECTING DATA]'
        params = "--service_base_name '{}' --user_name '{}'".format(edge_conf['service_base_name'], edge_conf['user_name'])
        try:
            local("~/scripts/{}.py {}".format('common_collect_data', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Failed to collect necessary information. Exception: " + str(err))
        sys.exit(1)