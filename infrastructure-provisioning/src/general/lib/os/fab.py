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
import logging
import os
import random
import sys
import string
import json, uuid, time, datetime
from dlab.meta_lib import *
from dlab.actions_lib import *


def ensure_pip(requisites):
    try:
        if not exists('/home/{}/.ensure_dir/pip_path_added'.format(os.environ['conf_os_user'])):
            sudo('echo PATH=$PATH:/usr/local/bin/:/opt/spark/bin/ >> /etc/profile')
            sudo('echo export PATH >> /etc/profile')
            sudo('pip install -U pip --no-cache-dir')
            sudo('pip install -U ' + requisites + ' --no-cache-dir')
            sudo('touch /home/{}/.ensure_dir/pip_path_added'.format(os.environ['conf_os_user']))
        return True
    except:
        return False


def create_aws_config_files(generate_full_config=False):
    try:
        aws_user_dir = os.environ['AWS_DIR']
        logging.info(local("rm -rf " + aws_user_dir+" 2>&1", capture=True))
        logging.info(local("mkdir -p " + aws_user_dir+" 2>&1", capture=True))

        with open(aws_user_dir + '/config', 'w') as aws_file:
            aws_file.write("[default]\n")
            aws_file.write("region = {}\n".format(os.environ['aws_region']))

        if generate_full_config:
            with open(aws_user_dir + '/credentials', 'w') as aws_file:
                aws_file.write("[default]\n")
                aws_file.write("aws_access_key_id = {}\n".format(os.environ['aws_access_key']))
                aws_file.write("aws_secret_access_key = {}\n".format(os.environ['aws_secret_access_key']))

        logging.info(local("chmod 600 " + aws_user_dir + "/*"+" 2>&1", capture=True))
        logging.info(local("chmod 550 " + aws_user_dir+" 2>&1", capture=True))

        return True
    except:
        return False


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def prepare_disk(os_user):
    if not exists('/home/' + os_user + '/.ensure_dir/disk_ensured'):
        try:
            sudo('''bash -c 'echo -e "o\nn\np\n1\n\n\nw" | fdisk /dev/xvdb' ''')
            sudo('mkfs.ext4 /dev/xvdb1')
            sudo('mount /dev/xvdb1 /opt/')
            sudo(''' bash -c "echo '/dev/xvdb1 /opt/ ext4 errors=remount-ro 0 1' >> /etc/fstab" ''')
            sudo('touch /home/' + os_user + '/.ensure_dir/disk_ensured')
        except:
            sys.exit(1)


def ensure_local_jars(os_user, s3_jars_dir, files_dir, region, templates_dir):
    if not exists('/home/' + os_user + '/.ensure_dir/s3_kernel_ensured'):
        try:
            if region == 'us-east-1':
                endpoint_url = 'https://s3.amazonaws.com'
            else:
                endpoint_url = 'https://s3-' + region + '.amazonaws.com'
            sudo('mkdir -p ' + s3_jars_dir)
            put(files_dir + 'notebook_local_jars.tar.gz', '/tmp/notebook_local_jars.tar.gz')
            sudo('tar -xzf /tmp/notebook_local_jars.tar.gz -C ' + s3_jars_dir)
            put(templates_dir + 'notebook_spark-defaults_local.conf', '/tmp/notebook_spark-defaults_local.conf')
            sudo("sed -i 's|URL|{}|' /tmp/notebook_spark-defaults_local.conf".format(endpoint_url))
            if os.environ['application'] == 'zeppelin':
                sudo('echo \"spark.jars $(ls -1 ' + s3_jars_dir + '* | tr \'\\n\' \',\')\" >> /tmp/notebook_spark-defaults_local.conf')
            sudo('\cp /tmp/notebook_spark-defaults_local.conf /opt/spark/conf/spark-defaults.conf')
            sudo('touch /home/' + os_user + '/.ensure_dir/s3_kernel_ensured')
        except:
            sys.exit(1)


def ensure_local_spark(os_user, spark_link, spark_version, hadoop_version, local_spark_path):
    if not exists('/home/' + os_user + '/.ensure_dir/local_spark_ensured'):
        try:
            sudo('wget ' + spark_link + ' -O /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz')
            sudo('tar -zxvf /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz -C /opt/')
            sudo('mv /opt/spark-' + spark_version + '-bin-hadoop' + hadoop_version + ' ' + local_spark_path)
            sudo('chown -R ' + os_user + ':' + os_user + ' ' + local_spark_path)
            sudo('touch /home/' + os_user + '/.ensure_dir/local_spark_ensured')
        except:
            sys.exit(1)


