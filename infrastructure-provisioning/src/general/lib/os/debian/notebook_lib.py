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
from dlab.common_lib import *
import os


def enable_proxy(proxy_host, proxy_port):
    if not exists('/tmp/proxy_enabled'):
        try:
            proxy_string = "http://%s:%s" % (proxy_host, proxy_port)
            sudo('echo export http_proxy=' + proxy_string + ' >> /etc/profile')
            sudo('echo export https_proxy=' + proxy_string + ' >> /etc/profile')
            sudo("echo 'Acquire::http::Proxy \"" + proxy_string + "\";' >> /etc/apt/apt.conf")
            sudo('touch /tmp/proxy_enabled ')

            print "Renewing gpg key"
            renew_gpg_key()
        except:
            sys.exit(1)


def ensure_spark_scala(scala_link, spark_link, spark_version, hadoop_version, pyspark_local_path_dir, py3spark_local_path_dir, templates_dir, scala_kernel_path, scala_version, os_user, files_dir):
    if not exists('/home/' + os_user + '/.ensure_dir/spark_scala_ensured'):
        try:
            sudo('apt-get install -y default-jre')
            sudo('apt-get install -y default-jdk')
            sudo('wget ' + scala_link + ' -O /tmp/scala.deb')
            sudo('dpkg -i /tmp/scala.deb')
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
            put(files_dir + 'toree-assembly-0.2.0.jar', '/tmp/toree-assembly-0.2.0.jar')
            sudo('mv /tmp/toree-assembly-0.2.0.jar ' + scala_kernel_path + 'lib/')
            sudo('sed -i "s|Apache Toree - Scala|Local Apache Toree - Scala (Scala-' + scala_version + ', Spark-' + spark_version + ')|g" ' + scala_kernel_path + 'kernel.json')
            sudo('touch /home/' + os_user + '/.ensure_dir/spark_scala_ensured')
        except:
            sys.exit(1)


