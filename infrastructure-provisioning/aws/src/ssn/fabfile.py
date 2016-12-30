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

import json
from dlab.fab import *
from dlab.aws_meta import *
from dlab.aws_actions import *
import sys, os
from fabric.api import *


def run():
    local_log_filename = "{}_{}.log".format(os.environ['resource'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    instance = 'ssn'
    try:
        logging.info('[CREATE AWS CONFIG FILE]')
        print '[CREATE AWS CONFIG FILE]'
        if not create_aws_config_files(generate_full_config=True):
            logging.info('Unable to create configuration')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create configuration", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[DERIVING NAMES]')
        print '[DERIVING NAMES]'
        service_base_name = os.environ['conf_service_base_name']
        role_name = service_base_name + '-ssn-Role'
        role_profile_name = service_base_name + '-ssn-Profile'
        policy_name = service_base_name + '-ssn-Policy'
        user_bucket_name = (service_base_name + '-ssn-bucket').lower().replace('_', '-')
        tag_name = service_base_name + '-Tag'
        instance_name = service_base_name + '-ssn'
        region = os.environ['creds_region']
        ssn_ami_id = get_ami_id(os.environ['ssn_ami_name'])
        policy_path = '/root/templates/policy.json'
        vpc_cidr = '172.31.0.0/16'
        sg_name = instance_name + '-SG'

        if os.environ['creds_vpc_id'] == '' or os.environ['creds_vpc_id'] == 'PUT_YOUR_VALUE_HERE':
            try:
                params = "--vpc {} --region {} --infra_tag_name {} --infra_tag_value {}".format(vpc_cidr, region, tag_name, instance_name)
                if not run_routine('create_vpc', params):
                    logging.info('Failed to create VPC')
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed to create VPC"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                    sys.exit(1)
                os.environ['creds_vpc_id'] = get_vpc_by_tag(tag_name, instance_name)
            except:
                sys.exit(1)

        if os.environ['creds_subnet_id'] == '' or os.environ['creds_subnet_id'] == 'PUT_YOUR_VALUE_HERE':
            try:
                params = "--vpc_id {} --username {} --infra_tag_name {} --infra_tag_value {} --prefix {} --ssn {}".format(os.environ['creds_vpc_id'], 'ssn', tag_name, instance_name, '20', True)
                if not run_routine('create_subnet', params):
                    logging.info('Failed to create Subnet')
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed to create Subnet"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                    sys.exit(1)
                with open('/tmp/ssn_subnet_id', 'r') as f:
                    os.environ['creds_subnet_id'] = f.read()
                enable_auto_assign_ip(os.environ['creds_subnet_id'])
            except:
                remove_vpc(os.environ['creds_vpc_id'])
                sys.exit(1)

        if os.environ['creds_security_groups_ids'] == '' or os.environ['creds_security_groups_ids'] == 'PUT_YOUR_VALUE_HERE':
            try:
                ingress_sg_rules_template = [
                    {
                        "PrefixListIds": [],
                        "FromPort": 80,
                        "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                        "ToPort": 80, "IpProtocol": "tcp", "UserIdGroupPairs": []
                    },
                    {
                        "PrefixListIds": [],
                        "FromPort": 8080,
                        "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                        "ToPort": 8080, "IpProtocol": "tcp", "UserIdGroupPairs": []
                    },
                    {
                        "PrefixListIds": [],
                        "FromPort": 22,
                        "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                        "ToPort": 22, "IpProtocol": "tcp", "UserIdGroupPairs": []
                    },
                    {
                        "PrefixListIds": [],
                        "FromPort": 3128,
                        "IpRanges": [{"CidrIp": vpc_cidr}],
                        "ToPort": 3128, "IpProtocol": "tcp", "UserIdGroupPairs": []
                    },
                    {
                        "PrefixListIds": [],
                        "FromPort": 443,
                        "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                        "ToPort": 443, "IpProtocol": "tcp", "UserIdGroupPairs": []
                    },
                    {
                        "PrefixListIds": [],
                        "FromPort": -1,
                        "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                        "ToPort": -1, "IpProtocol": "icmp", "UserIdGroupPairs": []
                    }
                ]
                egress_sg_rules_template = [
                    {"IpProtocol": "-1", "IpRanges": [{"CidrIp": "0.0.0.0/0"}], "UserIdGroupPairs": [], "PrefixListIds": []}
                ]
                params = "--name {} --vpc_id {} --security_group_rules '{}' --egress '{}' --infra_tag_name {} --infra_tag_value {} --force {}". \
                    format(sg_name, os.environ['creds_vpc_id'], json.dumps(ingress_sg_rules_template), json.dumps(egress_sg_rules_template), tag_name, instance_name, False)
                if not run_routine('create_security_group', params):
                    logging.info('Failed creating security group for SSN')
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed creating security group for SSN"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                    sys.exit(1)
                with open('/tmp/ssn_sg_id', 'r') as f:
                    os.environ['creds_security_groups_ids'] = f.read()
            except:
                sys.exit(1)
        print "HERE-----------------------------------------------!!!!"
        logging.info('[CREATE ROLES]')
        print('[CREATE ROLES]')
        params = "--role_name %s --role_profile_name %s --policy_name %s --policy_file_name %s" % \
                 (role_name, role_profile_name, policy_name, policy_path)

        if not run_routine('create_role_policy', params):
            logging.info('Unable to create roles')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create roles", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CREATE ENDPOINT AND ROUTE-TABLE]')
        print('[CREATE ENDPOINT AND ROUTE-TABLE]')
        params = "--vpc_id {} --region {} --infra_tag_name {} --infra_tag_value {}".format(
            os.environ['creds_vpc_id'], os.environ['creds_region'], tag_name, service_base_name)
        if not run_routine('create_endpoint', params):
            logging.info('Unable to create Endpoint')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create an endpoint", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_all_iam_resources(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CREATE BUCKETS]')
        print('[CREATE BUCKETS]')
        params = "--bucket_name %s --infra_tag_name %s --infra_tag_value %s --region %s" % \
                 (user_bucket_name, tag_name, "bucket", region)

        if not run_routine('create_bucket', params):
            logging.info('Unable to create bucket')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create bucket", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_all_iam_resources(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CREATE SSN INSTANCE]')
        print('[CREATE SSN INSTANCE]')
        params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
                 "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
                 (instance_name, ssn_ami_id, os.environ['ssn_instance_size'],
                  os.environ['creds_key_name'], os.environ['creds_security_groups_ids'],
                  os.environ['creds_subnet_id'], role_profile_name, tag_name, instance_name)

        if not run_routine('create_instance', params):
            logging.info('Unable to create ssn instance')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create ssn instance", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        instance_hostname = get_instance_hostname(instance_name)

        logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        params = "--hostname %s --keyfile %s " \
                 "--pip_packages 'boto3 argparse fabric jupyter awscli'" % \
                 (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])

        if not run_routine('install_prerequisites', params):
            logging.info('Failed installing software: pip, apt')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing software: pip, apt", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CONFIGURE SSN INSTANCE]')
        print('[CONFIGURE SSN INSTANCE]')
        additional_config = {"nginx_template_dir": "/root/templates/"}
        params = "--hostname %s --keyfile %s --additional_config '%s'" % \
                 (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))

        if not run_routine('configure_ssn', params):
            logging.info('Failed configuring ssn')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed configuring ssn", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CONFIGURING DOCKER AT SSN INSTANCE]')
        print('[CONFIGURING DOCKER AT SSN INSTANCE]')
        additional_config = [{"name": "base", "tag": "latest"},
                             {"name": "jupyter", "tag": "latest"},
                             {"name": "rstudio", "tag": "latest"},
                             {"name": "edge", "tag": "latest"},
                             {"name": "emr", "tag": "latest"}, ]
        params = "--hostname %s --keyfile %s --additional_config '%s'" % \
                 (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))

        if not run_routine('configure_docker', params):
            logging.info('Unable to configure docker')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to configure docker", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[CONFIGURE SSN INSTANCE UI]')
        print('[CONFIGURE SSN INSTANCE UI]')
        params = "--hostname %s --keyfile %s " \
                 "--pip_packages 'pymongo pyyaml'" % \
                 (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])

        if not run_routine('install_prerequisites', params):
            logging.info('Unable to preconfigure ui')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to preconfigure ui", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        params = "--hostname %s --keyfile %s" % \
                 (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])

        if not run_routine('configure_ui', params):
            logging.info('Unable to upload UI')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to upload UI", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

    try:
        logging.info('[SUMMARY]')
        print('[SUMMARY]')
        print "Service base name: " + service_base_name
        print "SSN Name: " + instance_name
        print "SSN Hostname: " + instance_hostname
        print "Role name: " + role_name
        print "Role profile name: " + role_profile_name
        print "Policy name: " + policy_name
        print "Key name: " + os.environ['creds_key_name']
        print "Policies: " + os.environ['conf_policy_arn']
        print "VPC ID: " + os.environ['creds_vpc_id']
        print "Subnet ID: " + os.environ['creds_subnet_id']
        print "Security IDs: " + os.environ['creds_security_groups_ids']
        print "SSN instance shape: " + os.environ['ssn_instance_size']
        print "SSN AMI name: " + os.environ['ssn_ami_name']
        print "SSN bucket name: " + user_bucket_name
        print "Region: " + region
        jenkins_url = "http://%s/jenkins" % get_instance_hostname(instance_name)
        print "Jenkins URL: " + jenkins_url
        try:
            with open('jenkins_crids.txt') as f:
                print f.read()
        except:
            print "Jenkins is either configured already or have issues in configuration routine."

        with open("/root/result.json", 'w') as f:
            res = {"service_base_name": service_base_name,
                   "instance_name": instance_name,
                   "instance_hostname": get_instance_hostname(instance_name),
                   "role_name": role_name,
                   "role_profile_name": role_profile_name,
                   "policy_name": policy_name,
                   "master_keyname": os.environ['creds_key_name'],
                   "policies": os.environ['conf_policy_arn'],
                   "vpc_id": os.environ['creds_vpc_id'],
                   "subnet_id": os.environ['creds_subnet_id'],
                   "security_id": os.environ['creds_security_groups_ids'],
                   "instance_shape": os.environ['ssn_instance_size'],
                   "bucket_name": user_bucket_name,
                   "region": region,
                   "action": "Create SSN instance"}
            f.write(json.dumps(res))

        print 'Upload response file'
        instance_hostname = get_instance_hostname(instance_name)
        print 'Connect to SSN instance with hostname: ' + instance_hostname + 'and name: ' + instance_name
        env['connection_attempts'] = 100
        env.key_filename = "/root/keys/%s.pem" % os.environ['creds_key_name']
        env.host_string = 'ubuntu@' + instance_hostname
        try:
            put('/root/result.json', '/home/ubuntu/%s.json' % os.environ['request_id'])
            sudo('mv /home/ubuntu/' + os.environ['request_id'] + '.json ' + os.environ['ssn_dlab_path'] + 'tmp/result/')
            put(local_log_filepath, '/home/ubuntu/ssn.log')
            sudo('mv /home/ubuntu/ssn.log /var/opt/dlab/log/ssn/')
        except:
            print 'Failed to upload response file'
            sys.exit(1)

        logging.info('[FINALIZE]')
        print('[FINALIZE]')
        params = ""
        if os.environ['ops_lifecycle_stage'] == 'prod':
            params += "--key_id %s" % os.environ['creds_access_key']
            run_routine('finalize', params)
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        try:
            remove_sgroups(instance_name)
        except:
            print "Pre-defined security group exists. Removing SG for SSN will be removed."
        sys.exit(1)

def terminate():
    local_log_filename = "{}_{}.log".format(os.environ['resource'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files(generate_full_config=True)
    print 'Generating infrastructure names and tags'
    ssn_conf = dict()
    ssn_conf['service_base_name'] = os.environ['conf_service_base_name']
    ssn_conf['tag_name'] = ssn_conf['service_base_name'] + '-Tag'
    ssn_conf['edge_sg'] = ssn_conf['service_base_name'] + "*" + '-edge'
    ssn_conf['nb_sg'] = ssn_conf['service_base_name'] + "*" + '-nb'

    try:
        logging.info('[TERMINATE SSN]')
        print '[TERMINATE SSN]'
        params = "--tag_name %s --edge_sg %s --nb_sg %s" % \
                 (ssn_conf['tag_name'], ssn_conf['edge_sg'], ssn_conf['nb_sg'])
        if not run_routine('terminate_aws_resources', params):
            logging.info('Failed to terminate ssn')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to terminate ssn", "conf": ssn_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"service_base_name": ssn_conf['service_base_name'],
                   "Action": "Terminate ssn with all service_base_name environment"}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        print "Failed writing results."
        sys.exit(0)