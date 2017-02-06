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


def configure_zeppelin_emr_interpreter(args):
    try:
        port_number_found = False
        default_port = 8998
        livy_port = ''
        livy_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/livy/'
        spark_libs = "/opt/" + args.emr_version + "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-core*.jar /opt/" + \
                     args.emr_version + "/jars/usr/lib/hadoop/hadoop-aws*.jar /opt/" + args.emr_version + \
                     "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-s3-*.jar /opt/" + args.emr_version + \
                     "/jars/usr/lib/hadoop-lzo/lib/hadoop-lzo-*.jar"
        local('echo \"Configuring emr path for Zeppelin\"')
        local('sed -i \"s/^export SPARK_HOME.*/export SPARK_HOME=\/opt\/' + args.emr_version + '\/' +
              args.cluster_name + '\/spark/\" /opt/zeppelin/conf/zeppelin-env.sh')
        local('sed -i \"s/^export HADOOP_CONF_DIR.*/export HADOOP_CONF_DIR=\/opt\/' + args.emr_version + '\/' +
              args.cluster_name + '\/conf/\" /opt/' + args.emr_version + '/' + args.cluster_name +
              '/spark/conf/spark-env.sh')
        local('echo \"spark.jars $(ls ' + spark_libs + ' | tr \'\\n\' \',\')\" >> /opt/' + args.emr_version + '/' +
              args.cluster_name + '/spark/conf/spark-defaults.conf')
        local('echo \"spark.executorEnv.PYTHONPATH pyspark.zip:py4j-src.zip\" >> /opt/' + args.emr_version + '/' +
              args.cluster_name + '/spark/conf/spark-defaults.conf')
        local('sed -i \'/spark.yarn.dist.files/s/$/,file:\/opt\/' + args.emr_version + '\/' + args.cluster_name +
              '\/spark\/python\/lib\/py4j-src.zip,file:\/opt\/' + args.emr_version + '\/' + args.cluster_name +
              '\/spark\/python\/lib\/pyspark.zip/\' /opt/' + args.emr_version + '/' +  args.cluster_name +
              '/spark/conf/spark-defaults.conf')
        local('service zeppelin-notebook restart')
        local('sleep 5')
        local('echo \"Configuring emr spark interpreter for Zeppelin\"')
        installing_python(args.region, args.bucket, args.user_name, args.cluster_name)
        s3_client = boto3.client('s3', config=Config(signature_version='s3v4'), region_name=args.region)
        s3_client.download_file(args.bucket, args.user_name + '/' + args.cluster_name + '/python_version',
                                '/tmp/python_version')
        with file('/tmp/python_version') as f:
            python_version = f.read()
        template_file = "/tmp/emr_spark_interpreter.json"
        p_versions = ["2.7", python_version]
        for p_version in p_versions:
            fr = open(template_file, 'r+')
            text = fr.read()
            text = text.replace('CLUSTER_NAME', args.cluster_name)
            text = text.replace('PYTHON_FULL_VERSION', p_version)
            text = text.replace('SPARK_VERSION', args.spark_version)
            text = text.replace('CLUSTER_NAME', args.cluster_name)
            text = text.replace('SPARK_HOME', spark_dir)
            text = text.replace('AWS_REGION', args.region)
            while not port_number_found:
                port_free = sudo('nc -z localhost ' + str(default_port) + '; echo $?')
                if port_free == '1':
                    livy_port = default_port
                    port_number_found = True
                else:
                    default_port += 1
            text = text.replace('LIVY_PORT', str(livy_port))
            tmp_file = "/tmp/emr_spark_py" + p_version + "_interpreter.json"
            fw = open(tmp_file, 'w')
            fw.write(text)
            fw.close()
            for _ in range(5):
                try:
                    local("curl --noproxy localhost -H 'Content-Type: application/json' -X POST -d " +
                          "@/tmp/emr_spark_py" + p_version +
                          "_interpreter.json http://localhost:8080/api/interpreter/setting")
                    break
                except:
                    local('sleep 5')
                    pass
        sudo('echo "livy.server.port = ' + str(livy_port) + '" >> ' + livy_path + 'conf/livy.conf')
        sudo('''echo "SPARK_HOME=' ''' + spark_dir + ''' '" >> ''' + livy_path + '''conf/livy-env.sh''')
        sudo('sed -i "s/^/#/g" ' + livy_path + 'conf/spark-blacklist.conf')
        sudo(livy_path + 'bin/livy-server start')
        local('touch /home/' + args.os_user + '/.ensure_dir/emr_' + args.cluster_name + '_interpreter_ensured')
    except:
            sys.exit(1)


def install_remote_livy(args):
    install_maven()
    install_livy_dependencies()
    with cd('/opt/' + args.emr_version + '/' + args.cluster_name + '/'):
        sudo('git init')
        sudo('git clone https://github.com/cloudera/livy.git')
    livy_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/livy/'
    with cd(livy_path):
        sudo('mvn package -DskipTests -Dhttp.proxyHost=' + args.edge_hostname + ' -Dhttp.proxyPort=' +
             args.proxy_port + ' -Dhttps.proxyHost=' + args.edge_hostname +
             ' -Dhttps.proxyPort=' + args.proxy_port)
    sudo('mkdir -p /var/run/livy')
    sudo('mkdir -p ' + livy_path + '/logs')
    sudo('chown ' + args.os_user + ':' + args.os_user + ' -R /var/run/livy')
    sudo('chown ' + args.os_user + ':' + args.os_user + ' -R ' + livy_path)


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
        configuring_notebook(args)
        install_remote_livy(args)
        configure_zeppelin_emr_interpreter(args)
