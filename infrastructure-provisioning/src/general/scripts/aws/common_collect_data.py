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

import argparse
import json
import datetime
from fabric.api import *
from dlab.actions_lib import *
from dlab.meta_lib import *
from dlab.fab import *
import traceback
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--list_resources', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    data = json.loads(args.list_resources)
    statuses = []
    list_instances_ids = []
    list_cluster_ids = []
    try:
        for i in data:
            if i.get('resource_type') == 'host':
                list_instances_ids.append(i.get('id'))
            elif i.get('resource_type') == 'cluster':
                list_cluster_ids.append(i.get('id'))
        data_ec2 = get_list_instance_statuses(list_instances_ids)
        statuses = get_list_cluster_statuses(list_cluster_ids, data_ec2)
        with open('/root/result.json', 'w') as outfile:
            json.dump(statuses, outfile)
    except Exception as err:
        traceback.print_exc()
        append_result("Failed getting resources statuses. Exception: " + str(err))
        sys.exit(1)