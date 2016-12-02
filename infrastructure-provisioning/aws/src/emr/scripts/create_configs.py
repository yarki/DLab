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

import boto3
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument('--bucket', type=str, default='')
parser.add_argument('--cluster_name', type=str, default='')
parser.add_argument('--dry_run', type=str, default='false')
parser.add_argument('--emr_version', type=str, default='')
parser.add_argument('--spark_version', type=str, default='')
parser.add_argument('--hadoop_version', type=str, default='')
parser.add_argument('--region', type=str, default='')
parser.add_argument('--excluded_lines', type=str, default='')
parser.add_argument('--user_name', type=str, default='')
args = parser.parse_args()

emr_dir = '/opt/' + args.emr_version + '/jars/'
kernels_dir = '/home/ubuntu/.local/share/jupyter/kernels/'
yarn_dir = '/opt/' + args.emr_version + '/' + args.cluster_name + '/conf/'
# if args.emr_version == 'emr-4.3.0' or args.emr_version == 'emr-4.6.0' or args.emr_version == 'emr-4.8.0':
#     hadoop_version = '2.6'
# else:1
#     hadoop_version = args.hadoop_version
# spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-" + args.spark_version + "-bin-hadoop" + hadoop_version + ".tgz"


def install_emr_spark(args):
    s3_client = boto3.client('s3')
    s3_client.download_file(args.bucket, args.user_name + '/' + args.cluster_name + '/spark.tar.gz', '/tmp/spark.tar.gz')
    local('sudo tar -zhxvf /tmp/spark.tar.gz -C /opt/' + args.emr_version + '/' + args.cluster_name + '/')


def prepare():
    local('mkdir -p ' + emr_dir)
    local('mkdir -p ' + yarn_dir)
    local('sudo mkdir -p /opt/python/')
    result = os.path.exists(emr_dir + 'usr/')
    return result


def jars(args):
    print "Downloading jars..."
    s3_client = boto3.client('s3', endpoint_url='https://s3-{}.amazonaws.com'.format(args.region))
    s3_client.download_file(args.bucket, 'jars/' + args.emr_version + '/jars.tar.gz', '/tmp/jars.tar.gz')
    local('tar -zhxvf /tmp/jars.tar.gz -C ' + emr_dir)


def yarn(args):
    print "Downloading yarn configuration..."
    s3client = boto3.client('s3', endpoint_url='https://s3-{}.amazonaws.com'.format(args.region))
    s3resource = boto3.resource('s3', endpoint_url='https://s3-{}.amazonaws.com'.format(args.region))
    get_files(s3client, s3resource, args.user_name + '/' + args.cluster_name + '/config/', args.bucket, yarn_dir)
    local('sudo mv ' + yarn_dir + args.user_name + '/' + args.cluster_name + '/config/* ' + yarn_dir)
    local('sudo rm -rf ' + yarn_dir + args.user_name + '/')


