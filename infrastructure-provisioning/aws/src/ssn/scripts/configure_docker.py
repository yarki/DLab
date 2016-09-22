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

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_docker_daemon():
    if not exists('/tmp/docker_daemon_ensured'):
        sudo('apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D')
        sudo('echo "deb https://apt.dockerproject.org/repo ubuntu-xenial main" | sudo tee /etc/apt/sources.list.d/docker.list')
        sudo('apt-get update')
        sudo('apt-cache policy docker-engine')
        sudo('apt-get install -y docker-engine')
        sudo('sysv-rc-conf docker on')
        sudo('touch /tmp/docker_daemon_ensured')


def configure_docker_daemon():
    pass


def pull_docker_images(image_list):
    pass


def build_docker_images(image_list):
    sudo('mkdir /project_images; chown ubuntu /project_images')
    local('scp -r -i %s /project_tree/* %s:/project_images/' % (args.keyfile, env.host_string))
    for image in image_list:
        name = image['name']
        tag = image['tag']
        sudo("cd /project_images; "
             "docker build --file %s/Dockerfile "
             "-t docker.epmc-bdcc.projects.epam.com/dlab-aws-%s:%s ." % (name, name, tag))


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing docker daemon"
    ensure_docker_daemon()

    print "Configuring docker daemon"
    configure_docker_daemon()

    print "Building dlab images"
    build_docker_images(deeper_config)
