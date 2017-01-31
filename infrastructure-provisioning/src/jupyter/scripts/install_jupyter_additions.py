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
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import sys
from dlab.notebook_lib import *

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
parser.add_argument('--os_user', type=str, default='')
args = parser.parse_args()

s3_jars_dir = '/opt/jars/'


def add_breeze_library_local():
    breeze_tmp_dir = '/tmp/breeze_tmp_local/'
    sudo('mkdir -p ' + breeze_tmp_dir)
    sudo('wget http://central.maven.org/maven2/org/scalanlp/breeze_2.11/0.12/breeze_2.11-0.12.jar -O ' +
         breeze_tmp_dir + 'breeze_2.11-0.12.jar')
    sudo('wget http://central.maven.org/maven2/org/scalanlp/breeze-natives_2.11/0.12/breeze-natives_2.11-0.12.jar -O ' +
         breeze_tmp_dir + 'breeze-natives_2.11-0.12.jar')
    sudo('wget http://central.maven.org/maven2/org/scalanlp/breeze-viz_2.11/0.12/breeze-viz_2.11-0.12.jar -O ' +
         breeze_tmp_dir + 'breeze-viz_2.11-0.12.jar')
    sudo('wget http://central.maven.org/maven2/org/scalanlp/breeze-macros_2.11/0.12/breeze-macros_2.11-0.12.jar -O ' +
         breeze_tmp_dir + 'breeze-macros_2.11-0.12.jar')
    sudo('wget http://central.maven.org/maven2/org/scalanlp/breeze-parent_2.11/0.12/breeze-parent_2.11-0.12.jar -O ' +
         breeze_tmp_dir + 'breeze-parent_2.11-0.12.jar')
    sudo('mv ' + breeze_tmp_dir + '* ' + s3_jars_dir)


def ensure_libraries_py3(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/ensure_libraries_py3_installed'):
        try:
            sudo('pip3 install -U pip --no-cache-dir')
            sudo('pip3 install boto boto3 --no-cache-dir')
            sudo('pip3 install NumPy SciPy Matplotlib pandas Sympy Pillow sklearn fabvenv fabric-virtualenv --no-cache-dir')
            sudo('jupyter-kernelspec remove -f python3')
            sudo('touch /home/' + os_user + '/.ensure_dir/ensure_libraries_py3_installed')
        except:
            sys.exit(1)


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = args.os_user + '@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing required libraries for Python 2.7"
    ensure_libraries_py2(args.os_user)

    print "Installing required libraries for Python 3"
    ensure_libraries_py3(args.os_user)

    print "Installing notebook additions: matplotlib."
    ensure_matplot(args.os_user)

    print "Installing notebook additions: sbt."
    ensure_sbt(args.os_user)

    print "Installing Breeze library"
    add_breeze_library_local()
