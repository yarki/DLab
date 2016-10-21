#!/usr/bin/python
import argparse
import json
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--subnet', type=str, default='')
parser.add_argument('--region', type=str, default='us-west-2')
parser.add_argument('--infra_tag_name', type=str, default='Name')
parser.add_argument('--infra_tag_value', type=str, default='BDCC-DSA-POC-infra')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.subnet != '':
        try:
            subnet_id = get_subnet_by_cidr(args.subnet)
            if subnet_id == '':
                print "Creating subnet %s in vpc %s, region %s with tag %s." % \
                      (args.subnet, args.vpc_id, args.region, json.dumps(tag))
                subnet_id = create_subnet(args.vpc_id, args.subnet, tag)
                print "Associating route_table with subnet"
                route_table = get_route_table_by_tag(args.infra_tag_name, args.infra_tag_value)
                route_table.associate_with_subnet(SubnetId=subnet_id)
            else:
                print "REQUESTED SUBNET ALREADY EXISTS"
            print "SUBNET_ID " + subnet_id
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
