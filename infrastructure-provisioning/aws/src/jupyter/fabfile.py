#!/usr/bin/python
import json
import argparse
import sys
from dlab.fab import *
from dlab.aws_meta import *

parser = argparse.ArgumentParser()
parser.add_argument('--notebook_name', type=str, default='')
parser.add_argument('--subnet_cidr', type=str, default='')
args = parser.parse_args()


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


def run(nb_config):
    logging.info('[CREATE SUBNET]')
    print '[CREATE SUBNET]'
    params = "--vpc_id '%s' --subnet '%s' --region %s --infra_tag_name %s --infra_tag_value %s" % \
             (nb_config['vpc_id'], nb_config['subnet'], nb_config['region'],
              os.environ['conf_service_base_name'], nb_config['instance_name'])
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
              os.environ['conf_service_base_name'], nb_config['instance_name'])
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
              os.environ['conf_service_base_name'], nb_config['instance_name'])
    run_routine('create_instance', params)

    instance_hostname = get_instance_hostname(nb_config['instance_name'])
    ssn_instance_name = os.environ['conf_service_base_name'] + '-ssn-instance'
    ssn_instance_hostname = get_instance_hostname(ssn_instance_name)
    keyfile_name = "/root/keys/%s.pem" % os.environ['creds_key_name']

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
                         "backend_hostname": get_instance_hostname(nb_config['instance_name']),
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

    notebook_instance_name = os.environ['conf_service_base_name'] + '-notebook-' + args.notebook_name
    expected_ami_name = os.environ['conf_service_base_name'] + '-notebook-image'

    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    env.warn_only = True
    with hide('stderr', 'running'):
        create_aws_config_files(generate_full_config=False)

    print 'Searching preconfigured images'
    ami_id = get_ami_id_by_name(expected_ami_name)
    notebook_config = dict()
    notebook_config['vpc_id'] = os.environ['notebook_vpc_id']
    notebook_config['region'] = os.environ['notebook_region']
    notebook_config['instance_name'] = notebook_instance_name
    notebook_config['role_name'] = notebook_instance_name + "-Role"
    notebook_config['role_profile_name'] = notebook_instance_name + "-Role-Profile"
    notebook_config['policy_name'] = notebook_instance_name + "-Role-Policy"
    notebook_config['policy_arn'] = os.environ['notebook_policy_arn']
    notebook_config['security_group_name'] = notebook_instance_name + "-SG"
    notebook_config['security_group_rules'] = os.environ['notebook_security_group_rules']
    notebook_config['subnet'] = args.subnet_cidr
    notebook_config['key_name'] = os.environ['creds_key_name']
    if ami_id != '':
        print 'Preconfigured image found. Using: ' + ami_id
        notebook_config['ami_id'] = ami_id
    else:
        print 'No preconfigured image found. Using default one: ' + os.environ['notebook_ami_id']
        notebook_config['ami_id'] = os.environ['notebook_ami_id']

    run(notebook_config)

    if ami_id == '' and os.path.isfile("/tmp/" + notebook_instance_name + "passwd.file"):
        print "Looks like it's first time we configure notebook server. Creating image."
        image_id = create_image_from_instance(instance_name=notebook_instance_name,
                                              image_name=expected_ami_name)
        if image_id != '':
            print "Image succesfully created. It's ID is " + image_id

    with open("/tmp/" + notebook_instance_name + "passwd.file") as f:
        ip_address = get_instance_ip_address(notebook_instance_name)
        dns_name = get_instance_hostname(notebook_instance_name)
        ssn_dns_name = get_instance_hostname(os.environ['conf_service_base_name'] + '-ssn-instance')
        access_password = f.read()
        print "Notebook access url: http://%s/%s" % (ssn_dns_name, notebook_instance_name)
        print "Notebook access password: " + access_password
        print 'SSH access (from Edge node, via IP address): ssh -i keyfile.pem ubuntu@' + ip_address
        print 'SSH access (from Edge node, via FQDN): ssh -i keyfile.pem ubuntu@' + dns_name

        with open("/root/result.json", 'w') as result:
            res = {"hostname": dns_name,
                   "ip": ip_address,
                   "access_url": "http://%s/%s" %
                                 (ssn_dns_name,
                                  notebook_instance_name),
                   "access_password": access_password,
                   "master_keyname": os.environ['creds_key_name']}
            result.write(json.dumps(res))

