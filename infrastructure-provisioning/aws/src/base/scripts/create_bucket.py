#!/usr/bin/python
import boto3
import argparse
import json
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--bucket_name', type=str, default='dsa-test-bucket')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


def get_bucket_by_name(bucket_name):
    s3 = boto3.resource('s3')
    for bucket in s3.buckets.all():
        if bucket.name == bucket_name:
            return bucket.name
    return ''


def create_s3_bucket(bucket_name, tag):
    s3 = boto3.resource('s3')
    bucket = s3.create_bucket(Bucket=bucket_name)
    tagging = bucket.Tagging()
    tagging.put(Tagging={'TagSet': [tag]})
    tagging.reload()
    return bucket.name


if __name__ == "__main__":
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.bucket_name != '':
        bucket = get_bucket_by_name(args.bucket_name)
        if bucket == '':
            print "Creating bucket %s with tag %s." % (args.bucket_name, json.dumps(tag))
            bucket = create_s3_bucket(args.bucket_name, tag)
        else:
            print "REQUESTED BUCKET ALREADY EXISTS"
        print "BUCKET_NAME " + bucket
    else:
        parser.print_help()
