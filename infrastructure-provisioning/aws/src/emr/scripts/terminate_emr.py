#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
import boto3
from fabric.api import *
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--emr_name', type=str)
parser.add_argument('--bucket_name', type=str)
parser.add_argument('--tag_name', type=str)
parser.add_argument('--nb_tag_value', type=str)
parser.add_argument('--ssh_user', type=str)
parser.add_argument('--key_path', type=str)
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


# Function for removing notebook's local kernels
def remove_kernels(emr_name, tag_name, nb_tag_value, ssh_user, key_path):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running']},
                 {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(nb_tag_value)]}])
    for instance in instances:
        try:
            private = getattr(instance, 'private_dns_name')
            env.hosts = "{}".format(private)
            env.user = "{}".format(ssh_user)
            env.key_filename = "{}".format(key_path)
            env.host_string = env.user + "@" + env.hosts
            sudo('rm -rf /srv/hadoopconf/{}'.format(emr_name))
            sudo('rm -rf /home/{}/.local/share/jupyter/kernels/*_{}'.format(ssh_user, emr_name))
            print "Notebook's " + env.hosts + " kernels were removed"
        except:
            sys.exit(1)


##############
# Run script #
##############

if __name__ == "__main__":
    print 'Terminating EMR cluster'
    terminate_emr(args.emr_name)

    print 'Cleaning EMR config from S3 bucket'
    clean_s3(args.bucket_name, args.emr_name)

    print "Removing notebook's EMR kernels"
    remove_kernels(args.emr_name, args.tag_name, args.nb_tag_value, args.ssh_user, args.key_path)

