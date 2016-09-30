#!/usr/bin/python
import argparse
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--role_name', type=str, default='dsa-test-role')
parser.add_argument('--role_profile_name', type=str, default='dsa-test-role-profile')
parser.add_argument('--policy_name', type=str, default='dsa-test-policy')
parser.add_argument('--policy_arn', type=str, default='"arn:aws:iam::aws:policy/AmazonS3FullAccess", '
                                                      '"arn:aws:iam::aws:policy/AmazonEC2FullAccess", '
                                                      '"arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"')
parser.add_argument('--policy_file_name', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    if args.role_name != '':
        try:
            role_name = get_role_by_name(args.role_name)
            if role_name == '':
                print "Creating role %s, profile name %s" % (args.role_name, args.role_profile_name)
                create_iam_role(args.role_name, args.role_profile_name)
            else:
                print "ROLE AND ROLE PROFILE ARE ALREADY CREATED"
            print "ROLE %s created. IAM group %s created" % (args.role_name, args.role_profile_name)

            print "ATTACHING POLICIES TO ROLE"
            if args.policy_file_name != '':
                create_attach_policy(args.policy_name, args.role_name, args.policy_file_name)
            else:
                policy_arn_bits = eval(args.policy_arn)
                for bit in policy_arn_bits:
                    attach_policy(bit, args.role_name)
            print "POLICY %s created " % args.policy_name
            sys.exit(0)
        except:
            sys.exit(1)
    else:
        parser.print_help()
        sys.exit(2)
