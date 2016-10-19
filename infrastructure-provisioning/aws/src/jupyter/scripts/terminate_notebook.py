#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
from dlab.aws_meta import *
from dlab.aws_actions import *
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str)
parser.add_argument('--tag_name', type=str)
parser.add_argument('--nb_tag_value', type=str)
args = parser.parse_args()


##############
# Run script #
##############

if __name__ == "__main__":
    print 'Terminating EMR cluster and cleaning EMR config from S3 bucket'
    try:
        clusters_list = get_emr_list(args.nb_tag_value, 'Value')
        for cluster_id in clusters_list:
            client = boto3.client('emr')
            cluster = client.describe_cluster(ClusterId=cluster_id)
            cluster = cluster.get("Cluster")
            emr_name = cluster.get('Name')
            s3_cleanup(args.bucket_name, emr_name)
            print "The bucket " + args.bucket_name + " has been cleaned successfully"
            terminate_emr(cluster_id)
            print "The EMR cluster " + emr_name + " has been terminated successfully"
    except:
        sys.exit(1)

    print "Terminating notebook"
    try:
        remove_ec2(args.tag_name, args.nb_tag_value)
    except:
        sys.exit(1)
    print "The notebook instance " + args.nb_tag_value + " has been terminated successfully"
