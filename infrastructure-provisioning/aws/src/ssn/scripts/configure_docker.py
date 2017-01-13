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
import argparse
import json
import sys
import os
from dlab.ssn_lib import *

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def build_docker_images(image_list):
    try:
        sudo('mkdir /project_images; chown ' + os.environ['general_os_user'] + ' /project_images')
        print "KEYFILE: " + args.keyfile
        print "HOST_STRING: " + args.host_string
        local('scp -r -i %s /project_tree/* %s:/project_images/' % (args.keyfile, env.host_string))
        for image in image_list:
            name = image['name']
            tag = image['tag']
            sudo("cd /project_images/%s; docker build "
                 "-t docker.epmc-bdcc.projects.epam.com/dlab-aws-%s:%s ." % (name, name, tag))
        return True
    except:
        return False


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    try:
        env['connection_attempts'] = 100
        env.key_filename = [args.keyfile]
        env.host_string = os.environ['general_os_user'] + '@' + args.hostname
        deeper_config = json.loads(args.additional_config)
    except:
        sys.exit(2)

    print "Installing docker daemon"
    if not ensure_docker_daemon():
        sys.exit(1)

    print "Building dlab images"
    if not build_docker_images(deeper_config):
        sys.exit(1)

    sys.exit(0)
