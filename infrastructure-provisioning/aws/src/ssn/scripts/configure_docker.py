#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_docker_daemon():
    try:
        if not exists('/tmp/docker_daemon_ensured'):
            sudo('apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D')
            sudo('echo "deb https://apt.dockerproject.org/repo ubuntu-xenial main" | sudo tee /etc/apt/sources.list.d/docker.list')
            sudo('apt-get update')
            sudo('apt-cache policy docker-engine')
            sudo('apt-get install -y docker-engine')
            sudo('sysv-rc-conf docker on')
            sudo('touch /tmp/docker_daemon_ensured')
        return True
    except:
        return False
    return True


def configure_docker_daemon():
    pass


def pull_docker_images(image_list):
    pass


def build_docker_images(image_list):
    try:
        sudo('mkdir /project_images; chown ubuntu /project_images')
        local('scp -r -i %s /project_tree/* %s:/project_images/' % (args.keyfile, env.host_string))
        for image in image_list:
            name = image['name']
            tag = image['tag']
            sudo("cd /project_images/%s; docker build "
                 "-t docker.epmc-bdcc.projects.epam.com/dlab-aws-%s:%s ." % (name, name, tag))
        return True
    except:
        return False
    return True


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    try:
        env['connection_attempts'] = 100
        env.key_filename = [args.keyfile]
        env.host_string = 'ubuntu@' + args.hostname
        deeper_config = json.loads(args.additional_config)
    except:
        sys.exit(2)

    print "Installing docker daemon"
    if not ensure_docker_daemon():
        sys.exit(1)

    print "Configuring docker daemon"
    if not configure_docker_daemon():
        sys.exit(1)

    print "Building dlab images"
    if not build_docker_images(deeper_config):
        sys.exit(1)

    sys.exit(0)
