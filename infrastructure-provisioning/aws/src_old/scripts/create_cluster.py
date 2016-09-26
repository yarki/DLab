#!/usr/bin/python
import boto3
import argparse
import re
import time
import os
from fabric.api import *
from ConfigParser import SafeConfigParser

parser = argparse.ArgumentParser()
# parser.add_argument('--id', type=str, default='')
parser.add_argument('--dry_run', action='store_true', help='Print all variables')
parser.add_argument('--name', type=str, default='', help='Name to be applied to Cluster ( MANDATORY !!! )')
parser.add_argument('--applications', type=str, default='Hadoop Hive Hue Spark',
                    help='Set of applications to be installed on EMR (Default are: "Hadoop Hive Hue Spark")')
parser.add_argument('--instance_type', type=str, default='m3.xlarge', help='EC2 instance size (Default: m3.xlarge)')
parser.add_argument('--instance_count', type=int, default='3',
                    help='Number of nodes the cluster will consist of (Default: 3)')
parser.add_argument('--release_label', type=str, default='emr-4.8.0', help='EMR release version (Default: "emr-4.8.0")')
parser.add_argument('--steps', type=str, default='')
parser.add_argument('--tags', type=str, default='Name=DSA-POC-TEST, Project=DSA-POC')
parser.add_argument('--auto_terminate', action='store_true')
parser.add_argument('--service_role', type=str, default='EMR_DefaultRole',
                    help='Role name EMR cluster (Default: "EMR_DefaultRole")')
parser.add_argument('--ec2_role', type=str, default='EMR_EC2_DefaultRole',
                    help='Role name for EC2 instances in cluster (Default: "EMR_EC2_DefaultRole")')
parser.add_argument('--ssh_key', type=str, default='BDCC-DSA-POC')
parser.add_argument('--availability_zone', type=str, default='eu-west-1a')
parser.add_argument('--subnet', type=str, default='', help='Subnet CIDR')
parser.add_argument('--cp_jars_2_s3', action='store_true',
                    help='Copy executable JARS to S3 (Need only once per EMR release version)')
parser.add_argument('--nbs_ip', type=str, default='', help='Notebook server IP cluster should be attached to')
parser.add_argument('--nbs_user', type=str, default='ubuntu',
                    help='Username to be used for connection to Notebook server')
parser.add_argument('--s3_bucket', type=str, default='dsa-poc-test-bucket', help='S3 bucket name to work with')
args = parser.parse_args()

cp_config = "Name=CUSTOM_JAR, Args=aws s3 cp /etc/hive/conf/hive-site.xml s3://{}/config/{}/hive-site.xml, ActionOnFailure=CONTINUE,Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=aws s3 cp /etc/hadoop/conf/ s3://{}/config/{} --recursive, ActionOnFailure=CONTINUE, Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=sudo -u hadoop hdfs dfs -mkdir /user/{}, ActionOnFailure=CONTINUE,Jar=command-runner.jar; " \
            "Name=CUSTOM_JAR, Args=sudo -u hadoop hdfs dfs -chown -R {}:{} /user/{}, ActionOnFailure=CONTINUE,Jar=command-runner.jar".format(
    args.s3_bucket, args.name, args.s3_bucket, args.name, args.nbs_user, args.nbs_user, args.nbs_user, args.nbs_user)

cp_jars = "Name=CUSTOM_JAR, Args=aws s3 cp /usr/share/aws/ s3://{}/jars/{}/aws --recursive, ActionOnFailure=CONTINUE,Jar=command-runner.jar; " \
          "Name=CUSTOM_JAR,Args=aws s3 cp /usr/lib/hadoop/ s3://{}/jars/{}/lib --recursive,ActionOnFailure=CONTINUE,Jar=command-runner.jar".format(
    args.s3_bucket, args.release_label, args.s3_bucket, args.release_label)


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


def get_object_count(bucket, prefix):
    s3_cli = boto3.client('s3')
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


def get_subnet_from_cidr(cidr):
    ec2 = boto3.resource('ec2')
    for subnet in ec2.subnets.filter(Filters=[{'Name': 'cidrBlock', 'Values': [cidr]}]):
        return subnet.id
    return ''


