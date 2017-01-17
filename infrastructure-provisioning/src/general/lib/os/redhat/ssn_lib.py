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

import crypt
import yaml
from dlab.fab import *
from dlab.aws_meta import *
import os

def ensure_docker_daemon(dlab_path):
    try:
        if not exists('{}tmp/docker_daemon_ensured'.format(dlab_path)):
            sudo('yum update -y')
            sudo('curl -fsSL https://get.docker.com/ | sh')
            sudo('systemctl enable docker.service')
            sudo('systemctl start docker')
            sudo('touch {}tmp/docker_daemon_ensured'.format(dlab_path))
        return True
    except:
        return False


def ensure_nginx(dlab_path):
    try:
        if not exists('{}tmp/nginx_ensured'.format(dlab_path)):
            sudo('yum -y install nginx')
            sudo('systemctl restart nginx.service')
            sudo('chkconfig nginx on')
            sudo('touch {}tmp/nginx_ensured'.format(dlab_path))
        return True
    except:
        return False


def ensure_jenkins(dlab_path):
    try:
        if not exists('{}tmp/jenkins_ensured'.format(dlab_path)):
            sudo('wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat-stable/jenkins.repo')
            sudo('rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key')
            sudo('yum -y install java')
            sudo('yum -y install jenkins')
            sudo('touch {}tmp/jenkins_ensured'.format(dlab_path))
        return True
    except:
        return False


def configure_jenkins(dlab_path, os_user):
    try:
        if not exists('{}tmp/jenkins_configured'.format(dlab_path)):
            sudo('rm -rf /var/lib/jenkins/*')
            sudo('mkdir -p /var/lib/jenkins/jobs/')
            sudo('chown -R {0}:{0} /var/lib/jenkins/'.format(args.user))
            put('/root/templates/jenkins_jobs/*', '/var/lib/jenkins/jobs/')
            sudo("find /var/lib/jenkins/jobs/ -type f | xargs sed -i \'s/OS_USR/{}/g\'".format(os_user))
            sudo('chown -R jenkins:jenkins /var/lib/jenkins')
            sudo('/etc/init.d/jenkins stop; sleep 5')
            sudo('sed -i \'/JENKINS_PORT/ s/^/#/\' /etc/sysconfig/jenkins; echo \'JENKINS_PORT="8070"\' >> /etc/sysconfig/jenkins')
            sudo('sed -i \'/JENKINS_ARGS/ s|=""|="--prefix=/jenkins"|\' /etc/sysconfig/jenkins')
            sudo('semanage port -a -t http_port_t -p tcp 8070')
            sudo('setsebool -P httpd_can_network_connect 1')
            sudo('chkconfig jenkins on')
            sudo('systemctl start jenkins.service')
            sudo('echo "jenkins ALL = NOPASSWD:ALL" >> /etc/sudoers')
            sudo('touch {}tmp/jenkins_configured'.format(dlab_path))
        return True
    except:
        return False


