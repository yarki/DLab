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
from dlab.aws_actions import *
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
args = parser.parse_args()

emr_dir = '/opt/' + args.emr_version + '/jars/'
kernels_dir = '/home/' + args.os.user + '/.local/share/jupyter/kernels/'
spark_dir = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
yarn_dir = '/opt/' + args.emr_version + '/' + args.cluster_name + '/conf/'


def r_kernel(args):
    spark_path = '/opt/{}/{}/spark/'.format(args.emr_version, args.cluster_name)
    local('mkdir -p {}/r_{}/'.format(kernels_dir, args.cluster_name))
    kernel_path = "{}/r_{}/kernel.json".format(kernels_dir, args.cluster_name)
    template_file = "/tmp/r_emr_template.json"
    r_version = local("R --version | awk '/version / {print $3}'", capture = True)

    with open(template_file, 'r') as f:
        text = f.read()
    text = text.replace('CLUSTERNAME', args.cluster_name)
    text = text.replace('SPARK_PATH', spark_path)
    text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
    text = text.replace('R_VER', 'R-{}'.format(str(r_version)))
    text = text.replace('EMR', args.emr_version)
    if 'emr-4.' in args.emr_version:
        text = text.replace('YRN_CLI_TYPE', 'yarn-client')
        text = text.replace('SPRK_ACTION', 'init()')
    else:
        text = text.replace('YRN_CLI_TYPE', 'yarn')
        text = text.replace('SPRK_ACTION', 'session(master = \\\"yarn\\\")')
    with open(kernel_path, 'w') as f:
        f.write(text)


def pyspark_kernel(args):
    spark_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
    local('mkdir -p ' + kernels_dir + 'pyspark_' + args.cluster_name + '/')
    kernel_path = kernels_dir + "pyspark_" + args.cluster_name + "/kernel.json"
    template_file = "/tmp/pyspark_emr_template.json"
    with open(template_file, 'r') as f:
        text = f.read()
    text = text.replace('CLUSTER', args.cluster_name)
    text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
    text = text.replace('SPARK_PATH', spark_path)
    text = text.replace('PY_VER', '2.7')
    text = text.replace('PY_FULL', '2.7')
    text = text.replace('PYTHON_PATH', '/usr/bin/python2.7')
    text = text.replace('EMR', args.emr_version)
    with open(kernel_path, 'w') as f:
        f.write(text)
    local('touch /tmp/kernel_var.json')
    local(
        "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
    local('sudo mv /tmp/kernel_var.json ' + kernel_path)
    s3_client = boto3.client('s3', config=Config(signature_version='s3v4'), region_name=args.region)
    s3_client.download_file(args.bucket, args.user_name + '/' + args.cluster_name + '/python_version', '/tmp/python_version')
    with file('/tmp/python_version') as f:
        python_version = f.read()
    # python_version = python_version[0:3]
    if python_version != '\n':
        installing_python(args)
        local('mkdir -p ' + kernels_dir + 'py3spark_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "py3spark_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/pyspark_emr_template.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('PY_VER', python_version[0:3])
        text = text.replace('PY_FULL', python_version[0:5])
        text = text.replace('PYTHON_PATH', '/opt/python/python' + python_version[:5] + '/bin/python' + python_version[:3])
        text = text.replace('EMR', args.emr_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)


def toree_kernel(args):
    spark_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
    scala_version = local("dpkg -l scala | grep scala | awk '{print $3}'", capture=True)
    if args.emr_version == 'emr-4.3.0' or args.emr_version == 'emr-4.6.0' or args.emr_version == 'emr-4.8.0':
        local('mkdir -p ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "toree_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/toree_emr_template.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('EMR', args.emr_version)
        text = text.replace('SC_VER', scala_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)
    else:
        local('mkdir -p ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        local('tar zxvf /tmp/toree_kernel.tar.gz -C ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "toree_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/toree_emr_templatev2.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('EMR', args.emr_version)
        text = text.replace('SC_VER', scala_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)
        run_sh_path = kernels_dir + "toree_" + args.cluster_name + "/bin/run.sh"
        template_sh_file = '/tmp/run_template.sh'
        with open(template_sh_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        with open(run_sh_path, 'w') as f:
            f.write(text)


def configure_zeppelin_emr_interpreter(args):
    try:
        spark_libs = "/opt/" + args.emr_version + "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-core*.jar /opt/" + args.emr_version + "/jars/usr/lib/hadoop/hadoop-aws*.jar /opt/" + args.emr_version + "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-s3-*.jar /opt/" + args.emr_version + "/jars/usr/lib/hadoop-lzo/lib/hadoop-lzo-*.jar"
        local('echo \"Configuring emr path for Zeppelin\"')
        local('sed -i \"/^# export SPARK_HOME/c\export SPARK_HOME\" /opt/zeppelin/conf/zeppelin-env.sh')
        local('sed -i \"s/^export SPARK_HOME.*/export SPARK_HOME=\/opt\/' + args.emr_version + '\/' +  args.cluster_name + '\/spark/\" /opt/zeppelin/conf/zeppelin-env.sh')
        local('sed -i \"s/^export HADOOP_CONF_DIR.*/export HADOOP_CONF_DIR=\/opt\/' + args.emr_version + '\/' +  args.cluster_name + '\/conf/\" /opt/' + args.emr_version + '/' +  args.cluster_name + '/spark/conf/spark-env.sh')
        local('echo \"spark.jars $(ls ' + spark_libs + ' | tr \'\\n\' \',\')\" >> /opt/' + args.emr_version + '/' +  args.cluster_name + '/spark/conf/spark-defaults.conf')
        local('echo \"spark.executorEnv.PYTHONPATH pyspark.zip:py4j-src.zip\" >> /opt/' + args.emr_version + '/' +  args.cluster_name + '/spark/conf/spark-defaults.conf')
        local('sed -i \'/spark.yarn.dist.files/s/$/,file:\/opt\/' + args.emr_version + '\/' +  args.cluster_name + '\/spark\/python\/lib\/py4j-src.zip,file:\/opt\/' + args.emr_version + '\/' +  args.cluster_name + '\/spark\/python\/lib\/pyspark.zip/\' /opt/' + args.emr_version + '/' +  args.cluster_name + '/spark/conf/spark-defaults.conf')
        local('service zeppelin-notebook restart')
        local('sleep 5')
    except:
            sys.exit(1)
    if not os.path.exists('/home/' + args.os_user + '/.ensure_dir/emr_interpreter_ensured'):
        try:
            local('echo \"Configuring emr spark interpreter for Zeppelin\"')
            template_file = "/tmp/emr_spark_interpreter.json"
            fr = open(template_file, 'r+')
            text = fr.read()
            text = text.replace('CLUSTERNAME', args.cluster_name)
            text = text.replace('PYTHON_PATH', '/usr/bin/python2.7')
            text = text.replace('EMRVERSION', args.emr_version)
            fw = open(template_file, 'w')
            fw.write(text)
            fw.close()
            local("curl --noproxy localhost -H 'Content-Type: application/json' -X POST -d @/tmp/emr_spark_interpreter.json http://localhost:8080/api/interpreter/setting")
            local('touch /home/' + args.os_user + '/.ensure_dir/emr_interpreter_ensured')
        except:
            sys.exit(1)


if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        result = prepare(emr_dir, yarn_dir)
        if result == False :
            jars(args, emr_dir)
        yarn(args, yarn_dir)
        install_emr_spark(args)
        pyspark_kernel(args)
        toree_kernel(args)
        spark_defaults(args)
        r_kernel(args)
        configuring_notebook(args)
        configure_zeppelin_emr_interpreter(args)
