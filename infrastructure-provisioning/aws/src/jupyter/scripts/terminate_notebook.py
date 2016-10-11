#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
import boto3
import os
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--emr_name', type=str)
parser.add_argument('--bucket_name', type=str)
parser.add_argument('--tag_name', type=str)
parser.add_argument('--tag_value', type=str)
args = parser.parse_args()


# Function for terminating EMR cluster
def terminate_emr(emr_name):
    client = boto3.client('emr')
    try:
        clusters = client.list_clusters(ClusterStates=['STARTING', 'BOOTSTRAPPING', 'RUNNING', 'WAITING'])
        clusters = clusters.get("Clusters")
        for c in clusters:
            if c.get('Name') == "{}".format(emr_name):
                cluster_id = c.get('Id')
                cluster_name = c.get('Name')
                client.terminate_job_flows(JobFlowIds=[cluster_id])
                print "The EMR cluster " + cluster_name + " has been deleted successfully"
    except:
        sys.exit(1)


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


# Function for terminating any EC2 instances inc notebook servers
def remove_nb(tag_name, tag_value):
    print "========== EC2 =========="
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    notebook_instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                 {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(tag_value)]}])
    for instance in notebook_instances:
        print("ID: ", instance.id)
        client.terminate_instances(InstanceIds=[instance.id])
        waiter = client.get_waiter('instance_terminated')
        waiter.wait(InstanceIds=[instance.id])
        print "The notebook instance " + instance.id + " has been deleted successfully"


##############
# Run script #
##############

if __name__ == "__main__":
    print 'Terminating EMR cluster'
    terminate_emr(args.emr_name)

    print 'Cleaning EMR config from S3 bucket'
    clean_s3(args.bucket_name, args.emr_name)

    print "Removing notebook"
    remove_nb(args.tag_name, args.tag_value)

