#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
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
# ============================================================================
import boto3
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str, default='dsa-test-bucket')
parser.add_argument('--local_file', type=str, default='ami-7172b611')
parser.add_argument('--destination_file', type=str, default='t2.small')
args = parser.parse_args()


def kick_the_bucket(bucket_name, local_file, destination_file):
    s3 = boto3.client('s3')
    with open(local_file, 'rb') as data:
        s3.upload_fileobj(data, bucket_name, destination_file)


if __name__ == "__main__":
    kick_the_bucket(args.bucket_name, args.local_file, args.destination_file)
