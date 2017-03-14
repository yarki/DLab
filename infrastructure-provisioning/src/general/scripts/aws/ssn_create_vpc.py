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
from dlab.actions_lib import *
from dlab.meta_lib import *


parser = argparse.ArgumentParser()
parser.add_argument('--vpc', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--infra_tag_name', type=str, default='')
parser.add_argument('--infra_tag_value', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.vpc != '':
        try:
            vpc_id = get_vpc_by_tag(args.infra_tag_name, args.infra_tag_value)
            if vpc_id == '':
                print "Creating vpc %s in region %s with tag %s." % (args.vpc, args.region, json.dumps(tag))
                vpc_id = create_vpc(args.vpc, tag)
                enable_vpc_dns(vpc_id)
                rt_id = create_rt(vpc_id, args.infra_tag_name, args.infra_tag_value)
            else:
                print "REQUESTED VPC ALREADY EXISTS"
            print "VPC_ID " + vpc_id
            args.vpc_id = vpc_id
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
