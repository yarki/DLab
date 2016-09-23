#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
from notebook.auth import passwd as jupyter_passwd
import random
import string

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='')
parser.add_argument('--instance_name', type=str, default='')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def ensure_spark_scala():
    if not exists('/tmp/spark_scala_ensured'):
        sudo('apt-get install -y default-jre')
        sudo('apt-get install -y default-jdk')
        sudo('apt-get install -y scala')
        spark_link = "http://d3kbcqa49mib13.cloudfront.net/spark-1.6.2-bin-hadoop2.6.tgz"
        spark_version = "1.6.2"
        hadoop_version = "2.6"
        sudo('wget ' + spark_link + ' -O /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz')
        sudo('tar -zxvf /tmp/spark-' + spark_version + '-bin-hadoop' + hadoop_version + '.tgz -C /opt/')
        sudo('mv /opt/spark-' + spark_version + '-bin-hadoop' + hadoop_version + ' /opt/spark')
        sudo('mkdir -p /home/ubuntu/.local/share/jupyter/kernels/pyspark_local')
        sudo('touch /home/ubuntu/.local/share/jupyter/kernels/pyspark_local/kernel.json')
        local('cp /root/templates/pyspark_emr_template.json /tmp/pyspark_emr_template.json')
        local('cp /root/templates/spark-defaults_template.conf /tmp/spark-defaults_template.conf')
        local('cp /root/templates/pyspark_local_template.json /tmp/pyspark_local_template.json')
        local('cp /root/templates/create_configs.py /tmp/create_configs.py')
        put('/tmp/pyspark_emr_template.json', '/tmp/pyspark_emr_template.json')
        put('/tmp/spark-defaults_template.conf', '/tmp/spark-defaults_template.conf')
        put('/tmp/pyspark_local_template.json', '/tmp/pyspark_local_template.json')
        sudo('\cp /tmp/pyspark_local_template.json /home/ubuntu/.local/share/jupyter/kernels/pyspark_local/kernel.json')
        put('/tmp/create_configs.py', '/tmp/create_configs.py')
        sudo('\cp /tmp/create_configs.py /usr/local/bin/create_configs.py')
        sudo('chmod 755 /usr/local/bin/create_configs.py')
        sudo('touch /tmp/spark_scala_ensured')


def configure_notebook_server(notebook_name):
    jupyter_password = id_generator()
    sudo('pip install jupyter')
    sudo('rm -rf /root/.jupyter/jupyter_notebook_config.py')
    sudo("for i in $(ps aux | grep jupyter | grep -v grep | awk '{print $2}'); do kill -9 $i; done")
    sudo('jupyter notebook --generate-config --config /root/.jupyter/jupyter_notebook_config.py')
    sudo('echo "c.NotebookApp.password = \'' + jupyter_passwd(jupyter_password) +
         '\'" >> /root/.jupyter/jupyter_notebook_config.py')
    sudo('echo "c.NotebookApp.ip = \'*\'" >> /root/.jupyter/jupyter_notebook_config.py')
    sudo('echo c.NotebookApp.open_browser = False >> /root/.jupyter/jupyter_notebook_config.py')
    sudo('echo "c.NotebookApp.base_url = \'/' + notebook_name +
         '/\'" >> /root/.jupyter/jupyter_notebook_config.py')
    sudo('echo \'c.NotebookApp.cookie_secret = "' + id_generator() +
         '"\' >> /root/.jupyter/jupyter_notebook_config.py')
    with open("/tmp/" + notebook_name + "passwd.file", 'wb') as f:
        f.write(jupyter_password)

    ensure_spark_scala()

    sudo("sleep 5; for i in $(ps aux | grep jupyter | grep -v grep | awk '{print $2}'); do kill -9 $i; done")
    sudo("sleep 5; screen -d -m jupyter notebook --config /root/.jupyter/jupyter_notebook_config.py; "
         "sleep 5;")


def configure_nginx(config, instnace_name):
    random_file_part = id_generator(size=20)
    template_file = config['nginx_template_dir'] + 'proxy_location_notebook_template.conf'
    backend_hostname = config['backend_hostname']
    backend_port = config['backend_port']
    notebook_uri = "_".join(instnace_name.split())
    with open("/tmp/%s-proxy_location_notebook_template.conf" % random_file_part, 'w') as out:
        with open(template_file) as tpl:
            for line in tpl:
                out.write(line.replace('BACKEND_HOSTNAME', backend_hostname)
                          .replace('BACKEND_PORT', backend_port).replace('NOTEBOOK', notebook_uri))
            put("/tmp/%s-proxy_location_notebook_template.conf" % random_file_part,
                '/tmp/proxy_location_notebook_' + "".join(instnace_name.split()) + '.conf')
            sudo('\cp /tmp/proxy_location_notebook_' + "".join(instnace_name.split()) + '.conf /etc/nginx/locations/')
    sudo('service nginx restart')


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

    env.host_string = 'ubuntu@' + deeper_config['frontend_hostname']
    print "Preparing nginx proxy for notebook server."
    configure_nginx(deeper_config, args.instance_name)