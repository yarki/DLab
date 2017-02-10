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

import boto3
from botocore.client import Config
from fabric.api import *
import argparse
import os
import sys
import time
from fabric.api import lcd
from fabric.contrib.files import exists
from fabvenv import virtualenv
from dlab.notebook_lib import *
from dlab.actions_lib import *
from dlab.fab import *
from dlab.common_lib import *

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--emr_version', type=str, default='')
parser.add_argument('--spark_version', type=str, default='')
parser.add_argument('--hadoop_version', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--excluded_lines', type=str, default='')
parser.add_argument('--user_name', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--edge_hostname', type=str, default='')
parser.add_argument('--proxy_port', type=str, default='')
args = parser.parse_args()

emr_dir = '/opt/' + args.emr_version + '/jars/'
kernels_dir = '/home/' + args.os_user + '/.local/share/jupyter/kernels/'
spark_dir = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
yarn_dir = '/opt/' + args.emr_version + '/' + args.cluster_name + '/conf/'


def install_remote_livy(args):
    install_maven_emr()
    install_livy_dependencies_emr()
    local('sudo service zeppelin-notebook stop')
    with lcd('/opt/' + args.emr_version + '/' + args.cluster_name + '/'):
        local('sudo chown -R ' + args.os_user + ':' + args.os_user + ' /opt/' + args.emr_version + '/')
        local('git init')
        local('sudo rm -rf /opt/' + args.emr_version + '/' + args.cluster_name + '/livy/')
        local('git clone https://github.com/cloudera/livy.git')
    livy_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/livy/'
    with lcd(livy_path):
        local('mvn package -DskipTests -Dhttp.proxyHost=' +
              args.edge_hostname + ' -Dhttp.proxyPort=' + args.proxy_port + ' -Dhttps.proxyHost=' +
              args.edge_hostname + ' -Dhttps.proxyPort=' + args.proxy_port)
    local('sudo mkdir -p /var/run/livy')
    local('sudo mkdir -p ' + livy_path + '/logs')
    local('sudo chown ' + args.os_user + ':' + args.os_user + ' -R /var/run/livy')
    local('sudo chown ' + args.os_user + ':' + args.os_user + ' -R ' + livy_path)


if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        result = prepare(emr_dir, yarn_dir)
        if result == False :
            jars(args, emr_dir)
        yarn(args, yarn_dir)
        install_emr_spark(args)
        spark_defaults(args)
        configuring_notebook(args.emr_version)
        install_remote_livy(args)
        configure_zeppelin_emr_interpreter(args.emr_version, args.cluster_name, args.region, args.bucket,
                                           args.user_name, args.spark_version, spark_dir, args.os_user)
