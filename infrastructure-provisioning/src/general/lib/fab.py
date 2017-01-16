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
import random
import sys
import string


def ensure_pip(requisites):
    try:
        if not exists('/home/{}/.ensure_dir/pip_path_added'.format(os.environ['general_os_user'])):
            sudo('echo PATH=$PATH:/usr/local/bin/:/opt/spark/bin/ >> /etc/profile')
            sudo('echo export PATH >> /etc/profile')
            sudo('pip install -U pip --no-cache-dir')
            sudo('pip install -U ' + requisites + ' --no-cache-dir')
            sudo('touch /home/{}/.ensure_dir/pip_path_added'.format(os.environ['general_os_user']))
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
            aws_file.write("region = {}\n".format(os.environ['creds_region']))

        if generate_full_config:
            with open(aws_user_dir + '/credentials', 'w') as aws_file:
                aws_file.write("[default]\n")
                aws_file.write("aws_access_key_id = {}\n".format(os.environ['creds_access_key']))
                aws_file.write("aws_secret_access_key = {}\n".format(os.environ['creds_secret_access_key']))

        logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
        logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))

        return True
    except:
        return False


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def prepare_disk(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/disk_ensured'):
        try:
            sudo('''bash -c 'echo -e "o\nn\np\n1\n\n\nw" | fdisk /dev/xvdb' ''')
            sudo('mkfs.ext4 /dev/xvdb1')
            sudo('mount /dev/xvdb1 /opt/')
            sudo(''' bash -c "echo '/dev/xvdb1 /opt/ ext4 errors=remount-ro 0 1' >> /etc/fstab" ''')
            sudo('touch /home/' + os_user + '/.ensure_dir/disk_ensured')
        except:
            sys.exit(1)


def ensure_s3_kernel(os_user, s3_jars_dir, templates_dir, region):
    if not exists('/home/' + os_user + '/.ensure_dir/s3_kernel_ensured'):
        try:
            sudo('mkdir -p ' + s3_jars_dir)
            put(templates_dir + 'jars/local_jars.tar.gz', '/tmp/local_jars.tar.gz')
            sudo('tar -xzf /tmp/local_jars.tar.gz -C ' + s3_jars_dir)
            put(templates_dir + 'spark-defaults_local.conf', '/tmp/spark-defaults_local.conf')
            sudo("sed -i 's/URL/https:\/\/s3-{}.amazonaws.com/' /tmp/spark-defaults_local.conf".format(region))
            sudo('\cp /tmp/spark-defaults_local.conf /opt/spark/conf/spark-defaults.conf')
            sudo('touch /home/' + os_user + '/.ensure_dir/s3_kernel_ensured')
        except:
            sys.exit(1)