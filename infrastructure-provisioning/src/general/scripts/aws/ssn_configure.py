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
from dlab.actions_lib import *
import sys, os
from fabric.api import *
from dlab.ssn_lib import *
import traceback

if __name__ == "__main__":
    local_log_filename = "{}_{}.log".format(os.environ['conf_resource'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    instance = 'ssn'

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
        ssn_ami_name = os.environ['aws_' + os.environ['conf_os_family'] + '_ami_name']
        ssn_ami_id = get_ami_id(ssn_ami_name)
        policy_path = '/root/files/ssn_policy.json'
        vpc_cidr = '172.31.0.0/16'
        sg_name = instance_name + '-SG'
        pre_defined_vpc = False
        pre_defined_sg = False
        try:
            os.environ['aws_vpc_id']
        except KeyError:
            tag = {"Key": tag_name, "Value": "{}-subnet".format(service_base_name)}
            os.environ['aws_vpc_id'] = get_vpc_by_tag(tag_name, service_base_name)
            os.environ['aws_subnet_id'] = get_subnet_by_tag(tag, True)
            pre_defined_vpc = True
        try:
            os.environ['aws_security_groups_ids']
        except KeyError:
            os.environ['aws_security_groups_ids'] = get_security_group_by_name(sg_name)
            pre_defined_sg = True
    except:
        sys.exit(1)

    try:
        instance_hostname = get_instance_hostname(instance_name)

        logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
        params = "--hostname {} --keyfile {} --pip_packages 'boto3 argparse fabric awscli pymongo pyyaml' --user {}". \
            format(instance_hostname, "/root/keys/" + os.environ['conf_key_name'] + ".pem", os.environ['conf_os_user'])

        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Failed installing software: pip, packages. Exception: " + str(err))
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
        additional_config = {"nginx_template_dir": "/root/templates/", "service_base_name": service_base_name, "security_group_id": os.environ['aws_security_groups_ids'], "vpc_id": os.environ['aws_vpc_id'], "subnet_id": os.environ['aws_subnet_id'], "admin_key": os.environ['conf_key_name']}
        #additional_config = {"nginx_template_dir": "/root/templates/", "service_base_name": service_base_name}
        params = "--hostname {} --keyfile {} --additional_config '{}' --os_user {} --dlab_path {}". \
            format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']),
                   json.dumps(additional_config), os.environ['conf_os_user'], os.environ['ssn_dlab_path'])

        try:
            local("~/scripts/{}.py {}".format('configure_ssn_node', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Failed configuring ssn. Exception: " + str(err))
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
                             {"name": "tensor", "tag": "latest"},
                             {"name": "emr", "tag": "latest"}]
        params = "--hostname {} --keyfile {} --additional_config '{}' --os_family {} --os_user {} --dlab_path {} --cloud_provider {}". \
            format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']),
                   json.dumps(additional_config), os.environ['conf_os_family'], os.environ['conf_os_user'],
                   os.environ['ssn_dlab_path'], os.environ['conf_cloud_provider'])

        try:
            local("~/scripts/{}.py {}".format('configure_docker', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Unable to configure docker. Exception: " + str(err))
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
        params = "--hostname {} --keyfile {} --dlab_path {} --os_user {} --os_family {} --request_id {} --resource {} --region {} --service_base_name {} --security_groups_ids {} --vpc_id {} --subnet_id {}". \
            format(instance_hostname, "/root/keys/{}.pem".format(os.environ['conf_key_name']), os.environ['ssn_dlab_path'],
                   os.environ['conf_os_user'], os.environ['conf_os_family'], os.environ['request_id'], os.environ['conf_resource'], os.environ['aws_region'],
                   os.environ['conf_service_base_name'], os.environ['aws_security_groups_ids'], os.environ['aws_vpc_id'],
                   os.environ['aws_subnet_id'])

        try:
            local("~/scripts/{}.py {}".format('configure_ui', params))
        except:
            traceback.print_exc()
            raise Exception
    except Exception as err:
        append_result("Unable to configure UI. Exception: " + str(err))
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
        print "SSN instance shape: " + os.environ['aws_ssn_instance_size']
        print "SSN AMI name: " + ssn_ami_name
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
                   "instance_shape": os.environ['aws_ssn_instance_size'],
                   "bucket_name": user_bucket_name,
                   "region": region,
                   "action": "Create SSN instance"}
            f.write(json.dumps(res))

        print 'Upload response file'
        params = "--instance_name {} --local_log_filepath {} --os_user {}".format(instance_name, local_log_filepath,
                                                                                  os.environ['conf_os_user'])
        local("~/scripts/{}.py {}".format('upload_response_file', params))

        logging.info('[FINALIZE]')
        print('[FINALIZE]')
        params = ""
        if os.environ['conf_lifecycle_stage'] == 'prod':
            params += "--key_id {}".format(os.environ['aws_access_key'])
            local("~/scripts/{}.py {}".format('ssn_finalize', params))
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