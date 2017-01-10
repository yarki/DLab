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
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--node_name', type=str, default='')
parser.add_argument('--ami_id', type=str, default='')
parser.add_argument('--instance_type', type=str, default='')
parser.add_argument('--key_name', type=str, default='')
parser.add_argument('--security_group_ids', type=str, default='')
parser.add_argument('--subnet_id', type=str, default='')
parser.add_argument('--iam_profile', type=str, default='')
parser.add_argument('--infra_tag_name', type=str, default='')
parser.add_argument('--infra_tag_value', type=str, default='')
parser.add_argument('--user_data_file', type=str, default='')
parser.add_argument('--instance_class', type=str, default='')
parser.add_argument('--instance_disk_size', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    instance_tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.node_name != '':
        try:
            instance_id = get_instance_by_name(args.node_name)
            if instance_id == '':
                print "Creating instance %s of type %s in subnet %s with tag %s." % \
                      (args.node_name, args.instance_type, args.subnet_id, json.dumps(instance_tag))
                instance_id = create_instance(args, instance_tag)
            else:
                print "REQUESTED INSTANCE ALREADY EXISTS AND RUNNING"
            print "Instance_id " + instance_id
            print "Public_hostname " + get_instance_attr(instance_id, 'public_dns_name')
            print "Private_hostname " + get_instance_attr(instance_id, 'private_dns_name')
            success = True
        except:
            success = False
    else:
        parser.print_help()
        sys.exit(2)

    if success:
        sys.exit(0)
    else:
        sys.exit(1)
