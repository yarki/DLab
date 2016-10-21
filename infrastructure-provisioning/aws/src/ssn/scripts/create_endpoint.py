#!/usr/bin/python
import argparse
import json
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--region', type=str, default='us-west-2')
parser.add_argument('--infra_tag_name', type=str, default='Name')
parser.add_argument('--infra_tag_value', type=str, default='BDCC-DSA-POC-infra')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.vpc_id != '':
        try:
            print "Creating Endpoint in vpc {}, region {} with tag {}.".format(args.vpc_id, args.region, json.dumps(tag))
            endpoint = create_endpoint(vpc_id, "com.amazonaws.{}.s3".format(args.region), json.dumps(tag))
            if endpoint:
                print "ENDPOINT: " + endpoint
            else:
                print "REQUESTED ENDPOINT ALREADY EXISTS"
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