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


def ensure_r_local_kernel(spark_version, os_user, templates_dir, kernels_dir):
    if not exists('/home/{}/.ensure_dir/r_kernel_ensured'.format(os_user)):
        try:
            r_version = sudo("R --version | awk '/version / {print $3}'")
            put(templates_dir + 'r_template.json', '/tmp/r_template.json')
            sudo('sed -i "s|R_VER|' + r_version + '|g" /tmp/r_template.json')
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/r_template.json')
            sudo('\cp -f /tmp/r_template.json {}/ir/kernel.json'.format(kernels_dir))
            sudo('chown -R ' + os_user + ':' + os_user + ' /home/' + os_user + '/.local')
            sudo('touch /home/{}/.ensure_dir/r_kernel_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_r(os_user):
    if not exists('/home/{}/.ensure_dir/r_ensured'.format(os_user)):
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
            sudo('chown -R ' + os_user + ':' + os_user + ' /home/' + os_user + '/.local')
            run('R -e "IRkernel::installspec()"')
            sudo('cd /usr/local/spark/R/lib/SparkR; R -e "devtools::install(\'.\')"')
            sudo('touch /home/{}/.ensure_dir/r_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_matplot(os_user):
    if not exists('/home/{}/.ensure_dir/matplot_ensured'.format(os_user)):
        try:
            sudo('yum install -y python-matplotlib --nogpgcheck')
            sudo('pip2 install matplotlib --no-cache-dir')
            sudo('pip3 install matplotlib --no-cache-dir')
            sudo('touch /home/{}/.ensure_dir/matplot_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_sbt(os_user):
    if not exists('/home/{}/.ensure_dir/sbt_ensured'.format(os_user)):
        try:
            sudo('curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo')
            sudo('yum install -y sbt')
            sudo('touch /home/{}/.ensure_dir/sbt_ensured'.format(os_user))
        except:
            sys.exit(1)


def ensure_jre_jdk(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/jre_jdk_ensured'):
        try:
            sudo('yum install -y java-1.8.0-openjdk')
            sudo('yum install -y java-1.8.0-openjdk-devel')
            sudo('touch /home/' + os_user + '/.ensure_dir/jre_jdk_ensured')
        except:
            sys.exit(1)


def ensure_scala(scala_link, scala_version, os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/scala_ensured'):
        try:
            sudo('wget {}scala-{}.rpm -O /tmp/scala.rpm'.format(scala_link, scala_version))
            sudo('rpm -i /tmp/scala.rpm')
            sudo('touch /home/' + os_user + '/.ensure_dir/scala_ensured')
        except:
            sys.exit(1)


def ensure_additional_python_libs(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/additional_python_libs_ensured'):
        try:
            sudo('yum clean all')
            sudo('yum install -y zlib-devel libjpeg-turbo-devel --nogpgcheck')
            if os.environ['application'] == 'jupyter' or os.environ['application'] == 'zeppelin':
                sudo('pip2 install NumPy SciPy pandas Sympy Pillow sklearn --no-cache-dir')
                sudo('pip3 install NumPy SciPy pandas Sympy Pillow sklearn --no-cache-dir')
            if os.environ['application'] == 'tensor':
                sudo('pip2 install keras opencv-python h5py --no-cache-dir')
                sudo('python2 -m ipykernel install')
                sudo('pip3 install keras opencv-python h5py --no-cache-dir')
                sudo('python3 -m ipykernel install')
            sudo('touch /home/' + os_user + '/.ensure_dir/additional_python_libs_ensured')
        except:
            sys.exit(1)


def ensure_python3_specific_version(python3_version, os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/python3_specific_version_ensured'):
        try:
            sudo('yum install -y yum-utils python34 openssl-devel')
            sudo('yum -y groupinstall development')
            if len(python3_version) < 4:
                python3_version = python3_version + ".0"
            sudo('wget https://www.python.org/ftp/python/{0}/Python-{0}.tgz'.format(python3_version))
            sudo('tar xzf Python-{0}.tgz; cd Python-{0}; ./configure --prefix=/usr/local; make altinstall'.format(python3_version))
            sudo('touch /home/' + os_user + '/.ensure_dir/python3_specific_version_ensured')
        except:
            sys.exit(1)


def ensure_python2_libraries(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/python2_libraries_ensured'):
        try:
            sudo('yum install -y https://forensics.cert.org/centos/cert/7/x86_64/pyparsing-2.0.3-1.el7.noarch.rpm')
            sudo('yum install -y python-setuptools python-wheel')
            sudo('yum install -y python-virtualenv openssl-devel python-devel openssl-libs libxml2-devel libxslt-devel')
            sudo('python2 -m pip install backports.shutil_get_terminal_size ipython ipykernel')
            sudo('echo y | python2 -m pip uninstall backports.shutil_get_terminal_size')
            sudo('python2 -m pip install backports.shutil_get_terminal_size')
            sudo('pip2 install -U pip setuptools --no-cache-dir')
            sudo('pip2 install boto3 --no-cache-dir')
            sudo('pip2 install fabvenv fabric-virtualenv --no-cache-dir')
            sudo('touch /home/' + os_user + '/.ensure_dir/python2_libraries_ensured')
        except:
            sys.exit(1)


def ensure_python3_libraries(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/python3_libraries_ensured'):
        try:
            sudo('yum install -y python34-pip python34-devel')
            sudo('pip3 install -U pip setuptools --no-cache-dir')
            sudo('pip3 install boto3 --no-cache-dir')
            sudo('pip3 install fabvenv fabric-virtualenv --no-cache-dir')
            sudo('pip3 install ipython ipykernel --no-cache-dir')
            sudo('touch /home/' + os_user + '/.ensure_dir/python3_libraries_ensured')
        except:
            sys.exit(1)


def install_rstudio(os_user, local_spark_path, rstudio_pass):
    if not exists('/home/' + os_user + '/.ensure_dir/rstudio_ensured'):
        try:
            sudo('yum install -y java-1.8.0-openjdk-devel')
            sudo('yum install -y java-1.8.0-openjdk')
            sudo('yum install -y cmake')
            sudo('yum -y install libcur*')
            sudo('echo -e "[base]\nname=CentOS-7-Base\nbaseurl=http://buildlogs.centos.org/centos/7/os/x86_64-20140704-1/\ngpgcheck=1\ngpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7\npriority=1\nexclude=php mysql" >> /etc/yum.repos.d/CentOS-base.repo')
            sudo('yum install -y R R-core R-core-devel R-devel --nogpgcheck')
            sudo('yum install -y --nogpgcheck https://download2.rstudio.org/rstudio-server-rhel-1.0.136-x86_64.rpm')

            sudo('R CMD javareconf')
            sudo('R -e \'install.packages("rmarkdown", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('R -e \'install.packages("base64enc", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('R -e \'install.packages("tibble", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('mkdir /mnt/var')
            sudo('chown ' + os_user + ':' + os_user + ' /mnt/var')
            sudo('touch /home/' + os_user + '/.Renviron')
            sudo('chown ' + os_user + ':' + os_user + ' /home/' + os_user + '/.Renviron')
            sudo('''echo 'SPARK_HOME="''' + local_spark_path + '''"' >> /home/''' + os_user + '''/.Renviron''')
            sudo('touch /home/' + os_user + '/.Rprofile')
            sudo('chown ' + os_user + ':' + os_user + ' /home/' + os_user + '/.Rprofile')
            sudo('''echo 'library(SparkR, lib.loc = c(file.path(Sys.getenv("SPARK_HOME"), "R", "lib")))' >> /home/''' + os_user + '''/.Rprofile''')
            sudo('rstudio-server start')
            sudo('echo "' + os_user + ':' + rstudio_pass + '" | chpasswd')
            sudo("sed -i '/exit 0/d' /etc/rc.local")
            sudo('''bash -c "echo \'sed -i 's/^#SPARK_HOME/SPARK_HOME/' /home/''' + os_user + '''/.Renviron\' >> /etc/rc.local"''')
            sudo("bash -c 'echo exit 0 >> /etc/rc.local'")
            sudo('touch /home/' + os_user + '/.ensure_dir/rstudio_ensured')
        except:
            sys.exit(1)
    else:
        try:
            sudo('echo "' + os_user + ':' + rstudio_pass + '" | chpasswd')
        except:
            sys.exit(1)


def install_tensor(os_user, tensorflow_version, files_dir, templates_dir):
    if not exists('/home/' + os_user + '/.ensure_dir/tensor_ensured'):
        try:
            sudo('yum -y install gcc kernel-devel-$(uname -r) kernel-headers-$(uname -r)')
            # install cuda
            sudo('wget https://developer.nvidia.com/compute/cuda/8.0/prod/local_installers/cuda-repo-rhel7-8-0-local-8.0.44-1.x86_64-rpm')
            sudo('mv cuda-repo-rhel7-8-0-local-8.0.44-1.x86_64-rpm cuda-repo-rhel7-8-0-local-8.0.44-1.x86_64.rpm; rpm -i cuda-repo-rhel7-8-0-local-8.0.44-1.x86_64.rpm')
            sudo('yum clean all')
            sudo('yum -y install cuda')
            sudo('pip3 install --upgrade pip wheel numpy')
            sudo('mv /usr/local/cuda-8.0 /opt/')
            sudo('ln -s /opt/cuda-8.0 /usr/local/cuda-8.0')
            sudo('rm -f /home/' + os_user + '/cuda-repo-rhel7-8-0-local-8.0.44-1.x86_64-rpm')
            # install cuDNN
            put(files_dir + 'cudnn-8.0-linux-x64-v5.1.tgz', '/tmp/cudnn-8.0-linux-x64-v5.1.tgz')
            run('tar xvzf /tmp/cudnn-8.0-linux-x64-v5.1.tgz -C /tmp')
            sudo('mkdir -p /opt/cudnn/include')
            sudo('mkdir -p /opt/cudnn/lib64')
            sudo('mv /tmp/cuda/include/cudnn.h /opt/cudnn/include')
            sudo('mv /tmp/cuda/lib64/libcudnn* /opt/cudnn/lib64')
            sudo('chmod a+r /opt/cudnn/include/cudnn.h /opt/cudnn/lib64/libcudnn*')
            run('echo "export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:/opt/cudnn/lib64\"" >> ~/.bash_profile')
            # install TensorFlow and run TensorBoard
            sudo('wget https://storage.googleapis.com/tensorflow/linux/gpu/tensorflow_gpu-' + tensorflow_version + '-cp27-none-linux_x86_64.whl')
            sudo('wget https://storage.googleapis.com/tensorflow/linux/gpu/tensorflow_gpu-' + tensorflow_version + '-cp34-cp34m-linux_x86_64.whl')
            sudo('python2.7 -m pip install --upgrade tensorflow_gpu-' + tensorflow_version + '-cp27-none-linux_x86_64.whl')
            run('python3 -m pip install --upgrade tensorflow_gpu-' + tensorflow_version + '-cp34-cp34m-linux_x86_64.whl')
            sudo('mkdir /var/log/tensorboard')
            put(templates_dir + 'tensorboard-python2.service', '/tmp/tensorboard-python2.service')
            put(templates_dir + 'tensorboard-python3.service', '/tmp/tensorboard-python3.service')
            sudo("sed -i 's|OS_USR|" + os_user + "|' /tmp/tensorboard-python*")
            sudo("chmod 644 /tmp/tensorboard-python*")
            sudo('\cp /tmp/tensorboard-python* /etc/systemd/system/')
            sudo('mkdir -p /var/log/tensorboard_py2; chown ' + os_user + ':' + os_user + ' -R /var/log/tensorboard_py2')
            sudo('mkdir -p /var/log/tensorboard_py3; chown ' + os_user + ':' + os_user + ' -R /var/log/tensorboard_py3')
            sudo("systemctl daemon-reload")
            sudo("systemctl enable tensorboard-python2")
            sudo("systemctl enable tensorboard-python3")
            sudo("systemctl start tensorboard-python2")
            sudo("systemctl start tensorboard-python3")
            # install Theano
            sudo('python2.7 -m pip install Theano')
            sudo('python3 -m pip install Theano')
            sudo('touch /home/' + os_user + '/.ensure_dir/tensor_ensured')
        except:
            sys.exit(1)


def install_maven():
    sudo('yum install -y unzip')
    with cd('/tmp/'):
        sudo('wget http://apache.volia.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip')
        sudo('unzip apache-maven-3.3.9-bin.zip')
        sudo('mv apache-maven-3.3.9/ /opt/maven')
        sudo('ln -s /opt/maven/bin/mvn /usr/bin/mvn')


def install_livy_dependencies():
    sudo('pip install cloudpickle requests requests-kerberos flake8 flaky pytest')
    sudo('pip3 install cloudpickle requests requests-kerberos flake8 flaky pytest')


def install_maven_emr():
    local('sudo yum install -y unzip')
    with lcd('/tmp/'):
        local('sudo wget http://apache.volia.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip')
        local('sudo unzip apache-maven-3.3.9-bin.zip')
        local('sudo mv apache-maven-3.3.9/ /opt/maven')
        local('sudo ln -s /opt/maven/bin/mvn /usr/bin/mvn')


def install_livy_dependencies_emr():
    local('sudo pip install cloudpickle requests requests-kerberos flake8 flaky pytest')
    local('sudo pip3 install cloudpickle requests requests-kerberos flake8 flaky pytest')