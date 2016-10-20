#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import sys

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()

scala_link = "http://www.scala-lang.org/files/archive/scala-2.11.8.deb"
spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-1.6.2-bin-hadoop2.6.tgz"
spark_version = "1.6.2"
hadoop_version = "2.6"
pyspark_local_path_dir = '/home/ubuntu/.local/share/jupyter/kernels/pyspark_local/'
py3spark_local_path_dir = '/home/ubuntu/.local/share/jupyter/kernels/py3spark_local/'
templates_dir = '/root/templates/'


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def ensure_spark_scala():
    if not exists('/home/ubuntu/spark_scala_ensured'):
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
            sudo('\cp /tmp/pyspark_local_template.json ' + pyspark_local_path_dir + 'kernel.json')
            sudo('\cp /tmp/py3spark_local_template.json ' + py3spark_local_path_dir + 'kernel.json')
            sudo('pip install --pre toree')
            sudo('ln -s /opt/spark/ /usr/local/spark')
            sudo('jupyter toree install')
            sudo('touch /home/ubuntu/spark_scala_ensured')
        except:
            sys.exit(1)


def ensure_python3_kernel():
    if not exists('/home/ubuntu/python3_kernel_ensured'):
        try:
            sudo('apt-get install python3-setuptools')
            sudo('apt install -y python3-pip')
            sudo('pip3 install ipython ipykernel')
            sudo('python3 -m ipykernel install')
            sudo('touch /home/ubuntu/python3_kernel_ensured')
        except:
            sys.exit(1)


def configure_notebook_server(notebook_name):
    try:
        # jupyter_password = id_generator()
        sudo('pip install jupyter')
        sudo('rm -rf /root/.jupyter/jupyter_notebook_config.py')
        sudo("for i in $(ps aux | grep jupyter | grep -v grep | awk '{print $2}'); do kill -9 $i; done")
        sudo('jupyter notebook --generate-config --config /root/.jupyter/jupyter_notebook_config.py')
        # sudo('echo "c.NotebookApp.password = \'' + jupyter_passwd(jupyter_password) +
        #     '\'" >> /root/.jupyter/jupyter_notebook_config.py')
        sudo('echo "c.NotebookApp.ip = \'*\'" >> /root/.jupyter/jupyter_notebook_config.py')
        sudo('echo c.NotebookApp.open_browser = False >> /root/.jupyter/jupyter_notebook_config.py')
        sudo('echo "c.NotebookApp.base_url = \'/' + notebook_name +
             '/\'" >> /root/.jupyter/jupyter_notebook_config.py')
        sudo('echo \'c.NotebookApp.cookie_secret = "' + id_generator() +
             '"\' >> /root/.jupyter/jupyter_notebook_config.py')
        # with open("/tmp/" + notebook_name + "passwd.file", 'wb') as f:
        #    f.write(jupyter_password)
    except:
        sys.exit(1)

    ensure_spark_scala()

    try:
        sudo("sleep 5; for i in $(ps aux | grep jupyter | grep -v grep | awk '{print $2}'); do kill -9 $i; done")
        sudo("sleep 5; screen -d -m jupyter notebook --config /root/.jupyter/jupyter_notebook_config.py; "
             "sleep 5;")
    except:
        sys.exit(1)

    ensure_python3_kernel()


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Configuring notebook server."
    configure_notebook_server("_".join(args.instance_name.split()))
