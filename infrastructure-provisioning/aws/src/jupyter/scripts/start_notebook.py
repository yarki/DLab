#!/usr/bin/python

# ***************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ***************************************************************************

from dlab.aws_actions import *
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


