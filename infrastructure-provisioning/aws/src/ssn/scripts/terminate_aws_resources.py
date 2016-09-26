#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
import boto3
import os
import argparse
from ConfigParser import SafeConfigParser

parser = argparse.ArgumentParser()
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--resource_name', type=str)
parser.add_argument('--ssn_tag_value_name', type=str)
parser.add_argument('--notebook_tag_value', type=str)
parser.add_argument('--notebook_name', type=str)
parser.add_argument('--instance_type', type=str)
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


# Function for terminating IAM roles
def remove_role(notebook_name, instance_type):
    print "========= Role =========="
    client = boto3.client('iam')
    if instance_type == "ssn":
        role_name = determine_service_base_name() + '-role'
        role_profile_name = determine_service_base_name() + '-role-profile'
    elif instance_type == "notebook":
        role_name = determine_service_base_name() + '-' + "{}".format(notebook_name) + '-role'
        role_profile_name = determine_service_base_name() + '-' + "{}".format(notebook_name) + '-role-profile'
    try:
        role = client.get_role(RoleName="{}".format(role_name)).get("Role").get("RoleName")
    except:
        print "Wasn't able to get role!"
    print "Name: ", role
    policy_list = client.list_attached_role_policies(RoleName=role).get('AttachedPolicies')
    for i in policy_list:
        policy_arn = i.get('PolicyArn')
        print policy_arn
        client.detach_role_policy(RoleName=role, PolicyArn=policy_arn)
    print "===== Role_profile ======"
    try:
        profile = client.get_instance_profile(InstanceProfileName="{}".format(role_profile_name)).get(
            "InstanceProfile").get("InstanceProfileName")
    except:
        print "Wasn't able to get instance profile!"
    print "Name: ", profile
    try:
        client.remove_role_from_instance_profile(InstanceProfileName=profile, RoleName=role)
    except:
        print "\nWasn't able to remove role from instance profile!"
    try:
        client.delete_instance_profile(InstanceProfileName=profile)
    except:
        print "\nWasn't able to remove instance profile!"
    try:
        client.delete_role(RoleName=role)
    except:
        print "\nWasn't able to remove role!"
    print "The IAM role " + role + " has been deleted successfully"


# Function for terminating any EC2 instances inc notebook servers
def remove_ec2(ssn_tag_value, notebook_tag_value):
    print "========== EC2 =========="
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    ssn_tag_name = determine_service_base_name() + '-tag'
    notebook_tag_name = determine_service_base_name()
    ssn_instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                 {'Name': 'tag:{}'.format(ssn_tag_name), 'Values': ['{}'.format(ssn_tag_value)]}])
    for instance in ssn_instances:
        print("ID: ", instance.id)
        client.terminate_instances(InstanceIds=[instance.id])
        waiter = client.get_waiter('instance_terminated')
        waiter.wait(InstanceIds=[instance.id])
        remove_role("", "ssn")
        print "The ssn instance " + instance.id + " has been deleted successfully"
    notebook_instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-state-name', 'Values': ['running', 'stopped']},
                 {'Name': 'tag:{}'.format(notebook_tag_name), 'Values': ['{}'.format(notebook_tag_value)]}])
    for instance in notebook_instances:
        print("ID: ", instance.id)
        for i in instance.tags:
            if i.get("Key") == "Name":
                notebook_name = i.get("Value")
        client.terminate_instances(InstanceIds=[instance.id])
        waiter = client.get_waiter('instance_terminated')
        waiter.wait(InstanceIds=[instance.id])
        remove_role(notebook_name, "notebook")
        print "The notebook instance " + instance.id + " has been deleted successfully"


# Function for terminating security groups
def remove_sgroups():
    print "========== SG ==========="
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    tag_name = determine_service_base_name() + '-tag'
    sgs = ec2.security_groups.filter(
        Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': ['*']}])
    for sg in sgs:
        print sg.id
        client.delete_security_group(GroupId=sg.id)
        print "The security group " + sg.id + " has been deleted successfully"


# Function for terminating subnets
def remove_subnets():
    print "======== Subnet ========="
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    tag_name = determine_service_base_name() + '-tag'
    subnets = ec2.subnets.filter(
        Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': ['*']}])
    for subnet in subnets:
        print subnet.id
        client.delete_subnet(SubnetId=subnet.id)
        print "The subnet " + subnet.id + " has been deleted successfully"


# Function for terminating VPC
def remove_vpc():
    print "========== VPC =========="
    ec2 = boto3.resource('ec2')
    client = boto3.client('ec2')
    tag_name = determine_service_base_name() + '-tag'
    vpcs = ec2.vpcs.filter(
        Filters=[{'Name': 'tag:{}'.format(tag_name), 'Values': ['*']}])
    for vpc in vpcs:
        print vpc.id
        client.delete_vpc(VpcId=vpc.id)
        print "The VPC " + vpc.id + " has been deleted successfully"


# Function for terminating S3 buckets
def remove_s3():
    print "========== S3 ==========="
    s3 = boto3.resource('s3')
    client = boto3.client('s3')
    bucket_name = (determine_service_base_name() + '-bucket').lower().replace('_', '-')
    bucket = s3.Bucket("{}".format(bucket_name))
    print bucket.name
    list_obj = client.list_objects(Bucket=bucket.name)
    try:
        list_obj = list_obj.get('Contents')
    except:
        print "Wasn't able to get S3!"
    if list_obj is not None:
        for o in list_obj:
            list_obj = o.get('Key')
            print list_obj
            client.delete_objects(
                Bucket=bucket_name,
                Delete={'Objects': [{'Key': list_obj}]}
            )
            print "The S3 bucket " + bucket.name + " has been cleaned"
    try:
        client.delete_bucket(Bucket=bucket.name)
    except:
        print "Wasn't able to remove S3!"
    print "The S3 bucket " + bucket.name + " has been deleted successfully"


# Function for terminating EMR clusters, cleaning buckets and removing notebook's local kernels
def remove_emr(emr_name):
    print "========= EMR =========="
    client = boto3.client('emr')
    clusters = client.list_clusters(ClusterStates=['STARTING', 'BOOTSTRAPPING', 'RUNNING', 'WAITING'])
    clusters = clusters.get("Clusters")
    for c in clusters:
        if c.get('Name') == "{}".format(emr_name):
            cluster_id = c.get('Id')
            cluster_name = c.get('Name')
            print cluster_id
            client.terminate_job_flows(JobFlowIds=[cluster_id])
            print "The EMR cluster " + cluster_name + " has been deleted successfully"


##############
# Run script #
##############

if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        if args.resource_name == "EC2":
            remove_ec2(args.ssn_tag_value, args.notebook_tag_value)
        elif args.resource_name == "SG":
            remove_sgroups()
        elif args.resource_name == "SUBNET":
            remove_subnets()
        elif args.resource_name == "S3":
            remove_s3()
        elif args.resource_name == "VPC":
            remove_vpc()
        elif args.resource_name == "ROLE":
            remove_role(args.notebook_name, args.instance_type)
        elif args.resource_name == "EMR":
            remove_emr(args.emr_name)
        elif args.resource_name == "all":
            remove_emr(args.emr_name)
            remove_ec2(args.ssn_tag_value, args.notebook_tag_value)
            remove_sgroups()
            remove_subnets()
            remove_s3()
            remove_vpc()
        else:
            print """
            Please type correct resource name to delete
            """
