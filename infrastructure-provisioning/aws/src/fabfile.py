#!/usr/bin/python
#  ============================================================================
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
import json


def get_configuration(configuration_dir):
    merged_config = SafeConfigParser()

    crid_config = SafeConfigParser()
    crid_config.read(configuration_dir + 'aws_crids.ini')
    for section in ['creds', 'ops']:
        if not merged_config.has_section(section):
            merged_config.add_section(section)
        for option, value in crid_config.items(section):
            merged_config.set(section, option, value)

    base_infra_config = SafeConfigParser()
    base_infra_config.read(configuration_dir + 'self_service_node.ini')
    for section in ['conf', 'ssn']:
        if not merged_config.has_section(section):
            merged_config.add_section(section)
        for option, value in base_infra_config.items(section):
            merged_config.set(section, option, value)

    overwrite_config = SafeConfigParser()
    overwrite_config.read(configuration_dir + 'overwrite.ini')
    for section in ['creds', 'conf', 'ssn']:
        if overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in overwrite_config.items(section):
                merged_config.set(section, option, value)

    shadow_overwrite_config = SafeConfigParser()
    shadow_overwrite_config.read(configuration_dir + 'shadow_overwrite.ini')
    for section in ['creds', 'conf', 'ssn']:
        if shadow_overwrite_config.has_section(section):
            if not merged_config.has_section(section):
                merged_config.add_section(section)
            for option, value in shadow_overwrite_config.items(section):
                merged_config.set(section, option, value)

    return merged_config


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


def create_aws_config_files():
    config = get_configuration(os.environ['PROVISION_CONFIG_DIR'])

    aws_user_dir = local('echo ~', capture=True) + "/.aws"
    logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
    logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))
    with open(aws_user_dir + '/credentials', 'w') as aws_file:
        aws_file.write("[default]\n")
        aws_file.write("aws_access_key_id = %s\n" % config.get('creds', 'access_key'))
        aws_file.write("aws_secret_access_key = %s\n" % config.get('creds', 'secret_access_key'))

    with open(aws_user_dir + '/config', 'w') as aws_file:
        aws_file.write("[default]\n")
        aws_file.write("region = %s\n" % config.get('creds', 'region'))

    logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
    logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))


def create_buckets(params):
    logging.info("~/scripts/create_bucket.py " + params)
    shell_out = local("~/scripts/create_bucket.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def create_roles(params):
    logging.info("~/scripts/create_role_policy.py " + params)
    shell_out = local("~/scripts/create_role_policy.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def create_instance(params):
    logging.info("~/scripts/create_instance.py " + params)
    shell_out = local("~/scripts/create_instance.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def install_prerequisites(params):
    logging.info("~/scripts/install_prerequisites.py " + params)
    shell_out = local("~/scripts/install_prerequisites.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def configure_ssn(params):
    logging.info("~/scripts/configure_ssn.py " + params)
    shell_out = local("~/scripts/configure_ssn.py " + params)
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def report(params):
    logging.info("~/scripts/put_to_bucket.py " + params)
    shell_out = local("~/scripts/put_to_bucket.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def finalize(params):
    logging.info("~/scripts/finalize.py " + params)
    shell_out = local("~/scripts/finalize.py " + params)
    print shell_out
    logging.info(shell_out)
    print shell_out.stderr
    logging.error(shell_out.stderr)


def run():
    config = get_configuration(os.environ['PROVISION_CONFIG_DIR'])

    local_log_filename = "self_service_node_creation.log"
    local_log_filepath = "%s/%s" % (config.get('conf', 'local_log_dir'), local_log_filename)
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    env.warn_only = True

    logging.info('[CREATE AWS CONFIG FILE]')
    print '[CREATE AWS CONFIG FILE]'
    with hide('stderr', 'running'):
        create_aws_config_files()

    logging.info('[DERIVING NAMES]')
    print '[DERIVING NAMES]'
    service_base_name = config.get('conf', 'service_base_name')
    role_name = service_base_name + '-Role'
    role_profile_name = role_name + '-Profile'
    policy_name = role_name + '-Policy'
    user_bucket_name = (service_base_name + '-bucket').lower().replace('_', '-')
    tag_name = service_base_name + '-Tag'
    instance_name = service_base_name + '-ssn-instance'

    logging.info('[CREATE ROLES]')
    print('[CREATE ROLES]')
    params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
             (role_name, role_profile_name, policy_name, config.get('conf', 'policy_arn'))
    with hide('stderr', 'running'):
        create_roles(params)

    logging.info('[CREATE BUCKETS]')
    print('[CREATE BUCKETS]')
    params = "--bucket_name %s --infra_tag_name %s --infra_tag_value %s" % \
             (user_bucket_name, tag_name, "bucket")
    with hide('stderr', 'running'):
        create_buckets(params)

    logging.info('[CREATE SSN INSTANCE]')
    print('[CREATE SSN INSTANCE]')
    params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
             "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
             (instance_name, config.get('ssn', 'ami_id'), config.get('ssn', 'instance_type'),
              config.get('creds', 'key_name'), config.get('creds', 'security_groups_ids'),
              config.get('creds', 'subnet_id'), role_profile_name, tag_name, 'ssn')
    with hide('stderr', 'running'):
        create_instance(params)

    instance_hostname = get_hostname(instance_name)

    logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    params = "--hostname %s --keyfile %s " % \
             (instance_hostname, "/root/keys/%s.pem" % config.get('creds', 'key_name'))
    with hide('stderr', 'running'):
        install_prerequisites(params)

    logging.info('[CONFIGURE SSN INSTANCE]')
    print('[CONFIGURE SSN INSTANCE]')
    additional_config = {"nginx_template_dir": "/root/templates/",
                         "squid_template_file": "/root/templates/squid.conf",
                         "proxy_port": config.get("ssn", "proxy_port"),
                         "proxy_subnet": config.get("ssn", "proxy_subnet")}
    params = "--hostname %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, "/root/keys/%s.pem" % config.get('creds', 'key_name'), json.dumps(additional_config))
    with hide('stderr', 'running'):
        configure_ssn(params)

    logging.info('[REPORT TO BUCKET]')
    print('[REPORT TO BUCKET]')
    params = "--bucket_name %s --local_file %s --destination_file %s" % \
             (user_bucket_name, local_log_filepath, local_log_filename)
    with hide('stderr', 'running'):
        report(params)

    jenkins_url = "http://%s/jenkins" % get_hostname(instance_name)
    print "Jenkins URL: " + jenkins_url
    with open('jenkins_crids.txt') as f:
        print f.read()

    logging.info('[FINALIZE]')
    print('[FINALIZE]')
    params = ""
    if config.get('ops', 'lifecycle_stage') == 'prod':
        params += "--key_id %s" % config.get('creds', 'access_key')
        finalize(params)
