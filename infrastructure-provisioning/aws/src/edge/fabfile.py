#!/usr/bin/python
import json
from dlab.fab import *
from dlab.aws_meta import *
import sys


def run():
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    edge_conf = dict()
    # Base config
    edge_conf['service_base_name'] = os.environ['conf_service_base_name']
    edge_conf['key_name'] = os.environ['creds_key_name']
    edge_conf['policy_arn'] = os.environ['conf_policy_arn']
    edge_conf['public_subnet_id'] = os.environ['creds_subnet_id']
    edge_conf['private_subnet_cidr'] = os.environ['edge_subnet_cidr']
    edge_conf['vpc_id'] = os.environ['edge_vpc_id']
    edge_conf['region'] = os.environ['edge_region']
    edge_conf['ami_id'] = os.environ['edge_ami_id']
    edge_conf['instance_size'] = os.environ['edge_instance_size']

    # Edge config
    edge_conf['instance_name'] = edge_conf['service_base_name'] + "-" + os.environ['edge_user_name'] + '-edge'
    edge_conf['bucket_name'] = (edge_conf['instance_name'] + '-bucket').lower().replace('_', '-')
    edge_conf['role_name'] = edge_conf['instance_name'] + '-Role'
    edge_conf['role_profile_name'] = edge_conf['instance_name'] + '-Profile'
    edge_conf['policy_name'] = edge_conf['instance_name'] + '-Policy'
    edge_conf['edge_security_group_name'] = edge_conf['instance_name'] + '-SG'
    edge_conf['security_group_rules'] = [{"IpProtocol": "-1",
                                          "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                                          "UserIdGroupPairs": [],
                                          "PrefixListIds": []}]

    # Notebook \ EMR config
    edge_conf['notebook_role_name'] = edge_conf['service_base_name'] + "-" + os.environ['edge_user_name'] + '-nb-Role'
    edge_conf['notebook_policy_name'] = edge_conf['service_base_name'] + "-" + os.environ['edge_user_name'] + '-nb-Policy'
    edge_conf['notebook_role_profile_name'] = edge_conf['service_base_name'] + "-" + os.environ['edge_user_name'] + '-nb-Profile'
    edge_conf['notebook_policy_arn'] = os.environ['edge_notebook_policy_arn']
    edge_conf['notebook_security_group_name'] = edge_conf['service_base_name'] + "-" + os.environ['edge_user_name'] + '-nb-SG'
    edge_conf['notebook_security_group_rules'] = [{"IpProtocol": "-1",
                                                   "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                                                   "UserIdGroupPairs": [],
                                                   "PrefixListIds": []}]

    print "Will create exploratory environment with edge node as access point as following: " + \
          json.dumps(edge_conf, sort_keys=True, indent=4, separators=(',', ': '))
    logging.info(json.dumps(edge_conf))

    try:
        logging.info('[CREATE SUBNET]')
        print '[CREATE SUBNET]'
        params = "--vpc_id '%s' --subnet '%s' --region %s --infra_tag_name %s --infra_tag_value %s" % \
                 (edge_conf['vpc_id'], edge_conf['private_subnet_cidr'], edge_conf['region'],
                  edge_conf['instance_name'], edge_conf['instance_name'])
        if not run_routine('create_subnet', params):
            logging.info('Failed creating subnet')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to create subnet", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE EDGE ROLES]')
        print '[CREATE EDGE ROLES]'
        params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
                 (edge_conf['role_name'], edge_conf['role_profile_name'],
                  edge_conf['policy_name'], edge_conf['policy_arn'])
        if not run_routine('create_role_policy', params):
            logging.info('Failed creating roles')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to creating roles", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE BACKEND (NOTEBOOK) ROLES]')
        print '[CREATE BACKEND (NOTEBOOK) ROLES]'
        params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
                 (edge_conf['notebook_role_name'], edge_conf['notebook_role_profile_name'],
                  edge_conf['notebook_policy_name'], edge_conf['notebook_policy_arn'])
        if not run_routine('create_role_policy', params):
            logging.info('Failed creating roles')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to creating roles", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE SECURITY GROUP FOR EDGE NODE]')
        print '[CREATE SECURITY GROUPS FOR EDGE]'
        sg_rules_template = [
            {
                "IpProtocol": "-1",
                "IpRanges": [{"CidrIp": edge_conf['private_subnet_cidr']}],
                "UserIdGroupPairs": [], "PrefixListIds": []
            },
            {
                "PrefixListIds": [],
                "FromPort": 22,
                "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
                "ToPort": 22, "IpProtocol": "tcp", "UserIdGroupPairs": []
            }
        ]
        params = "--name %s --vpc_id %s --security_group_rules '%s' --infra_tag_name %s --infra_tag_value %s" % \
                 (edge_conf['edge_security_group_name'], edge_conf['vpc_id'], json.dumps(sg_rules_template),
                  edge_conf['service_base_name'], edge_conf['instance_name'])
        if not run_routine('create_security_group', params):
            logging.info('Failed creating security group for edge node')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed creating security group for edge node", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE SECURITY GROUP FOR PRIVATE SUBNET]')
        print '[CREATE SECURITY GROUP FOR PRIVATE SUBNET]'
        edge_group_id = get_security_group_by_name(edge_conf['edge_security_group_name'])
        ingress_sg_rules_template = [
            {"IpProtocol": "-1", "IpRanges": [], "UserIdGroupPairs": [{"GroupId": edge_group_id}], "PrefixListIds": []},
            {"IpProtocol": "-1", "IpRanges": [], "UserIdGroupPairs": [{"GroupId": os.environ['creds_security_groups_ids']}], "PrefixListIds": []}
        ]
        egress_sg_rules_template = [
            {"IpProtocol": "-1", "IpRanges": [], "UserIdGroupPairs": [{"GroupId": edge_group_id}], "PrefixListIds": []}
        ]
        params = "--name %s --vpc_id %s --security_group_rules '%s' --egress '%s' --infra_tag_name %s --infra_tag_value %s" % \
                 (edge_conf['notebook_security_group_name'], edge_conf['vpc_id'],
                  json.dumps(ingress_sg_rules_template), json.dumps(egress_sg_rules_template),
                  edge_conf['service_base_name'], edge_conf['instance_name'])
        if not run_routine('create_security_group', params):
            logging.info('Failed creating security group for private subnet')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed creating security group for private subnet", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)

        with hide('stderr', 'running', 'warnings'):
            local("echo Waitning for changes to propagate; sleep 10")
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE BUCKETS]')
        print('[CREATE BUCKETS]')
        params = "--bucket_name %s --infra_tag_name %s --infra_tag_value %s" % \
                 (edge_conf['bucket_name'], edge_conf['service_base_name'], edge_conf['instance_name'] + "bucket")
        if not run_routine('create_bucket', params):
            logging.info('Failed creating bucket')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to create bucket", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        logging.info('[CREATE EDGE INSTANCE]')
        print '[CREATE EDGE INSTANCE]'
        params = "--node_name %s --ami_id %s --instance_type %s --key_name %s --security_group_ids %s " \
                 "--subnet_id %s --iam_profile %s --infra_tag_name %s --infra_tag_value %s" % \
                 (edge_conf['instance_name'], edge_conf['ami_id'], edge_conf['instance_size'], edge_conf['key_name'],
                  edge_group_id, edge_conf['public_subnet_id'], edge_conf['role_profile_name'],
                  edge_conf['service_base_name'], edge_conf['instance_name'])
        if not run_routine('create_instance', params):
            logging.info('Failed creating instance')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to create instance", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)

        instance_hostname = get_instance_hostname(edge_conf['instance_name'])
        ip_address = get_instance_ip_address(edge_conf['instance_name'])
        keyfile_name = "/root/keys/%s.pem" % edge_conf['key_name']
    except:
        sys.exit(1)

    try:
        print '[INSTALLING PREREQUISITES]'
        logging.info('[INSTALLING PREREQUISITES]')
        params = "--hostname %s --keyfile %s " % (instance_hostname, keyfile_name)
        if not run_routine('install_prerequisites', params):
            logging.info('Failed installing apps: apt & pip')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing apps: apt & pip", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        print '[INSTALLING HTTP PROXY]'
        logging.info('[INSTALLING HTTP PROXY]')
        additional_config = {"exploratory_subnet": edge_conf['private_subnet_cidr'],
                             "template_file": "/root/templates/squid.conf"}
        params = "--hostname %s --keyfile %s --additional_config '%s'" % \
                 (instance_hostname, keyfile_name, json.dumps(additional_config))
        if not run_routine('configure_http_proxy', params):
            logging.info('Failed installing http proxy')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing http proxy", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        print '[INSTALLING SOCKS PROXY]'
        logging.info('[INSTALLING SOCKS PROXY]')
        additional_config = {"exploratory_subnet": edge_conf['private_subnet_cidr'],
                             "template_file": "/root/templates/danted.conf"}
        params = "--hostname %s --keyfile %s --additional_config '%s'" % \
                 (instance_hostname, keyfile_name, json.dumps(additional_config))
        if not run_routine('configure_socks_proxy', params):
            logging.info('Failed installing socks proxy')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing socks proxy", "conf": edge_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    try:
        with open("/root/result.json", 'w') as result:
            res = {"hostname": instance_hostname,
                   "ip": ip_address,
                   "key_name": edge_conf['key_name'],
                   "user_own_bicket_name": edge_conf['bucket_name'],
                   "tunnel_port": "22",
                   "socks_port": "1080",
                   "notebook_sg": edge_conf['notebook_security_group_name'],
                   "notebook_profile": edge_conf['notebook_role_profile_name'],
                   "edge_sg": edge_conf['edge_security_group_name'],
                   "notebook_subnet": edge_conf['private_subnet_cidr'],
                   "full_edge_conf": edge_conf}
            print json.dumps(res)
            result.write(json.dumps(res))
    except:
        sys.exit(1)

    sys.exit(0)
