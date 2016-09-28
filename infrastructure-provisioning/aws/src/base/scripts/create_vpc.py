#!/usr/bin/python
import sys
import argparse
import json
from dlab.aws_actions import *
from dlab.aws_meta import *


parser = argparse.ArgumentParser()
parser.add_argument('--vpc', type=str, default='')
parser.add_argument('--region', type=str, default='us-west-2')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


if __name__ == "__main__":
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.vpc != '':
        vpc_id = get_vpc_by_cidr(args.vpc)
        if vpc_id != '':
            print "Creating vpc %s in region %s with tag %s." % (args.vpc, args.region, json.dumps(tag))
            vpc_id = create_vpc(args.vpc, tag)
        else:
            print "REQUESTED VPC ALREADY EXISTS"
        print "VPC_ID " + vpc_id
        args.vpc_id = vpc_id
    else:
        parser.print_help()
        sys.exit(2)