def prepare(emr_dir, yarn_dir):
    local('mkdir -p ' + emr_dir)
    local('mkdir -p ' + yarn_dir)
    local('sudo mkdir -p /opt/python/')
    result = os.path.exists(emr_dir + 'usr/')
    return result


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
    if args.region == 'us-east-1':
        endpoint_url = 'https://s3.amazonaws.com'
    else:
        endpoint_url = 'https://s3-' + args.region + '.amazonaws.com'
    local("""bash -c 'echo "spark.hadoop.fs.s3a.endpoint    """ + endpoint_url + """" >> """ + spark_def_path + """'""")


def configuring_notebook(emr_version):
    jars_path = '/opt/' + emr_version + '/jars/'
    local("""sudo bash -c "find """ + jars_path + """ -name '*netty*' | xargs rm -f" """)


def append_result(error):
    ts = time.time()
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    with open('/root/result.json', 'a+') as f:
        text = f.read()
    if len(text) == 0:
        res = '{"error": ""}'
        with open('/root/result.json', 'w') as f:
            f.write(res)
    with open("/root/result.json") as f:
        data = json.load(f)
    data['error'] = data['error'] + " [Error-" + st + "]:" + error
    with open("/root/result.json", 'w') as f:
        json.dump(data, f)
    print data


def put_resource_status(resource, status, dlab_path, os_user):
    env['connection_attempts'] = 100
    keyfile = "/root/keys/" + os.environ['conf_key_name'] + ".pem"
    hostname = get_instance_hostname(os.environ['conf_service_base_name'] + '-ssn')
    env.key_filename = [keyfile]
    env.host_string = os_user + '@' + hostname
    sudo('python ' + dlab_path + 'tmp/resource_status.py --resource {} --status {}'.format(resource, status))


def configure_jupyter(os_user, jupyter_conf_file, templates_dir):
    if not exists('/home/' + os_user + '/.ensure_dir/jupyter_ensured'):
        try:
            sudo('pip install jupyter --no-cache-dir')
            sudo('rm -rf ' + jupyter_conf_file)
            sudo('jupyter notebook --generate-config --config ' + jupyter_conf_file)
            sudo('echo "c.NotebookApp.ip = \'*\'" >> ' + jupyter_conf_file)
            sudo('echo c.NotebookApp.open_browser = False >> ' + jupyter_conf_file)
            sudo('echo \'c.NotebookApp.cookie_secret = b"' + id_generator() + '"\' >> ' + jupyter_conf_file)
            sudo('''echo "c.NotebookApp.token = u''" >> ''' + jupyter_conf_file)
            sudo('echo \'c.KernelSpecManager.ensure_native_kernel = False\' >> ' + jupyter_conf_file)
            put(templates_dir + 'jupyter-notebook.service', '/tmp/jupyter-notebook.service')
            sudo("chmod 644 /tmp/jupyter-notebook.service")
            if os.environ['application'] == 'tensor':
                sudo("sed -i 's|ExecStart=/bin/bash -c \"/usr/local/bin/jupyter notebook --config CONF_PATH\"|"
                     "ExecStart=/bin/bash -c \"export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/cudnn/lib64:/usr/local/cuda/lib64; "
                     "/usr/local/bin/jupyter notebook --config CONF_PATH\"|' /tmp/jupyter-notebook.service")
            sudo("sed -i 's|CONF_PATH|" + jupyter_conf_file + "|' /tmp/jupyter-notebook.service")
            sudo("sed -i 's|OS_USR|" + os_user + "|' /tmp/jupyter-notebook.service")
            sudo('\cp /tmp/jupyter-notebook.service /etc/systemd/system/jupyter-notebook.service')
            sudo('chown -R ' + os_user + ':' + os_user + ' /home/' + os_user + '/.local')
            sudo('mkdir /mnt/var')
            sudo('chown ' + os_user + ':' + os_user + ' /mnt/var')
            sudo("systemctl daemon-reload")
            sudo("systemctl enable jupyter-notebook")
            sudo("systemctl start jupyter-notebook")
            sudo('touch /home/' + os_user + '/.ensure_dir/jupyter_ensured')
        except:
            sys.exit(1)


