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
from dlab.notebook_lib import *
from dlab.fab import *
import os


def enable_proxy(proxy_host, proxy_port):
    if not exists('/tmp/proxy_enabled'):
        try:
            proxy_string = "http://%s:%s" % (proxy_host, proxy_port)
            sudo('echo export http_proxy=' + proxy_string + ' >> /etc/profile')
            sudo('echo export https_proxy=' + proxy_string + ' >> /etc/profile')
            sudo("echo 'proxy={}' >> /etc/yum.conf".format(proxy_string))
            sudo('yum clean all')
            sudo('touch /tmp/proxy_enabled ')
        except:
            sys.exit(1)


def ensure_spark_scala(scala_link, spark_link, spark_version, hadoop_version, pyspark_local_path_dir, py3spark_local_path_dir, templates_dir, scala_kernel_path, scala_version, os_user):
    if not exists('/home/{}/.ensure_dir/spark_scala_ensured'.format(os_user)):
        try:
            sudo('yum install -y java-1.8.0-openjdk-devel')
            sudo('yum install -y java-1.8.0-openjdk')
            sudo('wget ' + scala_link + ' -O /tmp/scala.rpm')
            sudo('rpm -i /tmp/scala.rpm')

            sudo('wget ' + spark_link + ' -O /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz')
            sudo('tar -zxvf /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz -C /opt/')
            sudo('mv /opt/spark-' + spark_version + '-bin-hadoop' + hadoop_version + ' /opt/spark')
            sudo('mkdir -p ' + pyspark_local_path_dir)
            sudo('mkdir -p ' + py3spark_local_path_dir)
            sudo('touch ' + pyspark_local_path_dir + 'kernel.json')
            sudo('touch ' + py3spark_local_path_dir + 'kernel.json')
            put(templates_dir + 'pyspark_local_template.json', '/tmp/pyspark_local_template.json')
            put(templates_dir + 'py3spark_local_template.json', '/tmp/py3spark_local_template.json')
            sudo(
                "PYJ=`find /opt/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; sed -i 's|PY4J|'$PYJ'|g' /tmp/pyspark_local_template.json")
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/pyspark_local_template.json')
            sudo('\cp /tmp/pyspark_local_template.json ' + pyspark_local_path_dir + 'kernel.json')
            sudo(
                "PYJ=`find /opt/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; sed -i 's|PY4J|'$PYJ'|g' /tmp/py3spark_local_template.json")
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/py3spark_local_template.json')
            sudo('\cp /tmp/py3spark_local_template.json ' + py3spark_local_path_dir + 'kernel.json')
            sudo('pip install --pre toree --no-cache-dir')
            sudo('ln -s /opt/spark/ /usr/local/spark')
            sudo('jupyter toree install')
            sudo('mv ' + scala_kernel_path + 'lib/* /tmp/')
            put(templates_dir + 'toree-assembly-0.2.0.jar', '/tmp/toree-assembly-0.2.0.jar')
            sudo('mv /tmp/toree-assembly-0.2.0.jar ' + scala_kernel_path + 'lib/')
            sudo('sed -i "s|Apache Toree - Scala|Local Apache Toree - Scala (Scala-' + scala_version + ', Spark-' + spark_version + ')|g" ' + scala_kernel_path + 'kernel.json')
            sudo('touch /home/{}/.ensure_dir/spark_scala_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_python3_kernel(os_user):
    if not exists('/home/{}/.ensure_dir/python3_kernel_ensured'.format(os_user)):
        try:
            sudo('yum install -y python-setuptools python-wheel')
            sudo('yum install -y python34-setuptools python34-pip')
            sudo('yum install -y openssl-devel openssl-libs python-virtualenv python-devel python34-devel libxml2-devel libxslt-devel')
            sudo('pip3 install ipython ipykernel --no-cache-dir')
            sudo('python3 -m ipykernel install')
            sudo('touch /home/{}/.ensure_dir/python3_kernel_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_r_kernel(spark_version, os_user):
    templates_dir = '/root/templates/'
    kernels_dir = '/home/{}/.local/share/jupyter/kernels/'.format(os_user)
    if not exists('/home/{}/.ensure_dir/r_kernel_ensured'.format(os_user)):
        try:
            sudo('yum install -y cmake')
            sudo('yum -y install libcur*')
            sudo('echo -e "[base]\nname=CentOS-7-Base\nbaseurl=http://buildlogs.centos.org/centos/7/os/x86_64-20140704-1/\ngpgcheck=1\ngpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7\npriority=1\nexclude=php mysql" >> /etc/yum.repos.d/CentOS-base.repo')
            sudo('yum install -y R R-core R-core-devel R-devel --nogpgcheck')

            sudo('R CMD javareconf')
            sudo('cd /root; git clone https://github.com/zeromq/zeromq4-x.git; cd zeromq4-x/; mkdir build; cd build; cmake ..; make install; ldconfig')
            sudo('R -e "install.packages(\'R6\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'pbdZMQ\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'RCurl\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'devtools\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'reshape2\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'caTools\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'rJava\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "install.packages(\'ggplot2\',repos=\'http://cran.us.r-project.org\')"')
            sudo('R -e "library(\'devtools\');install.packages(repos=\'http://cran.us.r-project.org\',c(\'rzmq\',\'repr\',\'digest\',\'stringr\',\'RJSONIO\',\'functional\',\'plyr\'))"')
            sudo('R -e "library(\'devtools\');install_github(\'IRkernel/repr\');install_github(\'IRkernel/IRdisplay\');install_github(\'IRkernel/IRkernel\');"')
            sudo('R -e "install.packages(\'RJDBC\',repos=\'http://cran.us.r-project.org\',dep=TRUE)"')
            run('R -e "IRkernel::installspec()"')
            r_version = sudo("R --version | awk '/version / {print $3}'")
            put(templates_dir + 'r_template.json', '/tmp/r_template.json')
            sudo('sed -i "s|R_VER|' + r_version + '|g" /tmp/r_template.json')
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/r_template.json')
            sudo('\cp -f /tmp/r_template.json {}/ir/kernel.json'.format(kernels_dir))
            sudo('cd /usr/local/spark/R/lib/SparkR; R -e "devtools::install(\'.\')"')
            sudo('chown -R ' + os_user + ':' + os_user + ' /home/' + os_user + '/.local')
            sudo('touch /home/{}/.ensure_dir/r_kernel_ensured'.format(os_user))
        except:
            sys.exit(1)