def ensure_python3_kernel(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/python3_kernel_ensured'):
        try:
            sudo('apt-get install python3-setuptools')
            sudo('apt install -y python3-pip')
            sudo('pip3 install ipython ipykernel --no-cache-dir')
            sudo('python3 -m ipykernel install')
            sudo('apt-get install -y libssl-dev python-virtualenv')
            sudo('touch /home/' + os_user + '/.ensure_dir/python3_kernel_ensured')
        except:
            sys.exit(1)


def ensure_r_kernel(spark_version, os_user):
    templates_dir = '/root/templates/'
    kernels_dir = '/home/' + os_user + '/.local/share/jupyter/kernels/'
    if not exists('/home/' + os_user + '/.ensure_dir/r_kernel_ensured'):
        try:
            sudo('apt-get install -y r-base r-base-dev r-cran-rcurl')
            sudo('apt-get install -y libcurl4-openssl-dev libssl-dev libreadline-dev')
            sudo('apt-get install -y cmake')
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
            sudo('R -e "IRkernel::installspec()"')
            r_version = sudo("R --version | awk '/version / {print $3}'")
            put(templates_dir + 'r_template.json', '/tmp/r_template.json')
            sudo('sed -i "s|R_VER|' + r_version + '|g" /tmp/r_template.json')
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/r_template.json')
            sudo('\cp -f /tmp/r_template.json {}/ir/kernel.json'.format(kernels_dir))
            sudo('cd /usr/local/spark/R/lib/SparkR; R -e "devtools::install(\'.\')"')
            sudo('chown -R ' + os_user + ':' + os_user + ' /home/' + os_user + '/.local')
            sudo('touch /home/' + os_user + '/.ensure_dir/r_kernel_ensured')
        except:
            sys.exit(1)


def ensure_matplot(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/matplot_ensured'):
        try:
            sudo('apt-get build-dep -y python-matplotlib')
            sudo('pip install matplotlib --no-cache-dir')
            sudo('pip3 install matplotlib --no-cache-dir')
            sudo('touch /home/' + os_user + '/.ensure_dir/matplot_ensured')
        except:
            sys.exit(1)


def ensure_sbt(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/sbt_ensured'):
        try:
            sudo('apt-get install -y apt-transport-https')
            sudo('echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list')
            sudo('apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823')
            sudo('apt-get update')
            sudo('apt-get install -y sbt')
            sudo('touch /home/' + os_user + '/.ensure_dir/sbt_ensured')
        except:
            sys.exit(1)


def ensure_libraries_py2(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/ensure_libraries_py2_installed'):
        try:
            sudo('export LC_ALL=C')
            sudo('apt-get install -y libjpeg8-dev zlib1g-dev')
            sudo('pip2 install -U pip --no-cache-dir')
            sudo('pip2 install boto boto3 --no-cache-dir')
            sudo('pip2 install NumPy SciPy Matplotlib pandas Sympy Pillow sklearn fabvenv fabric-virtualenv --no-cache-dir')
            sudo('touch /home/' + os_user + '/.ensure_dir/ensure_libraries_py2_installed')
        except:
            sys.exit(1)


def ensure_jre_jdk(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/jre_jdk_ensured'):
        try:
            sudo('apt-get install -y default-jre')
            sudo('apt-get install -y default-jdk')
            sudo('touch /home/' + os_user + '/.ensure_dir/jre_jdk_ensured')
        except:
            sys.exit(1)


def ensure_python3_kernel_zeppelin(python3_version, os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/python3_kernel_ensured'):
        try:
            sudo('apt-get install python3-setuptools')
            sudo('apt install -y python3-pip')
            sudo('add-apt-repository -y ppa:fkrull/deadsnakes')
            sudo('apt update')
            sudo('apt install -y python' + python3_version + ' python' + python3_version +'-dev')
            sudo('touch /home/' + os_user + '/.ensure_dir/python3_kernel_ensured')
        except:
            sys.exit(1)


def ensure_libraries_py(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/ensure_libraries_py_installed'):
        try:
            sudo('export LC_ALL=C')
            sudo('apt-get install python3-setuptools')
            sudo('apt install -y python3-pip')
            sudo('apt-get install -y python-virtualenv')
            sudo('pip2 install -U pip --no-cache-dir')
            sudo('pip2 install boto boto3 --no-cache-dir')
            sudo('pip2 install fabvenv fabric-virtualenv --no-cache-dir')
            sudo('pip3 install -U pip --no-cache-dir')
            sudo('pip3 install boto boto3 --no-cache-dir')
            sudo('pip3 install fabvenv fabric-virtualenv --no-cache-dir')
            sudo('touch /home/' + os_user + '/.ensure_dir/ensure_libraries_py_installed')
        except:
            sys.exit(1)


def install_rstudio(os_user, local_spark_path, rstudio_pass):
    if not exists('/home/' + os_user + '/.ensure_dir/rstudio_ensured'):
        try:
            sudo('apt-get install -y default-jre')
            sudo('apt-get install -y default-jdk')
            sudo('apt-get install -y r-base')
            sudo('apt-get install -y gdebi-core')
            sudo('apt-get install -y r-cran-rjava r-cran-evaluate r-cran-formatr r-cran-yaml r-cran-rcpp r-cran-catools r-cran-jsonlite r-cran-ggplot2')
            sudo('R CMD javareconf')
            sudo('R -e \'install.packages("rmarkdown", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('R -e \'install.packages("base64enc", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('R -e \'install.packages("tibble", repos = "https://cran.revolutionanalytics.com")\'')
            sudo('wget https://download2.rstudio.org/rstudio-server-1.0.44-amd64.deb')
            sudo('gdebi -n rstudio-server-1.0.44-amd64.deb')
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


def install_tensor(os_user, tensorflow_version, files_dir):
    if not exists('/home/' + os_user + '/.ensure_dir/tensor_ensured'):
        try:
            # install cuda
            sudo('curl -O http://developer.download.nvidia.com/compute/cuda/repos/ubuntu1604/x86_64/cuda-repo-ubuntu1604_8.0.44-1_amd64.deb')
            sudo('dpkg -i cuda-repo-ubuntu1604_8.0.44-1_amd64.deb')
            sudo('apt-get update')
            sudo('apt-get -y install cuda')
            sudo('mv /usr/local/cuda-8.0 /opt/')
            sudo('ln -s /opt/cuda-8.0 /usr/local/cuda-8.0')
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
            sudo('python2.7 -m pip install --upgrade https://storage.googleapis.com/tensorflow/linux/gpu/tensorflow_gpu-' + tensorflow_version + '-cp27-none-linux_x86_64.whl')
            sudo('python3 -m pip install --upgrade https://storage.googleapis.com/tensorflow/linux/gpu/tensorflow_gpu-' + tensorflow_version + '-cp35-cp35m-linux_x86_64.whl')
            sudo('mkdir /var/log/tensorboard')
            put(files_dir + 'tensorboard-python2.service', '/tmp/tensorboard-python2.service')
            put(files_dir + 'tensorboard-python3.service', '/tmp/tensorboard-python3.service')
            sudo("chmod 644 /tmp/tensorboard-python*")
            sudo('\cp /tmp/tensorboard-python* /etc/systemd/system/')
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