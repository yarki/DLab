#!/usr/bin/python

# *****************************************************************************
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
# ******************************************************************************

from fabric.api import *
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
args = parser.parse_args()

local('aws s3 cp --recursive s3://dlab-auto-tests-bucket/ s3://' + args.bucket + '/')
local('aws s3 cp s3://dlab-auto-tests-bucket/carriers.csv s3://' + args.bucket + '/')
local('aws s3 cp s3://dlab-auto-tests-bucket/airports.csv s3://' + args.bucket + '/')
local('aws s3 cp s3://dlab-auto-tests-bucket/2008.csv.bz2 s3://' + args.bucket + '/')
