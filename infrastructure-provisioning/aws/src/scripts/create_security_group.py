#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============================================================================
import boto3
import json
import argparse
import sys
import time

parser = argparse.ArgumentParser()
parser.add_argument('--name', type=str, default='')
parser.add_argument('--subnet', type=str, default='')
parser.add_argument('--security_group_rules', type=str, default='[]')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


def get_security_group_by_name(security_group_name):
    ec2 = boto3.resource('ec2')
    try:
        for security_group in ec2.security_groups.filter(GroupNames=[security_group_name]):
            return security_group.id
    except:
        return ''
    return ''


def create_security_group(security_group_name, subnet_cidr, security_group_rules, tag):
    ec2 = boto3.resource('ec2')
    for subnet in ec2.subnets.filter(Filters=[{'Name': 'cidrBlock', 'Values': [subnet_cidr]}]):
        vpc_id = subnet.vpc_id
    group = ec2.create_security_group(GroupName=security_group_name, Description='security_group_name', VpcId=vpc_id)
    # No proper waiters available. SG may fail
    time.sleep(10)
    group.create_tags(Tags=[tag])
    for rule in security_group_rules:
        group.authorize_ingress(IpPermissions=[rule])
    return group.id


##############
# Run script #
##############

if __name__ == "__main__":
    rules = json.loads(args.security_group_rules)
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.name != '':
        security_group_id = get_security_group_by_name(args.name)
        if security_group_id == '':
            print "Creating security group %s for subnet %s with tag %s." % (args.name, args.subnet, json.dumps(tag))
            security_group_id = create_security_group(args.name, args.subnet, rules, tag)
        else:
            print "REQUESTED SECURITY GROUP WITH NAME %s ALREADY EXISTS" % args.name
        print "SECURITY_GROUP_ID " + security_group_id
    else:
        parser.print_help()
