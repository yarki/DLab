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
import boto
import boto3
import sys
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--role_name', type=str, default='dsa-test-role')
parser.add_argument('--role_profile_name', type=str, default='dsa-test-role-profile')
parser.add_argument('--policy_name', type=str, default='dsa-test-policy')
parser.add_argument('--policy_arn', type=str, default='"arn:aws:iam::aws:policy/AmazonS3FullAccess", '
                                                      '"arn:aws:iam::aws:policy/AmazonEC2FullAccess", '
                                                      '"arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"')
parser.add_argument('--policy_file_name', type=str, default='')
args = parser.parse_args()


def get_role_by_name(role_name):
    iam = boto3.resource('iam')
    for role in iam.roles.all():
        if role.name == role_name:
            return role.name
    return ''


def create_iam_role(role_name, role_profile):
    conn = boto.connect_iam()
    conn.create_role(role_name)
    conn.create_instance_profile(role_profile)
    conn.add_role_to_instance_profile(role_profile, role_name)


def attach_policy(policy_arn, role_name):
    conn = boto.connect_iam()
    conn.attach_role_policy(policy_arn, role_name)


def create_attach_policy(policy_name, role_name, file_path):
    conn = boto.connect_iam()
    with open(file_path, 'r') as myfile:
        json = myfile.read()
    conn.put_role_policy(role_name, policy_name, json)


if __name__ == "__main__":
    if args.role_name != '':
        role_name = get_role_by_name(args.role_name)
        if role_name == '':
            print "Creating role %s, profile name %s" % (args.role_name, args.role_profile_name)
            create_iam_role(args.role_name, args.role_profile_name)
        else:
            print "ROLE AND ROLE PROFILE ARE ALREADY CREATED"
        print "ROLE %s created. IAM group %s created" % (args.role_name, args.role_profile_name)

        print "ATTACHING POLICIES TO ROLE"
        if args.policy_file_name != '':
            create_attach_policy(args.policy_name, args.role_name, args.policy_file_name)
        else:
            policy_arn_bits = eval(args.policy_arn)
            for bit in policy_arn_bits:
                attach_policy(bit, args.role_name)
        print "POLICY %s created " % args.policy_name
    else:
        parser.print_help()
