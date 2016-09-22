#!/usr/bin/python
# ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
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
# ============================================================================
from fabric.api import *
from ConfigParser import SafeConfigParser
import os
import logging
import boto3
from time import gmtime, strftime
import json
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--notebook_name', type=str, default='')
parser.add_argument('--subnet_cidr', type=str, default='')
parser.add_argument('--config_dir', type=str, default='/root/conf/')
args = parser.parse_args()


def get_ami_id_by_name(ami_name):
    ec2 = boto3.resource('ec2')
    try:
        for image in ec2.images.filter(Filters=[{'Name': 'name', 'Values': [ami_name]}]):
            return image.id
    except:
        return ''
    return ''


def get_configuration(configuration_dir):
    merged_config = SafeConfigParser()

    notebook_config = SafeConfigParser()
    notebook_config.read(configuration_dir + 'jupyter.ini')
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


def create_aws_config_files(config):
    aws_user_dir = local('echo ~', capture=True) + "/.aws"
    logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
    logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))
    with open(aws_user_dir + '/config', 'w') as aws_file:
        aws_file.write("[default]\n")
        aws_file.write("region = %s\n" % config.get('creds', 'region'))

    logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
    logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))


def get_hostname(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        public = getattr(instance, 'public_dns_name')
        private = getattr(instance, 'private_dns_name')
        if public:
            return public
        else:
            return private


def get_ip_address(instance_name):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        public = getattr(instance, 'public_ip_address')
        private = getattr(instance, 'private_ip_address')
        if public:
            return public
        else:
            return private


def create_image_from_instance(instance_name='', image_name=''):
    ec2 = boto3.resource('ec2')
    instances = ec2.instances.filter(
        Filters=[{'Name': 'tag:Name', 'Values': [instance_name]},
                 {'Name': 'instance-state-name', 'Values': ['running']}])
    for instance in instances:
        image = instance.create_image(Name=image_name,
                                      Description='Automatically created image for notebook server',
                                      NoReboot=True)
        image.load()
        while image.state != 'available':
            local("echo Waiting for image creation; sleep 20")
            image.load()
        return image.id
    return ''


def get_subnet_by_cidr(cidr):
    ec2 = boto3.resource('ec2')
    for subnet in ec2.subnets.filter(Filters=[{'Name': 'cidrBlock', 'Values': [cidr]}]):
        return subnet.id
    return ''


def get_security_group_by_name(security_group_name):
    ec2 = boto3.resource('ec2')
    for security_group in ec2.security_groups.filter(GroupNames=[security_group_name]):
        return security_group.id
    return ''


def run_routine(routine_name, params):
    logging.info("~/scripts/%s.py %s" % (routine_name, params))
    shell_out = local("~/scripts/%s.py %s" % (routine_name, params))
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def run(config):
    nb_config = config._sections['notebook']

    logging.info('[CREATE SUBNET]')
    print '[CREATE SUBNET]'
    params = "--vpc_id '%s' --subnet '%s' --region %s --infra_tag_name %s --infra_tag_value %s" % \
             (nb_config['vpc_id'], nb_config['subnet'], nb_config['region'],
              config.get('conf', 'service_base_name'), nb_config['instance_name'])
    run_routine('create_subnet', params)

    logging.info('[CREATE ROLES]')
    print '[CREATE ROLES]'
    params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
             (nb_config['role_name'], nb_config['role_profile_name'],
              nb_config['policy_name'], nb_config['policy_arn'])
    run_routine('create_role_policy', params)

    logging.info('[CREATE SECURITY GROUPS]')
    print '[CREATE SECURITY GROUPS]'
    params = "--name %s --subnet %s --security_group_rules %s --infra_tag_name %s --infra_tag_value %s" % \
             (nb_config['security_group_name'], nb_config['subnet'], nb_config['security_group_rules'],
              config.get('conf', 'service_base_name'), nb_config['instance_name'])
    run_routine('create_security_group', params)

    with hide('stderr', 'running', 'warnings'):
        local("echo Waitning for changes to propagate; sleep 10")

    logging.info('[CREATE JUPYTER NOTEBOOK INSTANCE]')
    print '[CREATE JUPYTER NOTEBOOK INSTANCE]'
    params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
             "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
             (nb_config['instance_name'], nb_config['ami_id'], nb_config['instance_type'], nb_config['key_name'],
              get_security_group_by_name(nb_config['security_group_name']),
              get_subnet_by_cidr(nb_config['subnet']), nb_config['role_profile_name'],
              config.get('conf', 'service_base_name'), nb_config['instance_name'])
    run_routine('create_instance', params)

    instance_hostname = get_hostname(nb_config['instance_name'])
    ssn_instance_name = config.get('conf', 'service_base_name') + '-ssn-instance'
    ssn_instance_hostname = get_hostname(ssn_instance_name)
    keyfile_name = "/root/keys/%s.pem" % config.get('creds', "key_name")

    logging.info('[CONFIGURE PROXY ON JUPYTER INSTANCE]')
    print '[CONFIGURE PROXY ON JUPYTER INSTANCE]'
    additional_config = {"proxy_host": ssn_instance_hostname, "proxy_port": "3128"}
    params = "--hostname %s --instance_name %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, nb_config['instance_name'], keyfile_name, json.dumps(additional_config))
    run_routine('configure_proxy', params)

    logging.info('[INSTALLING PREREQUISITES TO JUPYTER NOTEBOOK INSTANCE]')
    print('[INSTALLING PREREQUISITES TO JUPYTER NOTEBOOK INSTANCE]')
    params = "--hostname %s --keyfile %s " % (instance_hostname, keyfile_name)
    run_routine('install_prerequisites', params)

    logging.info('[CONFIGURE JUPYTER NOTEBOOK INSTANCE]')
    print '[CONFIGURE JUPYTER NOTEBOOK INSTANCE]'
    additional_config = {"frontend_hostname": ssn_instance_hostname,
                         "backend_hostname": get_hostname(nb_config['instance_name']),
                         "backend_port": "8888",
                         "nginx_template_dir": "/usr/share/notebook_automation/templates/"}
    params = "--hostname %s --instance_name %s --keyfile %s --additional_config '%s'" %  \
             (instance_hostname, nb_config['instance_name'], keyfile_name, json.dumps(additional_config))
    run_routine('configure_jupyter_node', params)

    logging.info('[CONFIGURE JUPYTER ADDITIONS]')
    print '[CONFIGURE JUPYTER ADDITIONS]'
    params = "--hostname %s --keyfile %s" % (instance_hostname, keyfile_name)
    run_routine('install_jupyter_additions', params)


if __name__ == "__main__":
    if args.notebook_name == '' or args.subnet_cidr == '':
        parser.print_help()
        sys.exit(2)

    config = get_configuration(args.config_dir)

    notebook_instance_name = config.get('conf', 'service_base_name') + '-notebook-' + args.notebook_name
    expected_ami_name = config.get('conf', 'service_base_name') + '-notebook-image'

    local_log_filename = "%s.log" % notebook_instance_name
    local_log_filepath = "%s/%s" % (os.environ['PWD'], local_log_filename)
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    env.warn_only = True
    with hide('stderr', 'running'):
        create_aws_config_files(config)

    print 'Searching preconfigured images'
    ami_id = get_ami_id_by_name(expected_ami_name)
    config.set('notebook', 'instance_name', notebook_instance_name)
    config.set('notebook', 'role_name', notebook_instance_name + "-Role")
    config.set('notebook', 'role_profile_name', notebook_instance_name + "-Role-Profile")
    config.set('notebook', 'policy_name', notebook_instance_name + "-Role-Policy")
    config.set('notebook', 'security_group_name', notebook_instance_name + "-SG")
    config.set('notebook', 'subnet', args.subnet_cidr)
    config.set('notebook', 'key_name', config.get('creds', 'key_name'))

    if ami_id != '':
        print 'Preconfigured image found. Using: ' + ami_id
        config.set('notebook', 'ami_id', ami_id)
    else:
        print 'No preconfigured image found. Using default one: ' + config.get('notebook', 'ami_id')

    run(config)

    with open("/tmp/" + notebook_instance_name + "passwd.file") as f:
        ip_address = get_ip_address(notebook_instance_name)
        dns_name = get_hostname(notebook_instance_name)
        print "Notebook access url: http://%s/%s" % \
              (get_hostname(config.get('conf', 'service_base_name') + '-ssn-instance'), notebook_instance_name)
        print "Notebook access password: " + f.read()
        print 'SSH access (from Edge node, via IP address): ssh -i keyfile.pem ubuntu@' + ip_address
        print 'SSH access (from Edge node, via FQDN): ssh -i keyfile.pem ubuntu@' + dns_name

    if ami_id == '' and os.path.isfile("/tmp/" + notebook_instance_name + "passwd.file"):
        print "Looks like it's first time we configure notebook server. Creating image."
        image_id = create_image_from_instance(instance_name=notebook_instance_name,
                                              image_name=expected_ami_name)
        if image_id != '':
            print "Image succesfully created. It's ID is " + image_id

