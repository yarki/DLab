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
import logging
import json
import sys
from dlab.fab import *
from dlab.aws_meta import *
from dlab.aws_actions import *
import os
import uuid


# Main function for provisioning notebook server
def run():
    # enable debug level for boto3
    logging.getLogger('botocore').setLevel(logging.DEBUG)
    logging.getLogger('boto3').setLevel(logging.DEBUG)

    instance_class = 'notebook'
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['notebook_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    notebook_config = dict()
    notebook_config['uuid'] = str(uuid.uuid4())[:5]
    try:
        notebook_config['exploratory_name'] = os.environ['exploratory_name']
    except:
        notebook_config['exploratory_name'] = ''
    notebook_config['service_base_name'] = os.environ['conf_service_base_name']
    notebook_config['instance_type'] = os.environ['notebook_instance_type']
    notebook_config['key_name'] = os.environ['creds_key_name']
    notebook_config['user_keyname'] = os.environ['notebook_user_name']
    notebook_config['instance_name'] = os.environ['conf_service_base_name'] + "-" + os.environ['notebook_user_name'] + "-nb-" + notebook_config['exploratory_name'] + "-" + notebook_config['uuid']
    notebook_config['expected_ami_name'] = os.environ['conf_service_base_name'] + "-" + os.environ[
        'notebook_user_name'] + '-notebook-image'
    notebook_config['role_profile_name'] = os.environ['conf_service_base_name'].lower().replace('-', '_') + "-" + os.environ[
        'notebook_user_name'] + "-nb-Profile"
    notebook_config['security_group_name'] = os.environ['conf_service_base_name'] + "-" + os.environ[
        'notebook_user_name'] + "-nb-SG"
    notebook_config['tag_name'] = notebook_config['service_base_name'] + '-Tag'

    print 'Searching preconfigured images'
    ami_id = get_ami_id_by_name(notebook_config['expected_ami_name'], 'available')
    if ami_id != '':
        print 'Preconfigured image found. Using: ' + ami_id
        notebook_config['ami_id'] = ami_id
    else:
        print 'No preconfigured image found. Using default one: ' + get_ami_id(os.environ['notebook_ami_name'])
        notebook_config['ami_id'] = get_ami_id(os.environ['notebook_ami_name'])

    tag = {"Key": notebook_config['tag_name'], "Value": "{}-{}-subnet".format(notebook_config['service_base_name'], os.environ['notebook_user_name'])}
    notebook_config['subnet_cidr'] = get_subnet_by_tag(tag)

    # launching instance for notebook server
    try:
        logging.info('[CREATE INSTANCE]')
        print '[CREATE ZEPPELIN NOTEBOOK INSTANCE]'
        params = "--node_name {} --ami_id {} --instance_type {} --key_name {} --security_group_ids {} --subnet_id {} --iam_profile {} --infra_tag_name {} --infra_tag_value {} --instance_class {} --instance_disk_size {}"\
            .format(notebook_config['instance_name'], notebook_config['ami_id'], notebook_config['instance_type'],
                    notebook_config['key_name'], get_security_group_by_name(notebook_config['security_group_name']),
                    get_subnet_by_cidr(notebook_config['subnet_cidr']), notebook_config['role_profile_name'],
                    notebook_config['tag_name'], notebook_config['instance_name'], instance_class, os.environ['notebook_disk_size'])
        local("~/scripts/{}.py {}".format('create_instance', params))
        with open("/root/result.json", 'w') as result:
            res = {"error": "Failed to create instance", "conf": notebook_config}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        sys.exit(1)

    # generating variables regarding EDGE proxy on Notebook instance
    instance_hostname = get_instance_hostname(notebook_config['instance_name'])
    edge_instance_name = os.environ['conf_service_base_name'] + "-" + os.environ['notebook_user_name'] + '-edge'
    edge_instance_hostname = get_instance_hostname(edge_instance_name)
    keyfile_name = "/root/keys/{}.pem".format(os.environ['creds_key_name'])

    # configuring proxy on Notebook instance
    try:
        logging.info('[CONFIGURE PROXY ON ZEPPELIN INSTANCE]')
        print '[CONFIGURE PROXY ON ZEPPELIN INSTANCE]'
        additional_config = {"proxy_host": edge_instance_hostname, "proxy_port": "3128"}
        params = "--hostname {} --instance_name {} --keyfile {} --additional_config '{}'"\
            .format(instance_hostname, notebook_config['instance_name'], keyfile_name, json.dumps(additional_config))
        try:
            local("~/scripts/{}.py {}".format('configure_proxy', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to configure proxy", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(notebook_config['tag_name'], notebook_config['instance_name'])
        sys.exit(1)

    # updating repositories & installing python packages
    try:
        logging.info('[INSTALLING PREREQUISITES TO ZEPPELIN NOTEBOOK INSTANCE]')
        print('[INSTALLING PREREQUISITES TO ZEPPELIN NOTEBOOK INSTANCE]')
        params = "--hostname {} --keyfile {} --user {}"\
            .format(instance_hostname, keyfile_name, os.environ['general_os_user'])
        try:
            local("~/scripts/{}.py {}".format('install_prerequisites', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing apps: apt & pip", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(notebook_config['tag_name'], notebook_config['instance_name'])
        sys.exit(1)

    # installing and configuring zeppelin and all dependencies
    try:
        logging.info('[CONFIGURE ZEPPELIN NOTEBOOK INSTANCE]')
        print '[CONFIGURE ZEPPELIN NOTEBOOK INSTANCE]'
        additional_config = {"frontend_hostname": edge_instance_hostname,
                             "backend_hostname": get_instance_hostname(notebook_config['instance_name']),
                             "backend_port": "8080",
                             "nginx_template_dir": "/root/templates/"}
        params = "--hostname {} --instance_name {} --keyfile {} --region {} --additional_config '{}' --os_user {}"\
            .format(instance_hostname, notebook_config['instance_name'], keyfile_name, os.environ['creds_region'],
                    json.dumps(additional_config), os.environ['general_os_user'])
        try:
            local("~/scripts/{}.py {}".format('configure_zeppelin_node', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to configure zeppelin", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
    except:
        remove_ec2(notebook_config['tag_name'], notebook_config['instance_name'])
        sys.exit(1)

    # installing python2 and python3 libs
    try:
        logging.info('[CONFIGURE ZEPPELIN ADDITIONS]')
        print '[CONFIGURE ZEPPELIN ADDITIONS]'
        params = "--hostname {} --keyfile {} --os_user {}"\
            .format(instance_hostname, keyfile_name, os.environ['general_os_user'])
        try:
            local("~/scripts/{}.py {}".format('install_zeppelin_additions', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to install python libs", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        remove_ec2(notebook_config['tag_name'], notebook_config['instance_name'])
        sys.exit(1)

    try:
        print '[INSTALLING USERs KEY]'
        logging.info('[INSTALLING USERs KEY]')
        additional_config = {"user_keyname": notebook_config['user_keyname'],
                             "user_keydir": "/root/keys/"}
        params = "--hostname {} --keyfile {} --additional_config '{}'".format(
            instance_hostname, keyfile_name, json.dumps(additional_config))
        try:
            local("~/scripts/{}.py {}".format('install_user_key', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing users key", "conf": params}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        sys.exit(1)

    # checking the need for image creation
    ami_id = get_ami_id_by_name(notebook_config['expected_ami_name'])
    if ami_id == '':
        print "Looks like it's first time we configure notebook server. Creating image."
        image_id = create_image_from_instance(instance_name=notebook_config['instance_name'],
                                              image_name=notebook_config['expected_ami_name'])
        if image_id != '':
            print "Image was successfully created. It's ID is " + image_id

    # generating output information
    ip_address = get_instance_ip_address(notebook_config['instance_name']).get('Private')
    dns_name = get_instance_hostname(notebook_config['instance_name'])
    zeppelin_ip_url = "http://" + ip_address + ":8080/" + notebook_config['instance_name'] + "/"
    zeppelin_dns_url = "http://" + dns_name + ":8080/" + notebook_config['instance_name'] + "/"
    print '[SUMMARY]'
    logging.info('[SUMMARY]')
    print "Instance name: " + notebook_config['instance_name']
    print "Private DNS: " + dns_name
    print "Private IP: " + ip_address
    print "Instance type: " + notebook_config['instance_type']
    print "Key name: " + notebook_config['key_name']
    print "User key name: " + notebook_config['user_keyname']
    print "AMI name: " + notebook_config['expected_ami_name']
    print "Profile name: " + notebook_config['role_profile_name']
    print "SG name: " + notebook_config['security_group_name']
    print "Zeppelin URL: " + zeppelin_ip_url
    print "Zeppelin URL: " + zeppelin_dns_url
    print 'SSH access (from Edge node, via IP address): ssh -i ' + notebook_config[
        'key_name'] + '.pem ubuntu@' + ip_address
    print 'SSH access (from Edge node, via FQDN): ssh -i ' + notebook_config['key_name'] + '.pem ubuntu@' + dns_name

    with open("/root/result.json", 'w') as result:
        res = {"hostname": dns_name,
               "ip": ip_address,
               "master_keyname": os.environ['creds_key_name'],
               "notebook_name": notebook_config['instance_name'],
               "Action": "Create new notebook server",
               "exploratory_url": zeppelin_ip_url}
        result.write(json.dumps(res))


# Main function for terminating exploratory environment
def terminate():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['notebook_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    notebook_config = dict()
    notebook_config['service_base_name'] = os.environ['conf_service_base_name']
    notebook_config['notebook_name'] = os.environ['notebook_instance_name']
    notebook_config['bucket_name'] = (notebook_config['service_base_name'] + '-ssn-bucket').lower().replace('_', '-')
    notebook_config['tag_name'] = notebook_config['service_base_name'] + '-Tag'

    try:
        logging.info('[TERMINATE NOTEBOOK]')
        print '[TERMINATE NOTEBOOK]'
        params = "--bucket_name {} --tag_name {} --nb_tag_value {}"\
            .format(notebook_config['bucket_name'], notebook_config['tag_name'], notebook_config['notebook_name'])
        try:
            local("~/scripts/{}.py {}".format('terminate_notebook', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to terminate notebook", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"notebook_name": notebook_config['notebook_name'],
                   "Tag_name": notebook_config['tag_name'],
                   "user_own_bucket_name": notebook_config['bucket_name'],
                   "Action": "Terminate notebook server"}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        print "Failed writing results."
        sys.exit(0)


# Main function for stopping notebook server
def stop():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['notebook_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    notebook_config = dict()
    notebook_config['service_base_name'] = os.environ['conf_service_base_name']
    notebook_config['notebook_name'] = os.environ['notebook_instance_name']
    notebook_config['bucket_name'] = (notebook_config['service_base_name'] + '-ssn-bucket').lower().replace('_', '-')
    notebook_config['tag_name'] = notebook_config['service_base_name'] + '-Tag'
    notebook_config['ssh_user'] = os.environ['notebook_ssh_user']
    notebook_config['key_path'] = os.environ['creds_key_dir'] + '/' + os.environ['creds_key_name'] + '.pem'

    try:
        logging.info('[STOP NOTEBOOK]')
        print '[STOP NOTEBOOK]'
        params = "--bucket_name {} --tag_name {} --nb_tag_value {} --ssh_user {} --key_path {}"\
            .format(notebook_config['bucket_name'], notebook_config['tag_name'], notebook_config['notebook_name'],
                    notebook_config['ssh_user'], notebook_config['key_path'])
        try:
            local("~/scripts/{}.py {}".format('stop_notebook', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to stop notebook", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"notebook_name": notebook_config['notebook_name'],
                   "Tag_name": notebook_config['tag_name'],
                   "user_own_bucket_name": notebook_config['bucket_name'],
                   "Action": "Stop notebook server"}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        print "Failed writing results."
        sys.exit(0)


# Main function for starting notebook server
def start():
    local_log_filename = "{}_{}_{}.log".format(os.environ['resource'], os.environ['notebook_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    notebook_config = dict()
    notebook_config['service_base_name'] = os.environ['conf_service_base_name']
    notebook_config['notebook_name'] = os.environ['notebook_instance_name']
    notebook_config['tag_name'] = notebook_config['service_base_name'] + '-Tag'

    try:
        logging.info('[START NOTEBOOK]')
        print '[START NOTEBOOK]'
        params = "--tag_name {} --nb_tag_value {}"\
            .format(notebook_config['tag_name'], notebook_config['notebook_name'])
        try:
            local("~/scripts/{}.py {}".format('start_notebook', params))
        except:
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to start notebook", "conf": notebook_config}
                print json.dumps(res)
                result.write(json.dumps(res))
                raise Exception
    except:
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"notebook_name": notebook_config['notebook_name'],
                   "Tag_name": notebook_config['tag_name'],
                   "Action": "Start up notebook server"}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        print "Failed writing results."
        sys.exit(0)