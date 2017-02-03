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
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import sys
import os
from dlab.notebook_lib import *
from dlab.fab import *

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
parser.add_argument('--os_user', type=str, default='')
parser.add_argument('--spark_version', type=str, default='')
parser.add_argument('--hadoop_version', type=str, default='')
parser.add_argument('--edge_hostname', type=str, default='')
parser.add_argument('--proxy_port', type=str, default='')
args = parser.parse_args()


spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-" + args.spark_version + "-bin-hadoop" + args.hadoop_version + ".tgz"
zeppelin_link = "http://www-us.apache.org/dist/zeppelin/zeppelin-0.6.2/zeppelin-0.6.2-bin-netinst.tgz"
zeppelin_version = "0.6.2"
zeppelin_interpreters = "md,python,livy"
python3_version = "3.4"
local_spark_path = '/opt/spark/'
templates_dir = '/root/templates/'
files_dir = '/root/files/'
s3_jars_dir = '/opt/jars/'


def configure_notebook_server(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/zeppelin_ensured'):
        try:
            sudo('wget ' + zeppelin_link + ' -O /tmp/zeppelin-' + zeppelin_version + '-bin-netinst.tgz')
            sudo('tar -zxvf /tmp/zeppelin-' + zeppelin_version + '-bin-netinst.tgz -C /opt/')
            sudo('ln -s /opt/zeppelin-' + zeppelin_version + '-bin-netinst /opt/zeppelin')
            sudo('cp /opt/zeppelin/conf/zeppelin-env.sh.template /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('cp /opt/zeppelin/conf/zeppelin-site.xml.template /opt/zeppelin/conf/zeppelin-site.xml')
            sudo('sed -i \"/# export ZEPPELIN_PID_DIR/c\export ZEPPELIN_PID_DIR=/var/run/zeppelin\" /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('sed -i \"/# export ZEPPELIN_IDENT_STRING/c\export ZEPPELIN_IDENT_STRING=notebook\" /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('sed -i \"/# export SPARK_HOME/c\export SPARK_HOME=\/opt\/spark/\" /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('mkdir /var/log/zeppelin')
            sudo('mkdir /var/run/zeppelin')
            sudo('ln -s /var/log/zeppelin /opt/zeppelin-' + zeppelin_version + '-bin-netinst/logs')
            sudo('chown ' + os_user + ':' + os_user + ' -R /var/log/zeppelin')
            sudo('ln -s /var/run/zeppelin /opt/zeppelin-' + zeppelin_version + '-bin-netinst/run')
            sudo('chown ' + os_user + ':' + os_user + ' -R /var/run/zeppelin')
            sudo('/opt/zeppelin/bin/install-interpreter.sh --name ' + zeppelin_interpreters + ' --proxy-url $http_proxy')
            sudo('chown ' + os_user + ':' + os_user + ' -R /opt/zeppelin-' + zeppelin_version + '-bin-netinst')
        except:
            sys.exit(1)
        try:
            put(templates_dir + 'zeppelin-notebook.service', '/tmp/zeppelin-notebook.service')
            sudo("sed -i 's|OS_USR|" + os_user + "|' /tmp/zeppelin-notebook.service")
            sudo("chmod 644 /tmp/zeppelin-notebook.service")
            sudo('cp /tmp/zeppelin-notebook.service /etc/systemd/system/zeppelin-notebook.service')
            sudo("systemctl daemon-reload")
            sudo("systemctl enable zeppelin-notebook")
            sudo("systemctl start zeppelin-notebook")
            sudo('echo \"d /var/run/zeppelin 0755 ' + os_user + '\" > /usr/lib/tmpfiles.d/zeppelin.conf')
            sudo('touch /home/' + os_user + '/.ensure_dir/zeppelin_ensured')
        except:
            sys.exit(1)


def configure_local_kernels(args):
    port_number_found = False
    default_port = 8998
    livy_port = ''
    put(templates_dir + 'interpreter.json', '/tmp/interpreter.json')
    sudo('sed -i "s|AWSREGION|' + args.region + '|g" /tmp/interpreter.json')
    sudo('sed -i "s|OS_USER|' + args.os_user + '|g" /tmp/interpreter.json')
    sudo('sed -i "s|SP_VER|' + args.spark_version + '|g" /tmp/interpreter.json')
    while not port_number_found:
        port_free = sudo('nc -z localhost ' + str(default_port) + '; echo $?')
        if port_free == '1':
            livy_port = default_port
            port_number_found = True
        else:
            default_port += 1
    sudo('sed -i "s|LIVYPORT|' + livy_port + '|g" /tmp/interpreter.json')
    sudo('cp /tmp/interpreter.json /opt/zeppelin/conf/interpreter.json')
    sudo('echo "livy.server.port = ' + str(livy_port) + '" >> /opt/livy/conf/livy.conf')
    sudo('''echo "SPARK_HOME='/opt/spark/'" >> /opt/livy/conf/livy-env.sh''')
    sudo('sed -i "s/^/#/g" /opt/livy/conf/spark-blacklist.conf')
    sudo("systemctl start livy-server")


def install_local_livy(args):
    install_maven()
    install_livy_dependencies()
    with cd('/opt/'):
        sudo('git init')
        sudo('git clone https://github.com/cloudera/livy.git')
    with cd('/opt/livy/'):
        sudo('mvn package -DskipTests -Dhttp.proxyHost=' + args.edge_hostname + ' -Dhttp.proxyPort=' +
             args.proxy_port + ' -Dhttps.proxyHost=' + args.edge_hostname +
             ' -Dhttps.proxyPort=' + args.proxy_port)
    sudo('mkdir -p /var/run/livy')
    sudo('mkdir -p /opt/livy/logs')
    sudo('chown ' + args.os_user + ':' + args.os_user + ' -R /var/run/livy')
    sudo('chown ' + args.os_user + ':' + args.os_user + ' -R /opt/livy/')
    put(templates_dir + 'livy-server.service', '/tmp/livy-server.service')
    sudo("sed -i 's|OS_USR|" + args.os_user + "|' /tmp/livy-server.service")
    sudo("chmod 644 /tmp/livy-server.service")
    sudo('cp /tmp/livy-server.service /etc/systemd/system/livy-server.service')
    sudo("systemctl daemon-reload")
    sudo("systemctl enable livy-server")


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = args.os_user + '@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Configuring notebook server."
    try:
        if not exists('/home/' + args.os_user + '/.ensure_dir'):
            sudo('mkdir /home/' + args.os_user + '/.ensure_dir')
    except:
        sys.exit(1)

    print "Mount additional volume"
    prepare_disk(args.os_user)

    print "Install Java"
    ensure_jre_jdk(args.os_user)

    print "Install local Spark"
    ensure_local_spark(args.os_user, spark_link, args.spark_version, args.hadoop_version, local_spark_path)

    print "Install local S3 kernels"
    ensure_s3_kernel(args.os_user, s3_jars_dir, files_dir, args.region, templates_dir)

    print "Install Zeppelin"
    configure_notebook_server(args.os_user)

    print "Install python3 kernels"
    ensure_python3_kernel_zeppelin(python3_version, args.os_user)

    print "Installing Livy for local kernels"
    install_local_livy(args)

    print "Configuring local kernels"
    configure_local_kernels(args)
