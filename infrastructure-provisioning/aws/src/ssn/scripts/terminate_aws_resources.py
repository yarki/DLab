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

from dlab.aws_meta import *
from dlab.aws_actions import *
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--tag_name', type=str)
parser.add_argument('--nb_sg', type=str)
parser.add_argument('--edge_sg', type=str)
args = parser.parse_args()


##############
# Run script #
##############

if __name__ == "__main__":
    print 'Terminating EMR cluster'
    try:
        clusters_list = get_emr_list(args.tag_name)
        if clusters_list:
            for cluster_id in clusters_list:
                client = boto3.client('emr')
                cluster = client.describe_cluster(ClusterId=cluster_id)
                cluster = cluster.get("Cluster")
                emr_name = cluster.get('Name')
                terminate_emr(cluster_id)
                print "The EMR cluster " + emr_name + " has been terminated successfully"
        else:
            print "There are no EMR clusters to terminate."
    except:
        sys.exit(1)

    print "Deregistering notebook's AMI"
    try:
        deregister_image('*')
    except:
        sys.exit(1)

    print "Terminating EDGE and notebook instances"
    try:
        remove_ec2(args.tag_name, '*')
    except:
        sys.exit(1)

    print "Removing s3 bucket"
    try:
        remove_s3('edge')
        remove_s3('ssn')
    except:
        sys.exit(1)

    print "Removing IAM roles and profiles"
    try:
        remove_role('notebook', '*')
        remove_role('edge', '*')
        remove_role('ssn', '*')
    except:
        sys.exit(1)

    print "Removing security groups"
    try:
        remove_sgroups(args.nb_sg)
        remove_sgroups(args.edge_sg)
    except:
        sys.exit(1)

    print "Removing private subnet"
    try:
        remove_subnets('*')
    except:
        sys.exit(1)

    print "Removing route tables"
    try:
        remove_route_tables(args.tag_name)
    except:
        sys.exit(1)
