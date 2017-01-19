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

from dlab.fab import *
from dlab.aws_actions import *
import sys, os
from fabric.api import *
from dlab.ssn_lib import *


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
        role_name = service_base_name.lower().replace('-', '_') + '-ssn-Role'
        role_profile_name = service_base_name.lower().replace('-', '_') + '-ssn-Profile'
        policy_name = service_base_name.lower().replace('-', '_') + '-ssn-Policy'
        user_bucket_name = (service_base_name + '-ssn-bucket').lower().replace('_', '-')
        tag_name = service_base_name + '-Tag'
        instance_name = service_base_name + '-ssn'
        region = os.environ['aws_region']
        ssn_ami_id = get_ami_id(os.environ['aws_debian_ami_name'])
        policy_path = '/root/templates/policy.json'
        vpc_cidr = '172.31.0.0/16'
        sg_name = instance_name + '-SG'
        pre_defined_vpc = False
        pre_defined_sg = False

        if os.environ['aws_vpc_id'] == '' or os.environ['aws_vpc_id'] == 'PUT_YOUR_VALUE_HERE':
            try:
                pre_defined_vpc = True
                logging.info('[CREATE VPC AND ROUTE TABLE]')
                print '[CREATE VPC AND ROUTE TABLE]'
                params = "--vpc {} --region {} --infra_tag_name {} --infra_tag_value {}".format(vpc_cidr, region, tag_name, service_base_name)
                try:
                    local("~/scripts/{}.py {}".format('create_vpc', params))
                except:
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed to create VPC"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                        raise Exception
                os.environ['aws_vpc_id'] = get_vpc_by_tag(tag_name, service_base_name)
                enable_vpc_dns(os.environ['aws_vpc_id'])
                rt_id = create_rt(os.environ['aws_vpc_id'], tag_name, service_base_name)
            except:
                sys.exit(1)

        if os.environ['aws_subnet_id'] == '' or os.environ['aws_subnet_id'] == 'PUT_YOUR_VALUE_HERE':
            try:
                pre_defined_vpc = True
                logging.info('[CREATE SUBNET]')
                print '[CREATE SUBNET]'
                params = "--vpc_id {} --username {} --infra_tag_name {} --infra_tag_value {} --prefix {} --ssn {}".format(os.environ['aws_vpc_id'], 'ssn', tag_name, service_base_name, '20', True)
                try:
                    local("~/scripts/{}.py {}".format('create_subnet', params))
                except:
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed to create Subnet"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                        raise Exception
                with open('/tmp/ssn_subnet_id', 'r') as f:
                    os.environ['aws_subnet_id'] = f.read()
                enable_auto_assign_ip(os.environ['aws_subnet_id'])
            except:
                if pre_defined_vpc:
                    remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
                    remove_route_tables(tag_name, True)
                    try:
                        remove_subnets(service_base_name + "-subnet")
                    except:
                        print "Subnet hasn't been created."
                    remove_vpc(os.environ['aws_vpc_id'])
                sys.exit(1)

        if os.environ['aws_security_groups_ids'] == '' or os.environ['aws_security_groups_ids'] == 'PUT_YOUR_VALUE_HERE':
            try:
                pre_defined_sg = True
                logging.info('[CREATE SG FOR SSN]')
                print '[CREATE SG FOR SSN]'
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
                params = "--name {} --vpc_id {} --security_group_rules '{}' --egress '{}' --infra_tag_name {} --infra_tag_value {} --force {} --ssn {}". \
                    format(sg_name, os.environ['aws_vpc_id'], json.dumps(ingress_sg_rules_template), json.dumps(egress_sg_rules_template), service_base_name, tag_name, False, True)
                try:
                    local("~/scripts/{}.py {}".format('create_security_group', params))
                except:
                    with open("/root/result.json", 'w') as result:
                        res = {"error": "Failed creating security group for SSN"}
                        print json.dumps(res)
                        result.write(json.dumps(res))
                        raise Exception
                with open('/tmp/ssn_sg_id', 'r') as f:
                    os.environ['aws_security_groups_ids'] = f.read()
            except:
                if pre_defined_vpc:
                    remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
                    remove_subnets(service_base_name + "-subnet")
                    remove_route_tables(tag_name, True)
                    remove_vpc(os.environ['aws_vpc_id'])
                sys.exit(1)
        logging.info('[CREATE ROLES]')
        print('[CREATE ROLES]')
        params = "--role_name {} --role_profile_name {} --policy_name {} --policy_file_name {}". \
                format(role_name, role_profile_name, policy_name, policy_path)
        try:
            local("~/scripts/{}.py {}".format('create_role_policy', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create roles", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CREATE ENDPOINT AND ROUTE-TABLE]')
        print('[CREATE ENDPOINT AND ROUTE-TABLE]')
        params = "--vpc_id {} --region {} --infra_tag_name {} --infra_tag_value {}".format(
            os.environ['aws_vpc_id'], os.environ['aws_region'], tag_name, service_base_name)
        try:
            local("~/scripts/{}.py {}".format('create_endpoint', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create an endpoint", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_all_iam_resources(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CREATE BUCKETS]')
        print('[CREATE BUCKETS]')
        params = "--bucket_name {} --infra_tag_name {} --infra_tag_value {} --region {}". \
                 format(user_bucket_name, tag_name, user_bucket_name, region)

        try:
            local("~/scripts/{}.py {}".format('create_bucket', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create bucket", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_all_iam_resources(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CREATE SSN INSTANCE]')
        print('[CREATE SSN INSTANCE]')
        params = "--node_name {} --ami_id {} --instance_type {} --key_name {} --security_group_ids {} " \
                 "--subnet_id {} --iam_profile {} --infra_tag_name {} --infra_tag_value {}". \
                 format(instance_name, ssn_ami_id, os.environ['ssn_instance_size'],
                  os.environ['conf_key_name'], os.environ['aws_security_groups_ids'],
                  os.environ['aws_subnet_id'], role_profile_name, tag_name, instance_name)

        try:
            local("~/scripts/{}.py {}".format('create_instance', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create ssn instance", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        instance_hostname = get_instance_hostname(instance_name)

        logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        params = "--hostname {} --keyfile {} --pip_packages 'boto3 argparse fabric jupyter awscli pymongo' --user {}".\
            format(instance_hostname, "/root/keys/" + os.environ['conf_key_name'] + ".pem", os.environ['conf_os_user'])

        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing software: pip, apt", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CONFIGURE SSN INSTANCE]')
        print('[CONFIGURE SSN INSTANCE]')
        additional_config = {"nginx_template_dir": "/root/templates/"}
        params = "--hostname {} --keyfile {} --additional_config '{}' --os_user {} --dlab_path {}". \
                 format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']), json.dumps(additional_config), os.environ['conf_os_user'], os.environ['ssn_dlab_path'])

        try:
            local("~/scripts/{}.py {}".format('configure_ssn', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed configuring ssn", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CONFIGURING DOCKER AT SSN INSTANCE]')
        print('[CONFIGURING DOCKER AT SSN INSTANCE]')
        additional_config = [{"name": "base", "tag": "latest"},
                             {"name": "edge", "tag": "latest"},
                             {"name": "jupyter", "tag": "latest"},
                             {"name": "rstudio", "tag": "latest"},
                             {"name": "zeppelin", "tag": "latest"},
                             {"name": "emr", "tag": "latest"} ]
        params = "--hostname {} --keyfile {} --additional_config '{}' --os_family {} --os_user {} --dlab_path {} --cloud_provider {}". \
                 format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']), json.dumps(additional_config), os.environ['conf_os_family'], os.environ['conf_os_user'], os.environ['ssn_dlab_path'], os.environ['conf_cloud_provider'])

        try:
            local("~/scripts/{}.py {}".format('configure_docker', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to configure docker", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        logging.info('[CONFIGURE SSN INSTANCE UI]')
        print('[CONFIGURE SSN INSTANCE UI]')
        params = "--hostname {} --keyfile {} " \
                 "--pip_packages 'pymongo pyyaml'". \
                 format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']))

        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to preconfigure ui", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            raise Exception
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
        sys.exit(1)

    try:
        params = "--hostname {} --keyfile {} --dlab_path {} --os_user {} --request_id {} --resource {} --region {} --service_base_name {} --security_groups_ids {} --vpc_id {} --subnet_id {}". \
                 format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']), os.environ['ssn_dlab_path'], os.environ['conf_os_user'], os.environ['request_id'], os.environ['resource'], os.environ['aws_region'], os.environ['conf_service_base_name'], os.environ['aws_security_groups_ids'], os.environ['aws_vpc_id'], os.environ['aws_subnet_id'])

        try:
            local("~/scripts/{}.py {}".format('configure_ui', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to upload UI", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
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
        print "Key name: " + os.environ['conf_key_name']
        print "VPC ID: " + os.environ['aws_vpc_id']
        print "Subnet ID: " + os.environ['aws_subnet_id']
        print "Security IDs: " + os.environ['aws_security_groups_ids']
        print "SSN instance shape: " + os.environ['ssn_instance_size']
        print "SSN AMI name: " + os.environ['aws_debian_ami_name']
        print "SSN bucket name: " + user_bucket_name
        print "Region: " + region
        jenkins_url = "http://{}/jenkins".format(get_instance_hostname(instance_name))
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
                   "master_keyname": os.environ['conf_key_name'],
                   "vpc_id": os.environ['aws_vpc_id'],
                   "subnet_id": os.environ['aws_subnet_id'],
                   "security_id": os.environ['aws_security_groups_ids'],
                   "instance_shape": os.environ['ssn_instance_size'],
                   "bucket_name": user_bucket_name,
                   "region": region,
                   "action": "Create SSN instance"}
            f.write(json.dumps(res))

        print 'Upload response file'
        params = "--instance_name {} --local_log_filepath {} --os_user {}".format(instance_name, local_log_filepath, os.environ['conf_os_user'])
        local("~/scripts/{}.py {}".format('upload_response_file', params))

        logging.info('[FINALIZE]')
        print('[FINALIZE]')
        params = ""
        if os.environ['conf_lifecycle_stage'] == 'prod':
            params += "--key_id {}".format(os.environ['aws_access_key'])
            local("~/scripts/{}.py {}".format('finalize', params))
    except:
        remove_ec2(tag_name, instance_name)
        remove_all_iam_resources(instance)
        remove_s3(instance)
        if pre_defined_sg:
            remove_sgroups(tag_name)
        if pre_defined_vpc:
            remove_vpc_endpoints(os.environ['aws_vpc_id'])
            remove_internet_gateways(os.environ['aws_vpc_id'], tag_name, service_base_name)
            remove_subnets(service_base_name + "-subnet")
            remove_route_tables(tag_name, True)
            remove_vpc(os.environ['aws_vpc_id'])
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
        params = "--tag_name {} --edge_sg {} --nb_sg {}". \
                 format(ssn_conf['tag_name'], ssn_conf['edge_sg'], ssn_conf['nb_sg'])
        try:
            local("~/scripts/{}.py {}".format('terminate_aws_resources', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to terminate ssn", "conf": ssn_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
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