#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
import boto3
from fabric.api import *
import os
import argparse
from ConfigParser import SafeConfigParser

parser = argparse.ArgumentParser()
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--notebook_tag_value_name', type=str)
parser.add_argument('--resource_name', type=str)
parser.add_argument('--emr_name', type=str)
args = parser.parse_args()


# Function for parsing config files for parameters
def get_configuration(configuration_dir):
    merged_config = SafeConfigParser()

    crid_config = SafeConfigParser()
    crid_config.read(configuration_dir + 'aws_crids.ini')
    for section in ['creds', 'ops']:
        for option, value in crid_config.items(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            merged_config.set(section, option, value)

    base_infra_config = SafeConfigParser()
    base_infra_config.read(configuration_dir + 'self_service_node.ini')
    for section in ['conf', 'ssn']:
        for option, value in base_infra_config.items(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            merged_config.set(section, option, value)

    notebook_config = SafeConfigParser()
    notebook_config.read(configuration_dir + 'notebook.ini')
    section = 'notebook'
    for option, value in notebook_config.items(section):
        if not merged_config.has_section(section):
            merged_config.add_section(section)
        merged_config.set(section, option, value)

    overwrite_config = SafeConfigParser()
    overwrite_config.read(configuration_dir + 'overwrite.ini')
    for section in ['creds', 'conf', 'ssn', 'notebook']:
        if overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in overwrite_config.items(section):
                merged_config.set(section, option, value)

    shadow_overwrite_config = SafeConfigParser()
    shadow_overwrite_config.read(configuration_dir + 'shadow_overwrite.ini')
    for section in ['creds', 'conf', 'ssn', 'notebook']:
        if shadow_overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in shadow_overwrite_config.items(section):
                merged_config.set(section, option, value)

    return merged_config


# Function for determining service_base_name from config
def determine_service_base_name():
    config = get_configuration(os.environ['PROVISION_CONFIG_DIR'])
    service_base_name = config.get('conf', 'service_base_name')

    return service_base_name


# Function for terminating EMR clusters, cleaning buckets and removing notebook's local kernels
def remove_emr(emr_name, notebook_tag_value_name):
    print "========= EMR =========="
    client = boto3.client('emr')
    bucket_name = (determine_service_base_name() + '-bucket').lower().replace('_', '-')
    tag_name = determine_service_base_name()
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
    list_obj = client.list_objects(Bucket=bucket_name)
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
    config = get_configuration(os.environ['PROVISION_CONFIG_DIR'])
    key_name = config.get('creds', 'key_name')
    user_name = config.get('notebook', 'ssh_user')
    path_to_key = '/root/keys/' + key_name + '.pem'
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
