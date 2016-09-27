#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
import boto3
from fabric.api import *
import os
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--notebook_tag_value_name', type=str)
parser.add_argument('--resource_name', type=str)
parser.add_argument('--emr_name', type=str)
args = parser.parse_args()


# Function for terminating EMR clusters, cleaning buckets and removing notebook's local kernels
def remove_emr(emr_name, notebook_tag_value_name):
    print "========= EMR =========="
    client = boto3.client('emr')
    bucket_name = (os.environ['conf_service_base_name'] + '-bucket').lower().replace('_', '-')
    tag_name = os.environ['conf_service_base_name']
    clusters = client.list_clusters(ClusterStates=['STARTING', 'BOOTSTRAPPING', 'RUNNING', 'WAITING'])
    clusters = clusters.get("Clusters")
    for c in clusters:
        if c.get('Name') == "{}".format(emr_name):
            cluster_id = c.get('Id')
            cluster_name = c.get('Name')
            print cluster_id
            client.terminate_job_flows(JobFlowIds=[cluster_id])
            print "The EMR cluster " + cluster_name + " has been deleted successfully"

    print "======= clean S3 ======="
    client = boto3.client('s3')
    try:
        list_obj = client.list_objects(Bucket=bucket_name)
    except:
        print "Wasn't able to get S3!"
    try:
        list_obj = list_obj.get('Contents')
    except:
        print "Wasn't able to get S3!"
    if list_obj is not None:
        for o in list_obj:
            list_obj = o.get('Key')
            client.delete_objects(
                Bucket=bucket_name,
                Delete={'Objects': [{'Key': list_obj}]}
            )
        print "The bucket " + bucket_name + " has been cleaned successfully"
    else:
        print "The bucket " + bucket_name + " was empty"

    print "==== remove kernels ===="
    ec2 = boto3.resource('ec2')
    key_name = os.environ['creds_key_name']
    user_name = os.environ['notebook_ssh_user']
    key_dir = os.environ['SYSTEM_KEYFILE_DIR']
    path_to_key = key_dir + key_name + '.pem'
    instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running']},
                 {'Name': 'tag:{}'.format(tag_name), 'Values': ['{}'.format(notebook_tag_value_name)]}])
    for instance in instances:
        private = getattr(instance, 'private_dns_name')
        env.hosts = "{}".format(private)
        print env.hosts
        env.user = "{}".format(user_name)
        env.key_filename = "{}".format(path_to_key)
        env.host_string = env.user + "@" + env.hosts
        sudo('rm -rf ' + "/srv/hadoopconf/" + "{}".format(emr_name))
        sudo('rm -rf ' + "/home/" + "{}".format(user_name) + "/.local/share/jupyter/kernels/pyspark_" + "{}".format(
            emr_name))
        print "Notebook's " + env.hosts + " kernels were removed"


##############
# Run script #
##############

if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        if args.resource_name == "EMR":
            remove_emr(args.emr_name, args.notebook_tag_value_name)
        else:
            print """
            Please type correct resource name to delete
            """