def wait_emr(bucket, cluster_name, timeout=1200, delay=20):
    deadline = time.time() + timeout
    prefix = "config/" + cluster_name + "/"
    global cluster_id
    while time.time() < deadline:
        state = action_validate(cluster_id)
        if get_object_count(bucket, prefix) > 20 and state[1] == "WAITING":
            return True
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


def get_emr_state(id):
    emr = boto3.client('emr')
    data = emr.describe_cluster(
        ClusterId=id
    )
    state = data.get('Cluster').get('Status').get('State')
    return state


def get_emr_stats(region):
    emr = boto3.client('emr')
    clusters = emr.list_clusters(
        ClusterStates=['RUNNING', 'TERMINATED']
    )
    clusters = clusters.get('Clusters')
    result = clusters[0]
    return result


def action_validate(id):
    state = get_emr_state(id)
    if state in ("TERMINATING", "TERMINATED", "TERMINATED_WITH_ERRORS"):
        print "Cluster is alredy stopped. Bye"
        return ["False", state]
    elif state in ("RUNNING", "WAITING"):
        return ["True", state]
    else:
        print "Cluster is still being built. Please increase timeout period and try again. Now terminating the cluster..."
        return ["True", state]


def terminate_emr(id):
    emr = boto3.client('emr')
    emr.terminate_job_flows(
        JobFlowIds=[id]
    )


def s3_cleanup(bucket, cluster_name):
    s3_res = boto3.resource('s3')
    resource = s3_res.Bucket(bucket)
    prefix = "config/" + cluster_name + "/"
    for i in resource.objects.filter(Prefix=prefix):
        s3_res.Object(resource.name, i.key).delete()


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

    prefix = "jars/" + args.release_label + "/"
    jars_exist = get_object_count(args.s3_bucket, prefix)

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
            Instances={'MasterInstanceType': args.instance_type,
                       'SlaveInstanceType': args.instance_type,
                       'InstanceCount': args.instance_count,
                       'Ec2KeyName': args.ssh_key,
                       # 'Placement': {'AvailabilityZone': args.availability_zone},
                       'KeepJobFlowAliveWhenNoSteps': not args.auto_terminate,
                       'Ec2SubnetId': get_subnet_from_cidr(args.subnet)},
            Applications=names,
            Tags=tags,
            Steps=steps,
            VisibleToAllUsers=not args.auto_terminate,
            JobFlowRole=args.ec2_role,
            ServiceRole=args.service_role)
        print "Cluster_id " + result.get('JobFlowId')
        return result.get('JobFlowId')


##############
# Run script #
##############

if __name__ == "__main__":
    # Get info from configs and redefine defaults in argparse
    config = get_configuration(os.environ['PROVISION_CONFIG_DIR'])
    args.tags = "Name=" + config.get('conf','service_base_name')
    args.ssh_key = config.get('creds','key_name')
    args.s3_bucket = config.get('conf','service_base_name') + "-bucket"
    args.nbs_user = config.get('notebook','ssh_user')
    
    if args.name == '':
        parser.print_help()
    elif args.dry_run:
        # get_emr_state(args.id)
        build_emr_cluster(args)
    else:
        cluster_id = build_emr_cluster(args)
        if wait_emr(args.s3_bucket, args.name):
            if args.nbs_ip != '':
                env.hosts = args.nbs_ip
                env.user = args.nbs_user
                env.host_string = env.user + "@" + env.hosts
                env.key_filename = "/usr/share/notebook_automation/keys/BDCC-DSS-POC.pem"
                #env.key_filename = "/usr/share/notebook_automation/conf/keyfile.pem"
                sudo(
                    '/usr/bin/python /usr/local/bin/create_configs.py --bucket ' + args.s3_bucket + ' --cluster_name ' + args.name)
                nbs_id = get_instance_by_ip(args.nbs_ip)
                current_sg = nbs_id.security_groups
                sg_list=[]
                for i in current_sg:
                    sg_list.append(i['GroupId'])
                sg_master, sg_slave = emr_sg(cluster_id)
                sg_list.extend([sg_master, sg_slave])
                nbs_id.modify_attribute( Groups = sg_list)
            else:
                print "Notebook server IP and/or user are not defined ! Terminatig the cluster..."
                terminate_emr(cluster_id)
            #                s3_cleanup(args.name, args.s3_bucket)
        else:
            if action_validate(id)[0] == "True":
                terminate_emr(cluster_id)
                #               s3_cleanup(args.name, args.s3_bucket)
