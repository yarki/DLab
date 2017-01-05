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

# v1.3 from 05/10/2016
import boto3
from botocore.client import Config
import argparse
import re
import time
import sys
from fabric.api import *
from dlab.aws_meta import *
from dlab.aws_actions import *
import json
import os

parser = argparse.ArgumentParser()
# parser.add_argument('--id', type=str, default='')
parser.add_argument('--dry_run', action='store_true', help='Print all variables')
parser.add_argument('--name', type=str, default='', help='Name to be applied to Cluster ( MANDATORY !!! )')
parser.add_argument('--applications', type=str, default='',
                    help='Set of applications to be installed on EMR (Default are: "Hadoop Hive Hue Spark")')
parser.add_argument('--master_instance_type', type=str, default='', help='EC2 instance size for Master-Node (Default: m3.xlarge)')
parser.add_argument('--slave_instance_type', type=str, default='', help='EC2 instance size for Worker-Nodes (Default: m3.xlarge)')
parser.add_argument('--instance_count', type=int, default='',
                    help='Number of nodes the cluster will consist of (Default: 3)')
parser.add_argument('--release_label', type=str, default='', help='EMR release version (Default: "emr-4.8.0")')
parser.add_argument('--steps', type=str, default='')
parser.add_argument('--tags', type=str, default='')
parser.add_argument('--auto_terminate', action='store_true')
parser.add_argument('--service_role', type=str, default='',
                    help='Role name EMR cluster (Default: "EMR_DefaultRole")')
parser.add_argument('--ec2_role', type=str, default='',
                    help='Role name for EC2 instances in cluster (Default: "EMR_EC2_DefaultRole")')
parser.add_argument('--ssh_key', type=str, default='')
parser.add_argument('--availability_zone', type=str, default='')
parser.add_argument('--subnet', type=str, default='', help='Subnet CIDR')
parser.add_argument('--cp_jars_2_s3', action='store_true',
                    help='Copy executable JARS to S3 (Need only once per EMR release version)')
parser.add_argument('--nbs_ip', type=str, default='', help='Notebook server IP cluster should be attached to')
parser.add_argument('--nbs_user', type=str, default='',
                    help='Username to be used for connection to Notebook server')
parser.add_argument('--s3_bucket', type=str, default='', help='S3 bucket name to work with')
parser.add_argument('--emr_timeout', type=int)
parser.add_argument('--configurations', type=str, default='')
parser.add_argument('--region', type=str, default='')
args = parser.parse_args()

cp_config = "Name=CUSTOM_JAR, Args=aws s3 cp /etc/hive/conf/hive-site.xml s3://{0}/{4}/{5}/config/hive-site.xml --endpoint-url https://s3-{3}.amazonaws.com --region {3}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=aws s3 cp /etc/hadoop/conf/ s3://{0}/{4}/{5}/config/ --recursive --endpoint-url https://s3-{3}.amazonaws.com --region {3}, ActionOnFailure=TERMINATE_CLUSTER, Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=sudo -u hadoop hdfs dfs -mkdir /user/{2}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=aws s3 cp s3://{0}/{4}/{4}.pub /tmp/{4}.pub --endpoint-url https://s3-{3}.amazonaws.com --region {3}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=sudo -u hadoop hdfs dfs -chown -R {2}:{2} /user/{2}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar".format(
    args.s3_bucket, args.name, args.nbs_user, args.region, os.environ['edge_user_name'], args.name)

cp_jars = "Name=CUSTOM_JAR, Args=aws s3 cp s3://{0}/jars_parser.sh /tmp/jars_parser.sh --endpoint-url https://s3-{2}.amazonaws.com --region {2}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar;" \
          "Name=CUSTOM_JAR, Args=aws s3 cp s3://{0}/key_importer.sh /tmp/key_importer.sh --endpoint-url https://s3-{2}.amazonaws.com --region {2}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar;" \
          "Name=CUSTOM_JAR, Args=sh /tmp/key_importer.sh {4}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar; " \
          "Name=CUSTOM_JAR, Args=sh /tmp/jars_parser.sh {0} {3} {2} {4} {5}, ActionOnFailure=TERMINATE_CLUSTER,Jar=command-runner.jar".format(args.s3_bucket, args.release_label, args.region, args.release_label, os.environ['edge_user_name'], args.name)