def pyspark_kernel(args):
    spark_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
    local('mkdir -p ' + kernels_dir + 'pyspark_' + args.cluster_name + '/')
    kernel_path = kernels_dir + "pyspark_" + args.cluster_name + "/kernel.json"
    template_file = "/tmp/pyspark_emr_template.json"
    with open(template_file, 'r') as f:
        text = f.read()
    text = text.replace('CLUSTER', args.cluster_name)
    text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
    text = text.replace('SPARK_PATH', spark_path)
    text = text.replace('PY_VER', '2.7')
    text = text.replace('EMR', args.emr_version)
    with open(kernel_path, 'w') as f:
        f.write(text)
    local('touch /tmp/kernel_var.json')
    local(
        "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
    local('sudo mv /tmp/kernel_var.json ' + kernel_path)
    s3_client = boto3.client('s3', endpoint_url='https://s3-{}.amazonaws.com'.format(args.region))
    s3_client.download_file(args.bucket, args.user_name + '/' + args.cluster_name + '/python_version', '/tmp/python_version')
    with file('/tmp/python_version') as f:
        python_version = f.read()
    # python_version = python_version[0:3]
    if python_version != '\n':
        installing_python(args)
        local('mkdir -p ' + kernels_dir + 'py3spark_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "py3spark_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/pyspark_emr_template.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('PY_VER', python_version[0:3])
        text = text.replace('PY_VER_FULL', python_version)
        text = text.replace('EMR', args.emr_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)


def toree_kernel(args):
    spark_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/'
    if args.emr_version == 'emr-4.3.0' or args.emr_version == 'emr-4.6.0' or args.emr_version == 'emr-4.8.0':
        local('mkdir -p ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "toree_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/toree_emr_template.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('EMR', args.emr_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)
    else:
        local('mkdir -p ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        local('tar zxvf /tmp/toree_kernel.tar.gz -C ' + kernels_dir + 'toree_' + args.cluster_name + '/')
        kernel_path = kernels_dir + "toree_" + args.cluster_name + "/kernel.json"
        template_file = "/tmp/toree_emr_templatev2.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + args.spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('EMR', args.emr_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + args.emr_version + "/" + args.cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)
        run_sh_path = kernels_dir + "toree_" + args.cluster_name + "/bin/run.sh"
        template_sh_file = '/tmp/run_template.sh'
        with open(template_sh_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER', args.cluster_name)
        with open(run_sh_path, 'w') as f:
            f.write(text)


def get_files(s3client, s3resource, dist, bucket, local):
    s3list = s3client.get_paginator('list_objects')
    for result in s3list.paginate(Bucket=bucket, Delimiter='/', Prefix=dist):
        if result.get('CommonPrefixes') is not None:
            for subdir in result.get('CommonPrefixes'):
                get_files(s3client, s3resource, subdir.get('Prefix'), bucket, local)
        if result.get('Contents') is not None:
            for file in result.get('Contents'):
                if not os.path.exists(os.path.dirname(local + os.sep + file.get('Key'))):
                     os.makedirs(os.path.dirname(local + os.sep + file.get('Key')))
                s3resource.meta.client.download_file(bucket, file.get('Key'), local + os.sep + file.get('Key'))


def spark_defaults(args):
    spark_def_path = '/opt/' + args.emr_version + '/' + args.cluster_name + '/spark/conf/spark-defaults.conf'
    for i in eval(args.excluded_lines):
        local(""" sudo bash -c " sed -i '/""" + i + """/d' """ + spark_def_path + """ " """)
    local(""" sudo bash -c " sed -i '/#/d' """ + spark_def_path + """ " """)
    local(""" sudo bash -c " sed -i '/^\s*$/d' """ + spark_def_path + """ " """)
    local(""" sudo bash -c "sed -i '/spark.driver.extraClassPath/,/spark.driver.extraLibraryPath/s|/usr|/opt/EMRVERSION/jars/usr|g' """ + spark_def_path + """ " """)
    local(""" sudo bash -c "sed -i '/spark.yarn.dist.files/s/\/etc\/spark\/conf/\/opt\/EMRVERSION\/CLUSTER\/conf/g' """ + spark_def_path + """ " """)
    template_file = spark_def_path
    with open(template_file, 'r') as f:
        text = f.read()
    text = text.replace('EMRVERSION', args.emr_version)
    text = text.replace('CLUSTER', args.cluster_name)
    with open(spark_def_path, 'w') as f:
        f.write(text)
    endpoint_url = 'https://s3-' + args.region + '.amazonaws.com'
    local("""bash -c 'echo "spark.hadoop.fs.s3a.endpoint    """ + endpoint_url + """" >> """ + spark_def_path + """'""")


def configuring_notebook(args):
    jars_path = '/opt/' + args.emr_version + '/jars/'
    local("""sudo bash -c "find """ + jars_path + """ -name '*netty*' | xargs rm -f" """)

def installing_python(args):
    s3_client = boto3.client('s3', endpoint_url='https://s3-{}.amazonaws.com'.format(args.region))
    s3_client.download_file(args.bucket, args.user_name + '/' + args.cluster_name + '/python_version', '/tmp/python_version')
    with file('/tmp/python_version') as f:
        python_version = f.read()
    if not exists('/opt/python/python' + python_version):
        local('sudo apt-get install -y build-essential checkinstall')
        local('sudo apt-get install -y libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev')
        local('sudo apt-get install -y libssl-dev openssl')
        local('sudo wget https://www.python.org/ftp/python/' + python_version + '/Python-' + python_version + '.tgz -O /tmp/Python-' + python_version + '.tgz' )
        local('sudo tar zxvf Python-' + python_version + '.tgz -C /tmp/')
        local('sudo cd /tmp/Python-' + python_version)
        local('sudo sudo ./configure --prefix=/opt/python/python' + python_version + ' --with-zlib-dir=/usr/local/lib/ --with-ensurepip=install')
        local('sudo make altinstall')
        local('sudo ln -s /opt/python/python' + python_version + '/bin/python' + python_version[0:3] + ' /usr/bin/')
        local('sudo cp /usr/bin/pip /usr/bin/pip' + python_version)
        local('''sudo sed -i 's|python|python''' + python_version + '''|g' /usr/bin/pip''' + python_version)
        local('sudo pip' + python_version + ' install -U pip --no-cache-dir')
        local('sudo pip' + python_version + ' install ipython ipykernel --no-cache-dir')
        local('sudo python' + python_version + ' -m ipykernel install')
        local('sudo pip' + python_version + ' install matplotlib --no-cache-dir')
        local('sudo pip' + python_version + ' install boto3 --no-cache-dir')
        local('sudo pip' + python_version + ' install NumPy SciPy Matplotlib pandas Sympy Pillow sklearn --no-cache-dir')


if __name__ == "__main__":
    if args.dry_run == 'true':
        parser.print_help()
    else:
        result = prepare()
        if result == False :
            jars(args)
        yarn(args)
        install_emr_spark(args)
        pyspark_kernel(args)
        toree_kernel(args)
        spark_defaults(args)
        configuring_notebook(args)