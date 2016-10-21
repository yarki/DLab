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

import boto3
from fabric.api import *
import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--emr_version', type=str, default='emr-4.8.0')
args = parser.parse_args()

emr_dir = '/opt/jars/'
kernels_dir = '/home/ubuntu/.local/share/jupyter/kernels/'
yarn_dir = '/srv/hadoopconf/'


def prepare():
    local('rm -rf /srv/*')
    local('mkdir -p ' + yarn_dir)
    local('mkdir -p ' + emr_dir)
    result = os.path.exists(emr_dir + args.emr_version + "/aws")
    return result


def jars(args):
    print "Downloading jars..."
    s3client = boto3.client('s3')
    s3resource = boto3.resource('s3')
    get_files(s3client, s3resource, 'jars/', args.bucket, '/opt/')


def yarn(args):
    print "Downloading yarn configuration..."
    s3client = boto3.client('s3')
    s3resource = boto3.resource('s3')
    get_files(s3client, s3resource, 'config/{}'.format(args.cluster_name), args.bucket, yarn_dir)


def pyspark_kernel(args):
    local('mkdir -p ' + kernels_dir + 'pyspark_' + args.cluster_name + '/')
    kernel_path = kernels_dir + "pyspark_" + args.cluster_name + "/kernel.json"
    template_file = "/tmp/pyspark_emr_template.json"
    with open(kernel_path, 'w') as out:
        with open(template_file) as tpl:
            for line in tpl:
                out.write(line.replace('CLUSTER', args.cluster_name))
    local('mkdir -p ' + kernels_dir + 'py3spark_' + args.cluster_name + '/')
    kernel_path = kernels_dir + "py3spark_" + args.cluster_name + "/kernel.json"
    template_file = "/tmp/py3spark_emr_template.json"
    with open(kernel_path, 'w') as out:
        with open(template_file) as tpl:
            for line in tpl:
                out.write(line.replace('CLUSTER', args.cluster_name))


def toree_kernel(args):
    local('mkdir -p ' + kernels_dir + 'toree_' + args.cluster_name + '/')
    kernel_path = kernels_dir + "toree_" + args.cluster_name + "/kernel.json"
    template_file = "/tmp/toree_emr_template.json"
    with open(kernel_path, 'w') as out:
        with open(template_file) as tpl:
            for line in tpl:
                out.write(line.replace('CLUSTER', args.cluster_name))


def get_files(s3client, s3resource, dist, bucket, local):
    s3list = s3client.get_paginator('list_objects')
    for result in s3list.paginate(Bucket=bucket, Delimiter='/', Prefix=dist):
        if result.get('CommonPrefixes') is not None:
            for subdir in result.get('CommonPrefixes'):
                get_files(s3client, s3resource, subdir.get('Prefix'), bucket, local)
        if result.get('Contents') is not None:
            for file in result.get('Contents'):
                if not os.path.exists(os.path.dirname(local + os.sep + file.get('Key'))):
                     os.makedirs(os.path.dirname(local + os.sep + file.get('Key')))
                s3resource.meta.client.download_file(bucket, file.get('Key'), local + os.sep + file.get('Key'))


def spark_defaults():
    #local('cp /tmp/spark-defaults_template.conf /opt/spark/conf/spark-defaults.conf')
    spark_def_path = '/opt/spark/conf/spark-defaults.conf'
    template_file = "/tmp/spark-defaults_template.conf"
    with open(spark_def_path, 'w') as out:
        with open(template_file) as tpl:
            for line in tpl:
                out.write(line.replace('EMRVERSION', args.emr_version))

if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        result = prepare()
        if result == False :
            jars(args)
        yarn(args)
        pyspark_kernel(args)
        toree_kernel(args)
        spark_defaults()