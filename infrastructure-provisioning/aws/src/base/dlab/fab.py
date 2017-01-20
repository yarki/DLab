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
import logging
import os
from dlab.aws_meta import *
from dlab.aws_actions import *


def ensure_apt(requisites):
    try:
        if not exists('/home/ubuntu/.ensure_dir/apt_upgraded'):
            sudo('apt-get update')
            sudo('apt-get -y install ' + requisites)
            sudo('unattended-upgrades -v')
            sudo('export LC_ALL=C')
            sudo('mkdir /home/ubuntu/.ensure_dir')
            sudo('touch /home/ubuntu/.ensure_dir/apt_upgraded')
        return True
    except:
        return False


def ensure_pip(requisites):
    try:
        if not exists('/home/ubuntu/.ensure_dir/pip_path_added'):
            sudo('echo PATH=$PATH:/usr/local/bin/:/opt/spark/bin/ >> /etc/profile')
            sudo('echo export PATH >> /etc/profile')
            sudo('pip install -U pip --no-cache-dir')
            sudo('pip install -U ' + requisites + ' --no-cache-dir')
            sudo('touch /home/ubuntu/.ensure_dir/pip_path_added')
        return True
    except:
        return False


def create_aws_config_files(generate_full_config=False):
    try:
        aws_user_dir = os.environ['AWS_DIR']
        logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
        logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))

        with open(aws_user_dir + '/config', 'w') as aws_file:
            aws_file.write("[default]\n")
            aws_file.write("region = %s\n" % os.environ['creds_region'])

        if generate_full_config:
            with open(aws_user_dir + '/credentials', 'w') as aws_file:
                aws_file.write("[default]\n")
                aws_file.write("aws_access_key_id = %s\n" % os.environ['creds_access_key'])
                aws_file.write("aws_secret_access_key = %s\n" % os.environ['creds_secret_access_key'])

        logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
        logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))

        return True
    except:
        return False


def put_resource_status(resource, status, instance):
    env['connection_attempts'] = 100
    keyfile = "/root/keys/" + os.environ['creds_key_name'] + ".pem"
    hostname = get_instance_hostname(os.environ['conf_service_base_name'] + '-ssn')
    env.key_filename = [keyfile]
    env.host_string = 'ubuntu@' + hostname
    sudo('python ' + os.environ[instance + '_dlab_path'] + 'tmp/resource_status.py --resource {} --status {}'.format(resource, status))

