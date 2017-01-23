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

import argparse
import json
import sys
from dlab.notebook_lib import *
from dlab.fab import *

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--spark_version', type=str, default='')
parser.add_argument('--hadoop_version', type=str, default='')
parser.add_argument('--os_user', type=str, default='')
args = parser.parse_args()

spark_version = args.spark_version
hadoop_version = args.hadoop_version
scala_version = '2.11.8'
scala_link = "http://www.scala-lang.org/files/archive/scala-" + scala_version + ".deb"
spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-" + spark_version + "-bin-hadoop" + hadoop_version + ".tgz"
pyspark_local_path_dir = '/home/' + args.os_user + '/.local/share/jupyter/kernels/pyspark_local/'
py3spark_local_path_dir = '/home/' + args.os_user + '/.local/share/jupyter/kernels/py3spark_local/'
jupyter_conf_file = '/home/' + args.os_user + '/.local/share/jupyter/jupyter_notebook_config.py'
scala_kernel_path = '/usr/local/share/jupyter/kernels/apache_toree_scala/'
s3_jars_dir = '/opt/jars/'
templates_dir = '/root/templates/'
files_dir = '/root/files/'


def configure_notebook_server():
    if not exists('/home/' + args.os_user + '/.ensure_dir/jupyter_ensured'):
        try:
            sudo('pip install jupyter --no-cache-dir')
            sudo('rm -rf ' + jupyter_conf_file)
            sudo('jupyter notebook --generate-config --config ' + jupyter_conf_file)
            sudo('echo "c.NotebookApp.ip = \'*\'" >> ' + jupyter_conf_file)
            sudo('echo c.NotebookApp.open_browser = False >> ' + jupyter_conf_file)
            sudo('echo \'c.NotebookApp.cookie_secret = b"' + id_generator() + '"\' >> ' + jupyter_conf_file)
            sudo('''echo "c.NotebookApp.token = u''" >> ''' + jupyter_conf_file)
            sudo('echo \'c.KernelSpecManager.ensure_native_kernel = False\' >> ' + jupyter_conf_file)
        except:
            sys.exit(1)

        ensure_spark_scala(scala_link, spark_link, spark_version, hadoop_version, pyspark_local_path_dir,
                           py3spark_local_path_dir, templates_dir, scala_kernel_path, scala_version, args.os_user,
                           files_dir)

        try:
            put(templates_dir + 'jupyter-notebook.service', '/tmp/jupyter-notebook.service')
            sudo("chmod 644 /tmp/jupyter-notebook.service")
            sudo("sed -i 's|CONF_PATH|" + jupyter_conf_file + "|' /tmp/jupyter-notebook.service")
            sudo("sed -i 's|OS_USR|" + args.os_user + "|' /tmp/jupyter-notebook.service")
            sudo('\cp /tmp/jupyter-notebook.service /etc/systemd/system/jupyter-notebook.service')
            sudo('chown -R ' + args.os_user + ':' + args.os_user + ' /home/' + args.os_user + '/.local')
            sudo('mkdir /mnt/var')
            sudo('chown ' + args.os_user + ':' + args.os_user + ' /mnt/var')
            sudo("systemctl daemon-reload")
            sudo("systemctl enable jupyter-notebook")
            sudo("systemctl start jupyter-notebook")
            sudo('touch /home/' + args.os_user + '/.ensure_dir/jupyter_ensured')
        except:
            sys.exit(1)

        ensure_python3_kernel(args.os_user)

        ensure_s3_kernel(args.os_user, s3_jars_dir, files_dir, args.region)

        ensure_r_kernel(spark_version, args.os_user)


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
    prepare_disk(args.os_user)
    configure_notebook_server()
