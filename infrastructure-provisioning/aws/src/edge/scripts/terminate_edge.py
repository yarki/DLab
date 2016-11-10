#!/usr/bin/python

# ******************************************************************************************************
#
# Copyright (c) 2016 EPAM Systems Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including # without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject # to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. # IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH # # THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
# ****************************************************************************************************/

from dlab.aws_meta import *
from dlab.aws_actions import *
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--user_name', type=str)
parser.add_argument('--tag_name', type=str)
parser.add_argument('--tag_value', type=str)
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
        deregister_image(args.user_name)
    except:
        sys.exit(1)

    print "Terminating EDGE and notebook instances"
    try:
        remove_ec2(args.tag_name, args.tag_value)
    except:
        sys.exit(1)

    print "Removing s3 bucket"
    try:
        remove_s3('edge', args.user_name)
    except:
        sys.exit(1)

    print "Removing IAM roles and profiles"
    try:
        remove_role('edge', args.user_name)
        remove_role('notebook', args.user_name)
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
        remove_subnets(args.tag_value)
    except:
        sys.exit(1)
