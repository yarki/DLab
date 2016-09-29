#!/usr/bin/python
import json
import argparse
from dlab.aws_actions import *
from dlab.aws_meta import *


parser = argparse.ArgumentParser()
parser.add_argument('--name', type=str, default='')
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--security_group_rules', type=str, default='[]')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


if __name__ == "__main__":
    rules = json.loads(args.security_group_rules)
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.name != '':
        security_group_id = get_security_group_by_name(args.name)
        if security_group_id == '':
            print "Creating security group %s for vpc %s with tag %s." % (args.name, args.vpc_id, json.dumps(tag))
            security_group_id = create_security_group(args.name, args.vpc_id, rules, tag)
        else:
            print "REQUESTED SECURITY GROUP WITH NAME %s ALREADY EXISTS" % args.name
        print "SECURITY_GROUP_ID " + security_group_id
    else:
        parser.print_help()