logfile = '{}_creation.log'.format(args.name)
logpath = '/response/' + logfile
out = open(logpath, 'w')
out.close()


def get_object_count(bucket, prefix):
    s3_cli = boto3.client('s3', config=Config(signature_version='s3v4'), region_name=args.region)
    content = s3_cli.get_paginator('list_objects')
    file_list = []
    try:
        for i in content.paginate(Bucket=bucket, Delimiter='/', Prefix=prefix):
            for file in i.get('Contents'):
                file_list.append(file.get('Key'))
        count = len(file_list)
    except:
        print prefix + " still not exist. Waiting..."
        count = 0
    return count


def upload_jars_parser(args):
    s3 = boto3.resource('s3', config=Config(signature_version='s3v4'))
    s3.meta.client.upload_file('/root/scripts/jars_parser.sh', args.s3_bucket, 'jars_parser.sh')


def upload_user_key(args):
    s3 = boto3.resource('s3', config=Config(signature_version='s3v4'))
    s3.meta.client.upload_file(os.environ['creds_key_dir'] + '/' + os.environ['edge_user_name'] + '.pub', args.s3_bucket, os.environ['edge_user_name'] + '/' + os.environ['edge_user_name'] + '.pub')
    s3.meta.client.upload_file('/root/scripts/key_importer.sh', args.s3_bucket, 'key_importer.sh')


def remove_user_key(args):
    client = boto3.client('s3', config=Config(signature_version='s3v4'), region_name=args.region)
    client.delete_object(Bucket=args.s3_bucket, Key=os.environ['edge_user_name'] + '.pub')


def get_instance_by_ip(ip):
    ec2 = boto3.resource('ec2')
    check = bool(re.match(r"^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$", ip))
    if check:
        instances = ec2.instances.filter(Filters=[{'Name': 'private-ip-address', 'Values': [ip]}])
    else:
        instances = ec2.instances.filter(Filters=[{'Name': 'private-dns-name', 'Values': [ip]}])
    for instance in instances:
        return instance


def emr_sg(id):
    client = boto3.client('emr')
    emr=client.describe_cluster(ClusterId=id)
    master = emr['Cluster']['Ec2InstanceAttributes']['EmrManagedMasterSecurityGroup']
    slave = emr['Cluster']['Ec2InstanceAttributes']['EmrManagedSlaveSecurityGroup']
    return master, slave


def wait_emr(bucket, cluster_name, timeout, delay=30):
    deadline = time.time() + timeout
    prefix = os.environ['edge_user_name'] + '/' + cluster_name + "/config/"
    global cluster_id
    while time.time() < deadline:
        state = action_validate(cluster_id)
        if get_object_count(bucket, prefix) > 20 and state[1] == "WAITING":
            return True
        elif state[0] == "False":
            return False
        else:
            time.sleep(delay)
    return False


def parse_steps(step_string):
    parser = re.split('; |;', step_string)
    steps = []
    for i in parser:
        step_parser = re.split(', |,', i)
        task = {}
        hdp_jar_step = {}
        for j in step_parser:
            key, value = j.split("=")
            if key == "Args":
                value = value.split(" ")
                hdp_jar_step.update({key: value})
            elif key == "Jar":
                # if key=="Type":
                #    key="MainClass"
                hdp_jar_step.update({key: value})
            else:
                task.update({key: value})
        task.update({"HadoopJarStep": hdp_jar_step})
        steps.append(task)
    return steps


def action_validate(id):
    state = get_emr_info(id, 'Status')['State']
    if state in ("TERMINATING", "TERMINATED", "TERMINATED_WITH_ERRORS"):
        print "Cluster is alredy stopped. Bye"
        return ["False", state]
    elif state in ("RUNNING", "WAITING"):
        return ["True", state]
    else:
        print "Cluster is still being built."
        return ["True", state]