def ensure_pyspark_local_kernel(os_user, pyspark_local_path_dir, templates_dir, spark_version):
    if not exists('/home/' + os_user + '/.ensure_dir/pyspark_local_kernel_ensured'):
        try:
            sudo('mkdir -p ' + pyspark_local_path_dir)
            sudo('touch ' + pyspark_local_path_dir + 'kernel.json')
            put(templates_dir + 'pyspark_local_template.json', '/tmp/pyspark_local_template.json')
            sudo(
                "PYJ=`find /opt/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; sed -i 's|PY4J|'$PYJ'|g' /tmp/pyspark_local_template.json")
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/pyspark_local_template.json')
            sudo('\cp /tmp/pyspark_local_template.json ' + pyspark_local_path_dir + 'kernel.json')
            sudo('touch /home/' + os_user + '/.ensure_dir/pyspark_local_kernel_ensured')
        except:
            sys.exit(1)


def ensure_py3spark_local_kernel(os_user, py3spark_local_path_dir, templates_dir, spark_version):
    if not exists('/home/' + os_user + '/.ensure_dir/py3spark_local_kernel_ensured'):
        try:
            sudo('mkdir -p ' + py3spark_local_path_dir)
            sudo('touch ' + py3spark_local_path_dir + 'kernel.json')
            put(templates_dir + 'py3spark_local_template.json', '/tmp/py3spark_local_template.json')
            sudo(
                "PYJ=`find /opt/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; sed -i 's|PY4J|'$PYJ'|g' /tmp/py3spark_local_template.json")
            sudo('sed -i "s|SP_VER|' + spark_version + '|g" /tmp/py3spark_local_template.json')
            sudo('\cp /tmp/py3spark_local_template.json ' + py3spark_local_path_dir + 'kernel.json')
            sudo('touch /home/' + os_user + '/.ensure_dir/py3spark_local_kernel_ensured')
        except:
            sys.exit(1)


def ensure_ciphers():
    sudo('echo -e "\nKexAlgorithms curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256" >> /etc/ssh/sshd_config')
    sudo('echo -e "Ciphers aes256-gcm@openssh.com,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr" >> /etc/ssh/sshd_config')
    sudo('echo -e "\tKexAlgorithms curve25519-sha256@libssh.org,diffie-hellman-group-exchange-sha256" >> /etc/ssh/ssh_config')
    sudo('echo -e "\tCiphers aes256-gcm@openssh.com,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr" >> /etc/ssh/ssh_config')
    sudo('systemctl reload sshd')


def installing_python(region, bucket, user_name, cluster_name):
    get_cluster_python_version(region, bucket, user_name, cluster_name)
    with file('/tmp/python_version') as f:
        python_version = f.read()
    python_version = python_version[0:5]
    if not os.path.exists('/opt/python/python' + python_version):
        local('wget https://www.python.org/ftp/python/' + python_version + '/Python-' + python_version + '.tgz -O /tmp/Python-' + python_version + '.tgz' )
        local('tar zxvf /tmp/Python-' + python_version + '.tgz -C /tmp/')
        with lcd('/tmp/Python-' + python_version):
            local('./configure --prefix=/opt/python/python' + python_version + ' --with-zlib-dir=/usr/local/lib/ --with-ensurepip=install')
            local('sudo make altinstall')
        with lcd('/tmp/'):
            local('sudo rm -rf Python-' + python_version + '/')
        local('sudo -i virtualenv /opt/python/python' + python_version)
        venv_command = '/bin/bash /opt/python/python' + python_version + '/bin/activate'
        pip_command = '/opt/python/python' + python_version + '/bin/pip' + python_version[:3]
        local(venv_command + ' && sudo -i ' + pip_command + ' install -U pip --no-cache-dir')
        local(venv_command + ' && sudo -i ' + pip_command + ' install ipython ipykernel --no-cache-dir')
        local(venv_command + ' && sudo -i ' + pip_command + ' install boto boto3 NumPy SciPy Matplotlib pandas Sympy Pillow sklearn --no-cache-dir')
        local('sudo rm -rf /usr/bin/python' + python_version[0:3])
        local('sudo ln -fs /opt/python/python' + python_version + '/bin/python' + python_version[0:3] +
              ' /usr/bin/python' + python_version[0:3])


