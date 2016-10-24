#!/usr/bin/python
# =============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# =============================================================================
from dlab.aws_actions import *
import boto3
import argparse
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--tag_name', type=str)
parser.add_argument('--nb_tag_value', type=str)
args = parser.parse_args()


##############
# Run script #
##############

if __name__ == "__main__":
    print "Starting notebook"
    try:
        start_ec2(args.tag_name, args.nb_tag_value)
    except:
        sys.exit(1)
    print "The notebook instance " + args.nb_tag_value + " has been stopped successfully"

