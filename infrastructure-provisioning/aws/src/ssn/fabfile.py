#!/usr/bin/python
import json
from dlab.fab import *
from dlab.aws_meta import *


def run():
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    env.warn_only = True

    logging.info('[CREATE AWS CONFIG FILE]')
    print '[CREATE AWS CONFIG FILE]'
    create_aws_config_files(generate_full_config=True)

    logging.info('[DERIVING NAMES]')
    print '[DERIVING NAMES]'
    service_base_name = os.environ['conf_service_base_name']
    role_name = service_base_name + '-Role'
    role_profile_name = role_name + '-Profile'
    policy_name = role_name + '-Policy'
    user_bucket_name = (service_base_name + '-bucket').lower().replace('_', '-')
    tag_name = service_base_name + '-Tag'
    instance_name = service_base_name + '-ssn-instance'

    logging.info('[CREATE ROLES]')
    print('[CREATE ROLES]')
    params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
             (role_name, role_profile_name, policy_name, os.environ['conf_policy_arn'])
    run_routine('create_role_policy', params)

    logging.info('[CREATE BUCKETS]')
    print('[CREATE BUCKETS]')
    params = "--bucket_name %s --infra_tag_name %s --infra_tag_value %s" % \
             (user_bucket_name, tag_name, "bucket")
    run_routine('create_bucket', params)

    logging.info('[CREATE SSN INSTANCE]')
    print('[CREATE SSN INSTANCE]')
    params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
             "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
             (instance_name, os.environ['ssn_ami_id'], os.environ['ssn_instance_type'],
              os.environ['creds_key_name'], os.environ['creds_security_groups_ids'],
              os.environ['creds_subnet_id'], role_profile_name, tag_name, 'ssn')
    run_routine('create_instance', params)

    instance_hostname = get_instance_hostname(instance_name)

    logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    params = "--hostname %s --keyfile %s " \
             "--pip_packages 'boto3 boto argparse fabric jupyter awscli'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    run_routine('install_prerequisites', params)

    logging.info('[CONFIGURE SSN INSTANCE]')
    print('[CONFIGURE SSN INSTANCE]')
    additional_config = {"nginx_template_dir": "/root/templates/",
                         "squid_template_file": "/root/templates/squid.conf",
                         "proxy_port": os.environ["ssn_proxy_port"],
                         "proxy_subnet": os.environ["ssn_proxy_subnet"]}
    params = "--hostname %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))
    run_routine('configure_ssn', params)

    logging.info('[CONFIGURING DOCKER AT SSN INSTANCE]')
    print('[CONFIGURING DOCKER AT SSN INSTANCE]')
    additional_config = [{"name": "base", "tag": "latest"},
                         {"name": "jupyter", "tag": "latest"},
                         {"name": "edge", "tag": "latest"}]
    params = "--hostname %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))
    run_routine('configure_docker', params)

    logging.info('[CONFIGURE SSN INSTANCE UI]')
    print('[CONFIGURE SSN INSTANCE UI]')
    params = "--hostname %s --keyfile %s " \
             "--pip_packages 'pymongo pyyaml'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    run_routine('install_prerequisites', params)

    params = "--hostname %s --keyfile %s" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    run_routine('configure_ui', params)

    logging.info('[REPORT TO BUCKET]')
    print('[REPORT TO BUCKET]')
    params = "--bucket_name %s --local_file %s --destination_file %s" % \
             (user_bucket_name, local_log_filepath, local_log_filename)
    run_routine('put_to_bucket', params)

    jenkins_url = "http://%s/jenkins" % get_instance_hostname(instance_name)
    print "Jenkins URL: " + jenkins_url
    try:
        with open('jenkins_crids.txt') as f:
            print f.read()
    except:
        print "Jenkins is either configured already or have issues in configuration routine."

    with open("/root/result.json", 'w') as f:
        res = {"hostname": get_instance_hostname(instance_name), "master_keyname": os.environ['creds_key_name']}
        f.write(json.dumps(res))

    logging.info('[FINALIZE]')
    print('[FINALIZE]')
    params = ""
    if os.environ['ops_lifecycle_stage'] == 'prod':
        params += "--key_id %s" % os.environ['creds_access_key']
        run_routine('finalize', params)