def pyspark_kernel(kernels_dir, emr_version, cluster_name, spark_version, bucket, user_name, region):
    spark_path = '/opt/' + emr_version + '/' + cluster_name + '/spark/'
    local('mkdir -p ' + kernels_dir + 'pyspark_' + cluster_name + '/')
    kernel_path = kernels_dir + "pyspark_" + cluster_name + "/kernel.json"
    template_file = "/tmp/pyspark_emr_template.json"
    with open(template_file, 'r') as f:
        text = f.read()
    text = text.replace('CLUSTER_NAME', cluster_name)
    text = text.replace('SPARK_VERSION', 'Spark-' + spark_version)
    text = text.replace('SPARK_PATH', spark_path)
    text = text.replace('PYTHON_SHORT_VERSION', '2.7')
    text = text.replace('PYTHON_FULL_VERSION', '2.7')
    text = text.replace('PYTHON_PATH', '/usr/bin/python2.7')
    text = text.replace('EMR_VERSION', emr_version)
    with open(kernel_path, 'w') as f:
        f.write(text)
    local('touch /tmp/kernel_var.json')
    local(
        "PYJ=`find /opt/" + emr_version + "/" + cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
    local('sudo mv /tmp/kernel_var.json ' + kernel_path)
    get_cluster_python_version(region, bucket, user_name, cluster_name)
    with file('/tmp/python_version') as f:
        python_version = f.read()
    # python_version = python_version[0:3]
    if python_version != '\n':
        installing_python(region, bucket, user_name, cluster_name)
        local('mkdir -p ' + kernels_dir + 'py3spark_' + cluster_name + '/')
        kernel_path = kernels_dir + "py3spark_" + cluster_name + "/kernel.json"
        template_file = "/tmp/pyspark_emr_template.json"
        with open(template_file, 'r') as f:
            text = f.read()
        text = text.replace('CLUSTER_NAME', cluster_name)
        text = text.replace('SPARK_VERSION', 'Spark-' + spark_version)
        text = text.replace('SPARK_PATH', spark_path)
        text = text.replace('PYTHON_SHORT_VERSION', python_version[0:3])
        text = text.replace('PYTHON_FULL_VERSION', python_version[0:5])
        text = text.replace('PYTHON_PATH', '/opt/python/python' + python_version[:5] + '/bin/python' + python_version[:3])
        text = text.replace('EMR_VERSION', emr_version)
        with open(kernel_path, 'w') as f:
            f.write(text)
        local('touch /tmp/kernel_var.json')
        local(
            "PYJ=`find /opt/" + emr_version + "/" + cluster_name + "/spark/ -name '*py4j*.zip' | tr '\\n' ':' | sed 's|:$||g'`; cat " + kernel_path + " | sed 's|PY4J|'$PYJ'|g' > /tmp/kernel_var.json")
        local('sudo mv /tmp/kernel_var.json ' + kernel_path)


def configure_zeppelin_emr_interpreter(emr_version, cluster_name, region, spark_dir, os_user, yarn_dir, bucket, user_name, endpoint_url):
    try:
        port_number_found = False
        zeppelin_restarted = False
        default_port = 8998
        get_cluster_python_version(region, bucket, user_name, cluster_name)
        with file('/tmp/python_version') as f:
            python_version = f.read()
        python_version = python_version[0:5]
        livy_port = ''
        livy_path = '/opt/' + emr_version + '/' + cluster_name + '/livy/'
        spark_libs = "/opt/" + emr_version + "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-core*.jar /opt/" + \
                     emr_version + "/jars/usr/lib/hadoop/hadoop-aws*.jar /opt/" + emr_version + \
                     "/jars/usr/share/aws/aws-java-sdk/aws-java-sdk-s3-*.jar /opt/" + emr_version + \
                     "/jars/usr/lib/hadoop-lzo/lib/hadoop-lzo-*.jar"
        local('echo \"Configuring emr path for Zeppelin\"')
        local('sed -i \"s/^export SPARK_HOME.*/export SPARK_HOME=\/opt\/' + emr_version + '\/' +
              cluster_name + '\/spark/\" /opt/zeppelin/conf/zeppelin-env.sh')
        local('sed -i \"s/^export HADOOP_CONF_DIR.*/export HADOOP_CONF_DIR=\/opt\/' + emr_version + '\/' +
              cluster_name + '\/conf/\" /opt/' + emr_version + '/' + cluster_name +
              '/spark/conf/spark-env.sh')
        local('echo \"spark.jars $(ls ' + spark_libs + ' | tr \'\\n\' \',\')\" >> /opt/' + emr_version + '/' +
              cluster_name + '/spark/conf/spark-defaults.conf')
        local('echo \"spark.executorEnv.PYTHONPATH pyspark.zip:py4j-src.zip\" >> /opt/' + emr_version + '/' +
              cluster_name + '/spark/conf/spark-defaults.conf')
        local('sed -i \'/spark.yarn.dist.files/s/$/,file:\/opt\/' + emr_version + '\/' + cluster_name +
              '\/spark\/python\/lib\/py4j-src.zip,file:\/opt\/' + emr_version + '\/' + cluster_name +
              '\/spark\/python\/lib\/pyspark.zip/\' /opt/' + emr_version + '/' + cluster_name +
              '/spark/conf/spark-defaults.conf')
        local('sudo chown ' + os_user + ':' + os_user + ' -R /opt/zeppelin/')
        local('sudo systemctl daemon-reload')
        local('sudo service zeppelin-notebook stop')
        local('sudo service zeppelin-notebook start')
        while not zeppelin_restarted:
            local('sleep 5')
            result = local('sudo bash -c "nmap -p 8080 localhost | grep closed > /dev/null" ; echo $?', capture=True)
            result = result[:1]
            if result == '1':
                zeppelin_restarted = True
        local('sleep 5')
        local('echo \"Configuring emr spark interpreter for Zeppelin\"')
        while not port_number_found:
            port_free = local('sudo bash -c "nmap -p ' + str(default_port) + ' localhost | grep closed > /dev/null" ; echo $?', capture=True)
            port_free = port_free[:1]
            if port_free == '0':
                livy_port = default_port
                port_number_found = True
            else:
                default_port += 1
        local('sudo echo "livy.server.port = ' + str(livy_port) + '" >> ' + livy_path + 'conf/livy.conf')
        local('sudo echo "livy.spark.master = yarn" >> ' + livy_path + 'conf/livy.conf')
        local(''' sudo echo "export SPARK_HOME=''' + spark_dir + '''" >> ''' + livy_path + '''conf/livy-env.sh''')
        local(''' sudo echo "export HADOOP_CONF_DIR=''' + yarn_dir + '''" >> ''' + livy_path + '''conf/livy-env.sh''')
        local(''' sudo echo "export PYSPARK3_PYTHON=python''' + python_version[0:3] + '''" >> ''' +
              livy_path + '''conf/livy-env.sh''')
        template_file = "/tmp/emr_interpreter.json"
        fr = open(template_file, 'r+')
        text = fr.read()
        text = text.replace('CLUSTER_NAME', cluster_name)
        text = text.replace('SPARK_HOME', spark_dir)
        text = text.replace('ENDPOINTURL', endpoint_url)
        text = text.replace('LIVY_PORT', str(livy_port))
        fw = open(template_file, 'w')
        fw.write(text)
        fw.close()
        for _ in range(5):
            try:
                local("curl --noproxy localhost -H 'Content-Type: application/json' -X POST -d " +
                      "@/tmp/emr_interpreter.json http://localhost:8080/api/interpreter/setting")
                break
            except:
                local('sleep 5')
                pass
        local('sudo cp /opt/livy-server-cluster.service /etc/systemd/system/livy-server-' + str(livy_port) + '.service')
        local("sudo sed -i 's|OS_USER|" + os_user + "|' /etc/systemd/system/livy-server-" + str(livy_port) + '.service')
        local("sudo sed -i 's|LIVY_PATH|" + livy_path + "|' /etc/systemd/system/livy-server-" + str(livy_port)
              + '.service')
        local('sudo chmod 644 /etc/systemd/system/livy-server-' + str(livy_port) + '.service')
        local("sudo systemctl daemon-reload")
        local("sudo systemctl enable livy-server-" + str(livy_port))
        local('sudo systemctl start livy-server-' + str(livy_port))
        local('touch /home/' + os_user + '/.ensure_dir/emr_' + cluster_name + '_interpreter_ensured')
    except:
            sys.exit(1)

