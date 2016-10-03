#!/usr/bin/python
import json
import argparse
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--name', type=str, default='')
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--security_group_rules', type=str, default='[]')
parser.add_argument('--egress', type=str, default='[]')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


def create_security_group(security_group_name, vpc_id, security_group_rules, egress, tag):
    ec2 = boto3.resource('ec2')
    group = ec2.create_security_group(GroupName=security_group_name, Description='security_group_name', VpcId=vpc_id)
    time.sleep(10)
    group.create_tags(Tags=[tag])
    for rule in security_group_rules:
        group.authorize_ingress(IpPermissions=[rule])
    for rule in egress:
        group.authorize_egress(IpPermissions=[rule])
    return group.id

if __name__ == "__main__":
    success = False
    try:
        rules = json.loads(args.security_group_rules)
        egress = json.loads(args.egress)
    except:
        sys.exit(1)
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.name != '':
        try:
            security_group_id = get_security_group_by_name(args.name)
            if security_group_id == '':
                print "Creating security group %s for vpc %s with tag %s." % (args.name, args.vpc_id, json.dumps(tag))
                security_group_id = create_security_group(args.name, args.vpc_id, rules, egress, tag)
            else:
                print "REQUESTED SECURITY GROUP WITH NAME %s ALREADY EXISTS" % args.name
            print "SECURITY_GROUP_ID " + security_group_id
            success = True
        except:
            success = False
    else:
        parser.print_help()
        sys.exit(2)

    if success:
        sys.exit(0)
    else:
        sys.exit(1)
