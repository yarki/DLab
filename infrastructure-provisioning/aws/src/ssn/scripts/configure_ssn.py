#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json
import random
import string
import crypt

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def id_generator(size=10, chars=string.digits + string.ascii_letters):
    return ''.join(random.choice(chars) for _ in range(size))


def ensure_nginx():
    if not exists('/tmp/nginx_ensured'):
        sudo('apt-get -y install nginx')
        sudo('service nginx restart')
        sudo('sysv-rc-conf nginx on')
        sudo('touch /tmp/nginx_ensured')


def configure_nginx(config):
    random_file_part = id_generator(size=20)
    if not exists("/etc/nginx/conf.d/nginx_proxy.conf"):
        sudo('rm -f /etc/nginx/conf.d/*')
        put(config['nginx_template_dir'] + 'nginx_proxy.conf', '/tmp/nginx_proxy.conf')
        sudo('\cp /tmp/nginx_proxy.conf /etc/nginx/conf.d/')
        sudo('mkdir -p /etc/nginx/locations')
        sudo('rm -f /etc/nginx/sites-enabled/default')

    if not exists("/etc/nginx/locations/proxy_location_jenkins.conf"):
        nginx_password = id_generator()
        template_file = config['nginx_template_dir'] + 'proxy_location_jenkins_template.conf'
        with open("/tmp/%s-tmpproxy_location_jenkins_template.conf" % random_file_part, 'w') as out:
            with open(template_file) as tpl:
                for line in tpl:
                    out.write(line)
        put("/tmp/%s-tmpproxy_location_jenkins_template.conf" % random_file_part, '/tmp/proxy_location_jenkins.conf')
        sudo('\cp /tmp/proxy_location_jenkins.conf /etc/nginx/locations/')
        sudo("echo 'engineer:" + crypt.crypt(nginx_password, id_generator()) + "' > /etc/nginx/htpasswd")
        with open('jenkins_crids.txt', 'w+') as f:
            f.write("Jenkins credentials: engineer  / " + nginx_password)

    sudo('service nginx reload')


def place_notebook_automation_scripts():
    sudo('mkdir -p /usr/share/notebook_automation')
    sudo('chown -R ubuntu /usr/share/notebook_automation')

    local('scp -r -i %s /usr/share/notebook_automation/* %s:/usr/share/notebook_automation/'
          % (args.keyfile, env.host_string))

    sudo('chmod a+x /usr/share/notebook_automation/scripts/*')
    sudo('chmod +x /usr/share/notebook_automation/fabfile_notebook.py')
    sudo('ln -s /usr/share/notebook_automation/fabfile_notebook.py /usr/local/bin/provision_notebook_server.py')
    sudo('ln -s /usr/share/notebook_automation/scripts/create_cluster.py /usr/local/bin/create_cluster.py')
    sudo('ln -s /usr/share/notebook_automation/scripts/terminate_aws_resources.py '
         '/usr/local/bin/terminate_aws_resources.py')
    sudo('echo export PROVISION_CONFIG=/usr/share/notebook_automation/conf/conf.ini >> /etc/profile')


def ensure_jenkins():
    if not exists('/tmp/jenkins_ensured'):
        sudo('wget -q -O - https://pkg.jenkins.io/debian/jenkins-ci.org.key | apt-key add -')
        sudo('echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list')
        sudo('apt-get -y update')
        with settings(warn_only=True):
            sudo('apt-get -y install jenkins')
        sudo('touch /tmp/jenkins_ensured')


def configure_jenkins():
    if not exists('/tmp/jenkins_configured'):
        sudo('echo \'JENKINS_ARGS="--prefix=/jenkins"\' >> /etc/default/jenkins')
        sudo('rm -rf /var/lib/jenkins/*')
        sudo('mkdir -p /var/lib/jenkins/jobs/')
        sudo('chown -R ubuntu /var/lib/jenkins/jobs/')
        put('/usr/share/notebook_automation/templates/jenkins_jobs/*', '/var/lib/jenkins/jobs/')
        sudo('chown -R jenkins:jenkins /var/lib/jenkins/jobs')
        with settings(warn_only=True):
            sudo('/etc/init.d/jenkins start; sleep 5; /etc/init.d/jenkins restart; sleep 10')
            sudo('sysv-rc-conf jenkins on')
        sudo('touch /tmp/jenkins_configured')


def configure_proxy_server(config):
    if not exists('/tmp/proxy_ensured'):
        with settings(warn_only=True):
            sudo('apt-get -y install squid')
        template_file = config['squid_template_file']
        proxy_port = config['proxy_port']
        proxy_subnet = config['proxy_subnet']
        with open("/tmp/tmpsquid.conf", 'w') as out:
            with open(template_file) as tpl:
                for line in tpl:
                    out.write(line.replace('PROXY_SUBNET', proxy_subnet)
                              .replace('PROXY_PORT', proxy_port))
        put('/tmp/tmpsquid.conf', '/tmp/squid.conf')
        sudo('\cp /tmp/squid.conf /etc/squid/squid.conf')
        sudo('service squid reload')
        sudo('sysv-rc-conf squid on')
        sudo('touch /tmp/proxy_ensured')




##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing nginx as frontend."
    ensure_nginx()

    print "Configuring nginx."
    configure_nginx(deeper_config)

    print "Installing proxy for notebooks."
    configure_proxy_server(deeper_config)

    print "Installing jenkins."
    ensure_jenkins()

    print "Configuring jenkins."
    configure_jenkins()

    print "Uploading notebook creation and configuration core."
    place_notebook_automation_scripts()
