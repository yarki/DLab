#!/usr/bin/python
import boto3
import argparse
import json
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--node_name', type=str, default='DSS-POC-TEST-instance')
parser.add_argument('--ami_id', type=str, default='ami-7172b611')
parser.add_argument('--instance_type', type=str, default='t2.small')
parser.add_argument('--key_name', type=str, default='BDCC-DSS-POC')
parser.add_argument('--security_group_ids', type=str, default='sg-1e0f7f79,sg-12345')
parser.add_argument('--subnet_id', type=str, default='subnet-1e6c9347')
parser.add_argument('--iam_profile', type=str, default='')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
parser.add_argument('--user_data_file', type=str, default='')
args = parser.parse_args()


def get_instance_by_name(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        return instance.id
    return ''


def get_instance_attr(instance_id, attribute_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'instance-id', 'Values': [instance_id]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        return getattr(instance, attribute_name)
    return ''


def create_instance(params, instance_tag):
    ec2 = boto3.resource('ec2')
    role_profile_name = params.iam_profile
    key_name = params.key_name
    ami_id = params.ami_id
    instance_type = params.instance_type
    subnet_id = params.subnet_id
    security_groups_ids = []
    for chunk in params.security_group_ids.split(','):
        security_groups_ids.append(chunk.strip())
    user_data_file = params.user_data_file
    
    ''' loading user_data_file ''' 
    user_data = ''
    if user_data_file != '':
        try:
            with open(user_data_file, 'r') as f: 
                for line in f:
                    user_data = user_data + line
            f.close()
        except:
            print("Error reading user-data file")
        
    instances = ec2.create_instances(ImageId=ami_id, MinCount=1, MaxCount=1,
                                     KeyName=key_name,
                                     SecurityGroupIds=security_groups_ids,
                                     InstanceType=instance_type,
                                     SubnetId=subnet_id,
                                     IamInstanceProfile={'Name': role_profile_name},
                                     UserData=user_data)
    for instance in instances:
        print "Waiting for instance " + instance.id + " become running."
        instance.wait_until_running()
        instance.create_tags(Tags=[{'Key': 'Name', 'Value': params.node_name}, instance_tag])
        return instance.id
    return ''


##############
# Run script #
##############

if __name__ == "__main__":
    instance_tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.node_name != '':
        instance_id = get_instance_by_name(args.node_name)
        if instance_id == '':
            print "Creating instance %s of type %s in subnet %s with tag %s." % \
                      (args.node_name, args.instance_type, args.subnet_id, json.dumps(instance_tag))
            instance_id = create_instance(args, instance_tag)
        else:
            print "REQUESTED INSTANCE ALREADY EXISTS AND RUNNING"
        print "Instance_id " + instance_id
        print "Public_hostname " + get_instance_attr(instance_id, 'public_dns_name')
        print "Private_hostname " + get_instance_attr(instance_id, 'private_dns_name')
    else:
        parser.print_help()
