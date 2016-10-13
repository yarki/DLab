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
    index = provide_index('EMR', os.environ['conf_service_base_name'] + '-Tag')
    print 'Generating infrastructure names and tags'
    emr_conf = dict()
    emr_conf['apps'] = 'Hadoop Hive Hue Spark'
    emr_conf['service_base_name'] = os.environ['conf_service_base_name']
    emr_conf['key_name'] = os.environ['creds_key_name']
    emr_conf['subnet_cidr'] = os.environ['edge_subnet_cidr']
    emr_conf['region'] = os.environ['creds_region']
    emr_conf['release_label'] = os.environ['emr_version']
    emr_conf['instance_type'] = os.environ['emr_instance_type']
    emr_conf['instance_count'] = os.environ['emr_instance_count']
    emr_conf['notebook_ip'] = get_instance_ip_address(os.environ['notebook_name'])
    #emr_conf['notebook_user'] = os.environ['edge_user_name']
    emr_conf['role_service_name'] = os.environ['service_role']
    emr_conf['role_ec2_name'] = os.environ['ec2_role']

    emr_conf['tags'] = 'Name=' + emr_conf['service_base_name'] + '-' + os.environ['edge_user_name'] + '-' + str(index) + ', ' \
                       + emr_conf['service_base_name'] + '-Tag=' + emr_conf['service_base_name'] + '-' + os.environ['edge_user_name'] + '-' + str(index)\
                       + ', Notebook=' + os.environ['notebook_name']
    emr_conf['cluster_name'] = emr_conf['service_base_name'] + '-' + os.environ['edge_user_name'] + '-' + str(index)
    emr_conf['bucket_name'] = (emr_conf['service_base_name'] + '-' + os.environ['edge_user_name'] + '-edge-bucket').lower().replace('_', '-')

    try:
        emr_conf['emr_timeout'] = os.environ['emr_timeout']
    except:
        emr_conf['emr_timeout'] = "1200"

        # TBD
    #    emr_conf['emr_security_group_name'] = emr_conf['instance_name'] + '-SG'
    #    emr_conf['isolated_security_group_name'] = emr_conf['instance_name'] + '-isolated-SG'
    #    emr_conf['security_group_rules'] = [{"IpProtocol": "-1",
    #                                          "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
    #                                          "UserIdGroupPairs": [],
    #                                          "PrefixListIds": []}]

    print "Will create exploratory environment with edge node as access point as following: " + \
          json.dumps(emr_conf, sort_keys=True, indent=4, separators=(',', ': '))
    logging.info(json.dumps(emr_conf))

#    logging.info('[CREATE ROLES]')
#    print '[CREATE ROLES]'
#    params = "--role_name %s --role_profile_name %s --policy_name %s --policy_arn %s" % \
#             (emr_conf['role_name'], emr_conf['role_profile_name'],
#              emr_conf['policy_name'], emr_conf['policy_arn'])
#    if not run_routine('create_role_policy', params):
#        logging.info('Failed creating roles')
#        with open("/root/result.json", 'w') as result:
#            res = {"error": "Failed to creating roles", "conf": emr_conf}
#            print json.dumps(res)
#            result.write(json.dumps(res))
#        sys.exit(1)

#    logging.info('[CREATE SECURITY GROUP FOR EDGE NODE]')
#    print '[CREATE SECURITY GROUPS FOR EDGE]'
#    sg_rules_template = [
#        {
#            "IpProtocol": "-1",
#            "IpRanges": [{"CidrIp": emr_conf['subnet_cidr']}],
#            "UserIdGroupPairs": [], "PrefixListIds": []
#        },
#        {
#            "PrefixListIds": [],
#            "FromPort": 22,
#            "IpRanges": [{"CidrIp": "0.0.0.0/0"}],
#            "ToPort": 22, "IpProtocol": "tcp", "UserIdGroupPairs": []
#        }
#    ]
#    params = "--name %s --vpc_id %s --security_group_rules '%s' --infra_tag_name %s --infra_tag_value %s" % \
#             (emr_conf['edge_security_group_name'], emr_conf['vpc_id'], json.dumps(sg_rules_template),
#              emr_conf['service_base_name'], emr_conf['instance_name'])
#    if not run_routine('create_security_group', params):
#        logging.info('Failed creating security group for edge node')
#        with open("/root/result.json", 'w') as result:
#            res = {"error": "Failed creating security group for edge node", "conf": emr_conf}
#            print json.dumps(res)
#            result.write(json.dumps(res))
#        sys.exit(1)

