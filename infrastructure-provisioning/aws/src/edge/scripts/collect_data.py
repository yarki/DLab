#!/usr/bin/python
import argparse
import json
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys


parser = argparse.ArgumentParser()
parser.add_argument('--service_base_name', type=str, default='')
parser.add_argument('--user_name', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
