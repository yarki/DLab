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

from dlab.actions_lib import *
from dlab.common_lib import *
from dlab.notebook_lib import *
from dlab.fab import *
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import sys
import os

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--rstudio_pass', type=str, default='')
args = parser.parse_args()

spark_version = os.environ['notebook_spark_version']
hadoop_version = os.environ['notebook_hadoop_version']
spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-" + spark_version + "-bin-hadoop" + hadoop_version + ".tgz"
local_spark_path = '/opt/spark/'
s3_jars_dir = '/opt/jars/'
templates_dir = '/root/templates/'
files_dir = '/root/files/'


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = args.os_user + '@' + args.hostname

    print "Configuring notebook server."
    try:
        if not exists('/home/' + args.os_user + '/.ensure_dir'):
            sudo('mkdir /home/' + args.os_user + '/.ensure_dir')
    except:
        sys.exit(1)

    print "Mount additional volume"
    prepare_disk(args.os_user)

    print "Install python libraries"
    ensure_libraries_py(args.os_user)

    print "Install RStudio"
    install_rstudio(args.os_user, local_spark_path, args.rstudio_pass)

    print "Install local Spark"
    ensure_local_spark(args.os_user, spark_link, spark_version, hadoop_version, local_spark_path )

    print "Install local S3 kernels"
    ensure_s3_kernel(args.os_user, s3_jars_dir, files_dir, args.region, templates_dir)