#    logging.info('[CREATE SECURITY GROUP FOR PRIVATE SUBNET]')
#    print '[CREATE SECURITY GROUPS FOR EDGE]'
#    edge_group_id = get_security_group_by_name(emr_conf['edge_security_group_name'])
#    ingress_sg_rules_template = [{"IpProtocol": "-1", "IpRanges": [], "UserIdGroupPairs": [{"GroupId": edge_group_id}], "PrefixListIds": []}]
#    egress_sg_rules_template = [{"IpProtocol": "-1", "IpRanges": [], "UserIdGroupPairs": [{"GroupId": edge_group_id}], "PrefixListIds": []}]
#    params = "--name %s --vpc_id %s --security_group_rules '%s' --egress '%s' --infra_tag_name %s --infra_tag_value %s" % \
#             (emr_conf['isolated_security_group_name'], emr_conf['vpc_id'],
#              json.dumps(ingress_sg_rules_template), json.dumps(egress_sg_rules_template),
#              emr_conf['service_base_name'], emr_conf['instance_name'])
#    if not run_routine('create_security_group', params):
#        logging.info('Failed creating security group for private subnet')
#        with open("/root/result.json", 'w') as result:
#            res = {"error": "Failed creating security group for private subnet", "conf": emr_conf}
#            print json.dumps(res)
#            result.write(json.dumps(res))
#        sys.exit(1)

    with hide('stderr', 'running', 'warnings'):
        local("echo Waitning for changes to propagate; sleep 10")

    try:
        logging.info('[CREATE EMR CLUSTER]')
        print '[CREATE EMR CLUSTER]'
        params = "--name {} --applications '{}' --instance_type {} --instance_count {} --ssh_key {} --release_label {} --emr_timeout {} " \
                 "--subnet {} --service_role {} --ec2_role {} --nbs_ip {} --nbs_user {} --s3_bucket {} --tags '{}'".format(
            emr_conf['cluster_name'], emr_conf['apps'], emr_conf['instance_type'], emr_conf['instance_count'], emr_conf['key_name'], emr_conf['release_label'], emr_conf['emr_timeout'],
            emr_conf['subnet_cidr'], emr_conf['role_service_name'], emr_conf['role_ec2_name'], emr_conf['notebook_ip'], 'ubuntu', emr_conf['bucket_name'], emr_conf['tags'])
        if not run_routine('create_cluster', params):
            logging.info('Failed creating EMR Cluster')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to create EMR Cluster", "conf": emr_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)

        cluster_name = emr_conf['cluster_name']
        keyfile_name = "/root/keys/%s.pem" % emr_conf['key_name']
    except:
        sys.exit(1)

    try:
        logging.info('[INSTALLING KERNELS INTO SPECIFIED NOTEBOOK]')
        print '[INSTALLING KERNELS INTO SPECIFIED NOTEBOOK]'
        params = "--bucket {} --cluster_name {} --emr_version {} --keyfile {} --notebook_ip {}".format(emr_conf['bucket_name'], emr_conf['cluster_name'], emr_conf['release_label'], keyfile_name, emr_conf['notebook_ip'])
        #params = "--hostname %s --keyfile %s " % (instance_hostname, keyfile_name)
        if not run_routine('install_emr_kernels', params):
            logging.info('Failed installing EMR kernels')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed installing EMR kernels", "conf": emr_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)

    with open("/root/result.json", 'w') as result:
        res = {"hostname": cluster_name,
               "key_name": emr_conf['key_name'],
               "user_own_bucket_name": emr_conf['bucket_name']}
        print json.dumps(res)
        result.write(json.dumps(res))

    sys.exit(0)


def terminate():
    local_log_filename = "%s.log" % os.environ['request_id']
    local_log_filepath = "/response/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    # generating variables dictionary
    create_aws_config_files()
    print 'Generating infrastructure names and tags'
    emr_conf = dict()
    emr_conf['service_base_name'] = os.environ['conf_service_base_name']
    emr_conf['emr_name'] = os.environ['emr_cluster_name']
    emr_conf['notebook_name'] = os.environ['notebook_instance_name']
    emr_conf['bucket_name'] = (emr_conf['service_base_name'] + '-' + os.environ['edge_user_name'] + '-edge-bucket').lower().replace('_', '-')
    emr_conf['ssh_user'] = os.environ['notebook_ssh_user']
    emr_conf['key_path'] = os.environ['creds_key_dir'] + os.environ['creds_key_name'] + '.pem'
    emr_conf['tag_name'] = emr_conf['service_base_name'] + '-Tag'

    try:
        logging.info('[TERMINATE EMR CLUSTER]')
        print '[TERMINATE EMR CLUSTER]'
        params = "--emr_name %s --bucket_name %s --key_path %s --ssh_user %s --tag_name %s --nb_tag_value %s" % \
                 (emr_conf['emr_name'], emr_conf['bucket_name'], emr_conf['key_path'], emr_conf['ssh_user'],
                  emr_conf['tag_name'], emr_conf['notebook_name'])
        if not run_routine('terminate_emr', params):
            logging.info('Failed to terminate EMR cluster')
            with open("/root/result.json", 'w') as result:
                res = {"error": "Failed to terminate EMR cluster", "conf": emr_conf}
                print json.dumps(res)
                result.write(json.dumps(res))
            sys.exit(1)
    except:
        sys.exit(1)
