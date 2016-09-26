#!/usr/bin/python
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
