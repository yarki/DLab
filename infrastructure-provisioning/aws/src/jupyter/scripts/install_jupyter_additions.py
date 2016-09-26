#!/usr/bin/python
from fabric.api import *
from fabric.contrib.files import exists
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--hostname', type=str, default='edge')
parser.add_argument('--keyfile', type=str, default='')
parser.add_argument('--additional_config', type=str, default='{"empty":"string"}')
args = parser.parse_args()


def ensure_matplot():
    if not exists('/tmp/matplot_ensured'):
        sudo('apt-get build-dep -y python-matplotlib')
        sudo('pip install matplotlib')
        sudo('touch /tmp/matplot_ensured')


def ensure_sbt():
    if not exists('/tmp/sbt_ensure'):
        sudo('apt-get install -y apt-transport-https')
        sudo('echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list')
        sudo('apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823')
        sudo('apt-get update')
        sudo('apt-get install -y sbt')
        sudo('touch /tmp/sbt_ensured')


def ensure_scala_breeze():
    if not exists('/tmp/scala_breeze_ensured'):
        working_root = "/tmp"
        breeze_repo = "https://github.com/scalanlp/breeze.git"
        releases = 'https://oss.sonatype.org/content/repositories/releases/'
        snapshots = 'https://oss.sonatype.org/content/repositories/snapshots/'
        scalanlp = 'org.scalanlp'
        scala_version = "2.11"
        sudo('cd ' + working_root + ' ; git clone ' + breeze_repo)
        sudo('echo \'libraryDependencies  ++= Seq(\' >> ' + working_root + '/breeze/build.sbt')
        sudo(
            'echo \'   "' + scalanlp + '" %% "breeze" % "latest.integration", \' >> ' + working_root + '/breeze/build.sbt ')
        sudo('echo \'   "' + scalanlp + '" %% "breeze-natives" % "0.12" , \' >> ' + working_root + '/breeze/build.sbt')
        sudo('echo \'   "' + scalanlp + '" %% "breeze-viz" % "0.12" ) \' >> ' + working_root + '/breeze/build.sbt ')
        sudo('echo \'resolvers ++= Seq( \' >> ' + working_root + '/breeze/build.sbt')
        sudo('echo \'  "Sonatype Releases" at "' + releases + '" , \' >> ' + working_root + '/breeze/build.sbt')
        sudo('echo \'  "Sonatype Snapshots" at "' + snapshots + '" ) \' >> ' + working_root + '/breeze/build.sbt')
        # sudo('echo \'scalaVersion := "' + scala_version + '" \' >> /tmp/breeze/build.sbt')
        sudo('cd ' + working_root + '/breeze ; sbt package publish-local')
        sudo('touch /tmp/scala_breeze_ensured')


def configure_scala_breeze():
    if not exists('/tmp/scala_breeze_configured'):
        source_root = "/tmp/breeze"
        target_dir = "/opt/spark/lib"
        scala_version = "2.11"
        sudo('cp ' + source_root + '/target/scala-' + scala_version + '/*.jar ' + target_dir)
        sudo('cp ' + source_root + '/math/target/scala-' + scala_version + '/*.jar ' + target_dir)
        sudo('cp ' + source_root + '/viz/target/scala-' + scala_version + '/*.jar ' + target_dir)
        sudo('touch /tmp/scala_breeze_configured')


def ensure_scala_wisp():
    if not exists('/tmp/scala_wisp_ensured'):
        wisp_repo = "https://github.com/quantifind/wisp.git"
        working_root = "/tmp"
        sudo('cd ' + working_root + ' ; git clone ' + wisp_repo)
        sudo('cd ' + working_root + '/wisp ; sbt package publish-local')
        sudo('touch /tmp/scala_wisp_ensured')


def configure_scala_wisp():
    if not exists('/tmp/scala_wisp_configured'):
        source_root = "/tmp/wisp"
        target_dir = "/opt/spark/lib"
        scala_version = "2.11"
        sudo('cp ' + source_root + '/core/target/scala-' + scala_version + '/*.jar ' + target_dir)
        sudo('touch /tmp/scala_wisp_configured')


##############
# Run script #
##############
if __name__ == "__main__":
    print "Configure connections"
    env['connection_attempts'] = 100
    env.key_filename = [args.keyfile]
    env.host_string = 'ubuntu@' + args.hostname
    deeper_config = json.loads(args.additional_config)

    print "Installing notebook additions: matplotlib."
    ensure_matplot()

    print "Installing notebook additions: sbt."
    ensure_sbt()

    print "Installing notebook additions: breeze."
    ensure_scala_breeze()
    configure_scala_breeze()

    print "Installing notebook additions: wisp."
    ensure_scala_wisp()
    configure_scala_wisp()