def read_json(path):
    try:
        with open(path) as json_data:
            data = json.load(json_data)
    except:
        data=[]
    return data


def build_emr_cluster(args):
    # Parse applications
    apps = args.applications.split(" ")
    names = []
    for i in apps:
        names.append({"Name": i})

    # Parse Tags
    parser = re.split('[, ]+', args.tags)
    tags = []
    for i in parser:
        key, value = i.split("=")
        tags.append({"Value": value, "Key": key})

    prefix = "jars/" + args.release_label + "/lib/"
    jars_exist = get_object_count(args.s3_bucket, prefix)

    # Parse steps
    if args.steps != '':
        global cp_config
        cp_config = cp_config + "; " + args.steps
    if args.cp_jars_2_s3 or jars_exist == 0:
        steps = parse_steps(cp_config + "; " + cp_jars)
    else:
        steps = parse_steps(cp_config)

    if args.dry_run:
        print "Build parameters are:"
        print args
        print "\n"
        print "Applications to be installed:"
        print names
        print "\n"
        print "Cluster tags:"
        print tags
        print "\n"
        print "Cluster Jobs:"
        print steps

    if not args.dry_run:
        socket = boto3.client('emr')
        result = socket.run_job_flow(
            Name=args.name,
            ReleaseLabel=args.release_label,
            Instances={'MasterInstanceType': args.master_instance_type,
                       'SlaveInstanceType': args.slave_instance_type,
                       'InstanceCount': args.instance_count,
                       'Ec2KeyName': args.ssh_key,
                       # 'Placement': {'AvailabilityZone': args.availability_zone},
                       'KeepJobFlowAliveWhenNoSteps': not args.auto_terminate,
                       'Ec2SubnetId': get_subnet_by_cidr(args.subnet)},
            Applications=names,
            Tags=tags,
            Steps=steps,
            VisibleToAllUsers=not args.auto_terminate,
            JobFlowRole=args.ec2_role,
            ServiceRole=args.service_role,
            Configurations=read_json(args.configurations))
        print "Cluster_id " + result.get('JobFlowId')
        return result.get('JobFlowId')


##############
# Run script #
##############

if __name__ == "__main__":

    if args.name == '':
        parser.print_help()
    elif args.dry_run:
        # get_emr_state(args.id)
        upload_jars_parser(args)
        upload_user_key(args)
        build_emr_cluster(args)
    else:
        upload_jars_parser(args)
        upload_user_key(args)
        out = open(logpath, 'a')
        nbs_id = get_instance_by_ip(args.nbs_ip)
        out.write('Notebook server "{}" IP is "{}"\n'.format(nbs_id, args.nbs_ip))
        current_sg = nbs_id.security_groups
        out.write('Current Notebooks SGs: {}\n'.format(current_sg))
        out.write('[BUILDING NEW CLUSTER - {}\n]'.format(args.name))
        cluster_id = build_emr_cluster(args)
        out.write('Cluster ID: {}\n'.format(cluster_id))
        if wait_emr(args.s3_bucket, args.name, args.emr_timeout):
            # Append Cluster's SGs to the Notebook server to grant access
            sg_list=[]
            for i in current_sg:
                sg_list.append(i['GroupId'])
            sg_master, sg_slave = emr_sg(cluster_id)
            sg_list.extend([sg_master, sg_slave])
            out.write('Updating SGs for Notebook to: {}\n'.format(sg_list))
            out.close()
            nbs_id.modify_attribute( Groups = sg_list)
        else:
            out.write("Timeout of {} seconds reached. Please increase timeout period and try again. Now terminating the cluster...".format(args.emr_timeout))
            out.close()
            if action_validate(cluster_id)[0] == "True":
                terminate_emr(cluster_id)
            s3_cleanup(args.s3_bucket, args.name, os.environ['edge_user_name'])
            sys.exit(1)
    sys.exit(0)