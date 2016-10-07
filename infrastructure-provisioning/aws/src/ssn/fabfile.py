#!/usr/bin/python
import json
from dlab.fab import *
from dlab.aws_meta import *
import sys


def run():
    success = True
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    logging.info('[CREATE AWS CONFIG FILE]')
    print '[CREATE AWS CONFIG FILE]'
    if success:
        if not create_aws_config_files(generate_full_config=True):
            logging.info('Unable to create configuration')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create configuration", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[DERIVING NAMES]')
    print '[DERIVING NAMES]'
    service_base_name = os.environ['conf_service_base_name']
    role_name = service_base_name + '-ssn-Role'
    role_profile_name = service_base_name + '-ssn-Profile'
    policy_name = service_base_name + '-ssn-Policy'
    user_bucket_name = (service_base_name + '-ssn-bucket').lower().replace('_', '-')
    tag_name = service_base_name + '-Tag'
    instance_name = service_base_name + '-ssn-instance'

    logging.info('[CREATE ROLES]')
    print('[CREATE ROLES]')
    params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
             (role_name, role_profile_name, policy_name, os.environ['conf_policy_arn'])
    if success:
        if not run_routine('create_role_policy', params):
            logging.info('Unable to create roles')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create roles", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[CREATE BUCKETS]')
    print('[CREATE BUCKETS]')
    params = "--bucket_name %s --infra_tag_name %s --infra_tag_value %s" % \
             (user_bucket_name, tag_name, "bucket")
    if success:
        if not run_routine('create_bucket', params):
            logging.info('Unable to create bucket')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create bucket", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[CREATE SSN INSTANCE]')
    print('[CREATE SSN INSTANCE]')
    params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
             "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
             (instance_name, os.environ['ssn_ami_id'], os.environ['ssn_instance_size'],
              os.environ['creds_key_name'], os.environ['creds_security_groups_ids'],
              os.environ['creds_subnet_id'], role_profile_name, tag_name, 'ssn')
    if success:
        if not run_routine('create_instance', params):
            logging.info('Unable to create ssn instance')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to create ssn instance", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    instance_hostname = get_instance_hostname(instance_name)

    logging.info('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    print('[INSTALLING PREREQUISITES TO SSN INSTANCE]')
    params = "--hostname %s --keyfile %s " \
             "--pip_packages 'boto3 boto argparse fabric jupyter awscli'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    if success:
        if not run_routine('install_prerequisites', params):
            logging.info('Failed installing software: pip, apt')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing software: pip, apt", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[CONFIGURE SSN INSTANCE]')
    print('[CONFIGURE SSN INSTANCE]')
    additional_config = {"nginx_template_dir": "/root/templates/",
                         "squid_template_file": "/root/templates/squid.conf",
                         "proxy_port": os.environ["ssn_proxy_port"],
                         "proxy_subnet": os.environ["ssn_proxy_subnet"]}
    params = "--hostname %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))
    if success:
        if not run_routine('configure_ssn', params):
            logging.info('Failed configuring ssn')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed configuring ssn", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[CONFIGURING DOCKER AT SSN INSTANCE]')
    print('[CONFIGURING DOCKER AT SSN INSTANCE]')
    additional_config = [{"name": "base", "tag": "latest"},
                         {"name": "jupyter", "tag": "latest"},
                         {"name": "edge", "tag": "latest"},
                         {"name": "emr", "tag": "latest"},]
    params = "--hostname %s --keyfile %s --additional_config '%s'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'], json.dumps(additional_config))
    if success:
        if not run_routine('configure_docker', params):
            logging.info('Unable to configure docker')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to configure docker", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    logging.info('[CONFIGURE SSN INSTANCE UI]')
    print('[CONFIGURE SSN INSTANCE UI]')
    params = "--hostname %s --keyfile %s " \
             "--pip_packages 'pymongo pyyaml'" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    if success:
        if not run_routine('install_prerequisites', params):
            logging.info('Unable to preconfigure ui')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to preconfigure ui", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

    params = "--hostname %s --keyfile %s" % \
             (instance_hostname, "/root/keys/%s.pem" % os.environ['creds_key_name'])
    if success:
        if not run_routine('configure_ui', params):
            logging.info('Unable to upload UI')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Unable to upload UI", "conf": os.environ.__dict__}
                print json.dumps(res)
                result.write(json.dumps(res))
            success = False
            sys.exit(1)

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
    if os.environ['ops_lifecycle_stage'] == 'prod' and success:
        params += "--key_id %s" % os.environ['creds_access_key']
        run_routine('finalize', params)

    if success:
        sys.exit(0)
    else:
        sys.exit(1)
