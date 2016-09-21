# 
# Spark installation script for DataScience project
# Version : 0.0.1
#

from fabric.api import env,hosts,run,execute    

host_file="./hosts"
spark_version="1.6.2"
hadoop_version="2.6"

#env.user='root'
#env.hosts = ['ds1','ds2']
env.hosts = open(host_file, 'r').readlines()

# Prepares the directories
def prepare_spark():
    run('mkdir /opt/spark')
    
# Downloads SPARK tgz
def download_spark():
    run('wget http://xenia.sote.hu/ftp/mirrors/www.apache.org/spark/spark-'+spark_version+'/spark-'+spark_version+'-bin-hadoop'+hadoop_version+'.tgz -O /tmp/spark-'+spark_version+'.tgz')
    
# Unpacks and moves SPARK to the final directory    
def unpack_spark():
    run('tar -zxf /tmp/spark-'+spark_version+'.tgz -C /opt/ ')
    run('mv /opt/spark-'+spark_version+'-bin-hadoop'+hadoop_version+'/  /opt/spark')
    
# Complete SPARK installation    
def install_spark():
    prepare_spark()
    download_spark()
    unpack_spark()