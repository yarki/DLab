#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
from dlab.aws_meta import *
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str)
parser.add_argument('--tag_name', type=str)
parser.add_argument('--nb_tag_value', type=str)
args = parser.parse_args()


# Function for cleaning EMR config from S3 bucket
def clean_s3(bucket_name, emr_name):
    s3 = boto3.resource('s3')
    try:
        s3_bucket = s3.Bucket(bucket_name)
        s3_dir = "config/" + emr_name + "/"
        for i in s3_bucket.objects.filter(Prefix=s3_dir):
            s3.Object(s3_bucket.name, i.key).delete()
        print "The bucket " + bucket_name + " has been cleaned successfully"
    except:
        sys.exit(1)


# Function for terminating EMR cluster
def terminate_emr(cluster_id, bucket_name):
    client = boto3.client('emr')
    try:
        cluster = client.describe_cluster(ClusterId=cluster_id)
        cluster = cluster.get("Cluster")
        emr_name = cluster.get('Name')
        client.terminate_job_flows(JobFlowIds=[cluster_id])
        print "The EMR cluster " + emr_name + " has been deleted successfully"
    except:
        sys.exit(1)
    clean_s3(bucket_name, emr_name)


# Function for terminating any EC2 instances inc user servers
def remove_nb(tag_name, nb_tag_value):
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    try:
        notebook_instances = ec2.instances.filter(
            Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                     {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(nb_tag_value)]}])
        for instance in notebook_instances:
            print("ID: ", instance.id)
            client.terminate_instances(InstanceIds=[instance.id])
            waiter = client.get_waiter('instance_terminated')
            waiter.wait(InstanceIds=[instance.id])
            print "The user instance " + instance.id + " has been deleted successfully"
    except:
        sys.exit(1)

##############
# Run script #
##############

if __name__ == "__main__":
    try:
        clusters_list = get_emr_list(args.nb_tag_value, 'Value')
    except:
        sys.exit(1)
    for cluster_id in clusters_list:
        print 'Terminating EMR cluster and cleaning EMR config from S3 bucket'
        terminate_emr(cluster_id, args.bucket_name)

    print "Removing user"
    remove_nb(args.tag_name, args.nb_tag_value)

