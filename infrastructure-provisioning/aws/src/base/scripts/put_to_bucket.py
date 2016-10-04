#!/usr/bin/python
import argparse
from dlab.aws_actions import *
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str, default='dsa-test-bucket')
parser.add_argument('--local_file', type=str, default='ami-7172b611')
parser.add_argument('--destination_file', type=str, default='t2.small')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    if put_to_bucket(args.bucket_name, args.local_file, args.destination_file):
        sys.exit(0)
    else:
        sys.exit(1)
