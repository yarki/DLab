#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
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
# ============================================================================
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import crypt

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()

def ensure_mongo():
    if not exists('/tmp/mongo_ensured'):
        sudo('apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927')
        sudo('ver=`lsb_release -cs`; echo "deb http://repo.mongodb.org/apt/ubuntu $ver/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list; apt-get update')
        sudo('apt-get -y install mongodb-org')
        sudo('sysv-rc-conf mongod on')
        sudo('touch /tmp/mongo_ensured')


def configure_mongo():
    if not exists("/lib/systemd/system/mongod.service"):
        local('scp -i {} /usr/share/notebook_automation/templates/mongod.service_template {}:/tmp/mongod.service'.format(args.keyfile, env.host_string))
        sudo('mv /tmp/mongod.service /lib/systemd/system/mongod.service')
    local('scp -i {} /usr/share/notebook_automation/templates/configure_mongo.py {}:/tmp/configure_mongo.py'.format(args.keyfile, env.host_string))
    sudo('python /tmp/configure_mongo.py')


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing MongoDB"
    ensure_mongo()

    print "Configuring MongoDB"
    configure_mongo()