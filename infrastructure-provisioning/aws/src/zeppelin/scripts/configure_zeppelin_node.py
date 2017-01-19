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

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()

zeppelin_link = "http://www-us.apache.org/dist/zeppelin/zeppelin-0.6.2/zeppelin-0.6.2-bin-netinst.tgz"
zeppelin_version = "0.6.2"
zeppelin_interpreters = "md,python"
python3_version = "3.4"
pyspark_local_path_dir = '/home/ubuntu/.local/share/zeppelin/interpreters/pyspark_local/'
py3spark_local_path_dir = '/home/ubuntu/.local/share/zeppelin/interpreters/py3spark_local/'
zeppelin_conf_file = '/home/ubuntu/.local/share/zeppelin/zeppelin_notebook_config.py'
templates_dir = '/root/templates/'


def prepare_disk():
    if not exists('/home/ubuntu/.ensure_dir/disk_ensured'):
        try:
            sudo('''bash -c 'echo -e "o\nn\np\n1\n\n\nw" | fdisk /dev/xvdb' ''')
            sudo('mkfs.ext4 /dev/xvdb1')
            sudo('mount /dev/xvdb1 /opt/')
            sudo(''' bash -c "echo '/dev/xvdb1 /opt/ ext4 errors=remount-ro 0 1' >> /etc/fstab" ''')
            sudo('touch /home/ubuntu/.ensure_dir/disk_ensured')
        except:
            sys.exit(1)


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))

def ensure_jre_jdk():
    if not exists('/home/ubuntu/.ensure_dir/jre_jdk_ensured'):
        try:
            sudo('apt-get install -y default-jre')
            sudo('apt-get install -y default-jdk')
            sudo('touch /home/ubuntu/.ensure_dir/jre_jdk_ensured')
        except:
            sys.exit(1)

def ensure_s3_libs():
    if not exists('/home/ubuntu/.ensure_dir/zp_s3_lib_ensured'):
        try:
            sudo('wget -P /opt/zeppelin/interpreter/spark/dep http://central.maven.org/maven2/com/amazonaws/aws-java-sdk-core/1.10.75/aws-java-sdk-core-1.10.75.jar')
            sudo('wget -P /opt/zeppelin/interpreter/spark/dep http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.6.0/hadoop-aws-2.6.0.jar')
            sudo('wget -P /opt/zeppelin/interpreter/spark/dep http://central.maven.org/maven2/com/amazonaws/aws-java-sdk-s3/1.10.75/aws-java-sdk-s3-1.10.75.jar')
            sudo('wget -P /opt/zeppelin/interpreter/spark/dep http://central.maven.org/maven2/org/anarres/lzo/lzo-hadoop/1.0.5/lzo-hadoop-1.0.5.jar')
            sudo('touch /home/ubuntu/.ensure_dir/zp_s3_lib_ensured')
        except:
            sys.exit(1)


def ensure_python3_kernel():
    if not exists('/home/ubuntu/.ensure_dir/python3_kernel_ensured'):
        try:
            sudo('apt-get install python3-setuptools')
            sudo('apt install -y python3-pip')
            sudo('add-apt-repository -y ppa:fkrull/deadsnakes')
            sudo('apt update')
            sudo('apt install -y python' + python3_version + ' python' + python3_version +'-dev')
            sudo('touch /home/ubuntu/.ensure_dir/python3_kernel_ensured')
        except:
            sys.exit(1)


def configure_notebook_server(notebook_name):
    if not exists('/home/ubuntu/.ensure_dir/zeppelin_ensured'):
        ensure_jre_jdk()
        try:
            sudo('wget ' + zeppelin_link + ' -O /tmp/zeppelin-' + zeppelin_version + '-bin-netinst.tgz')
            sudo('tar -zxvf /tmp/zeppelin-' + zeppelin_version + '-bin-netinst.tgz -C /opt/')
            sudo('ln -s /opt/zeppelin-' + zeppelin_version + '-bin-netinst /opt/zeppelin')
            sudo('cp /opt/zeppelin/conf/zeppelin-env.sh.template /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('cp /opt/zeppelin/conf/zeppelin-site.xml.template /opt/zeppelin/conf/zeppelin-site.xml')
            sudo('sed -i \"/# export ZEPPELIN_PID_DIR/c\export ZEPPELIN_PID_DIR=/var/run/zeppelin\" /opt/zeppelin/conf/zeppelin-env.sh')
            sudo('sed -i \"/# export ZEPPELIN_IDENT_STRING/c\export ZEPPELIN_IDENT_STRING=notebook\" /opt/zeppelin/conf/zeppelin-env.sh')
            put(templates_dir + 'interpreter.json', '/tmp/interpreter.json')
            sudo('cp /tmp/interpreter.json /opt/zeppelin/conf/interpreter.json')
            sudo('mkdir /var/log/zeppelin')
            sudo('mkdir /var/run/zeppelin')
            sudo('ln -s /var/log/zeppelin /opt/zeppelin-' + zeppelin_version + '-bin-netinst/logs')
            sudo('chown ubuntu:ubuntu -R /var/log/zeppelin')
            sudo('ln -s /var/run/zeppelin /opt/zeppelin-' + zeppelin_version + '-bin-netinst/run')
            sudo('chown ubuntu:ubuntu -R /var/run/zeppelin')
            sudo('/opt/zeppelin/bin/install-interpreter.sh --name ' + zeppelin_interpreters + ' --proxy-url $http_proxy')
            ensure_s3_libs()
            sudo('chown ubuntu:ubuntu -R /opt/zeppelin-' + zeppelin_version + '-bin-netinst')
        except:
            sys.exit(1)
        try:
            put(templates_dir + 'zeppelin-notebook.service', '/tmp/zeppelin-notebook.service')
            sudo("chmod 644 /tmp/zeppelin-notebook.service")
            sudo('cp /tmp/zeppelin-notebook.service /etc/systemd/system/zeppelin-notebook.service')
            sudo("systemctl daemon-reload")
            sudo("systemctl enable zeppelin-notebook")
            sudo("systemctl start zeppelin-notebook")
            sudo('echo \"d /var/run/zeppelin  0755 ' + args.os_user + '\" > /usr/lib/tmpfiles.d/zeppelin.conf')
            sudo('touch /home/ubuntu/.ensure_dir/zeppelin_ensured')
        except:
            sys.exit(1)

        ensure_python3_kernel()


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Configuring notebook server."
    try:
        if not exists('/home/ubuntu/.ensure_dir'):
            sudo('mkdir /home/ubuntu/.ensure_dir')
    except:
        sys.exit(1)
    prepare_disk()
    configure_notebook_server("_".join(args.instance_name.split()))
