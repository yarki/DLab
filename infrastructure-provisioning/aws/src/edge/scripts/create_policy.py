#!/usr/bin/python
# ******************************************************************************************************
#
# Copyright (c) 2016 EPAM Systems Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including # without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject # to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. # IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH # # THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
# ****************************************************************************************************/

import argparse
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys
import boto3, botocore

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str, default='')
parser.add_argument('--service_base_name', type=str, default='')
parser.add_argument('--iam_user', type=str, default='')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    if args.bucket_name:
        try:
            handler = open('/root/templates/s3_policy.json', 'r')
            policy = handler.read()
            policy.replace('BUCKET_NAME', args.bucket_name)
        except OSError:
            print "Failed to open policy template"
            success = False

        try:
            iam = boto3.client('iam')
            response = iam.create_policy(PolicyName='{}-{}-strict_to_S3-Policy'.format(args.service_base_name, args.iam_user), PolicyDocument=policy)
            try:
                iam.get_user(args.iam_user)
                iam.attach_user_policy(UserName=args.iam_user, PolicyArn=response.get('Policy').get('Arn'))
                print 'POLICY_NAME "{0}-{1}-strict_to_S3-Policy" has been attached to user "{1}"'.format(args.service_base_name, args.iam_user)
                success = True
            except botocore.exceptions.ClientError as e:
                print e.response['Error']['Message']
                success = False
            # success = True # This should be removed when goes PROD
        except:
            success = False
    else:
        parser.print_help()
        sys.exit(2)

    if success:
        sys.exit(0)
    else:
        sys.exit(1)