#!/usr/bin/env bash

# ***************************************************************************
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
# ***************************************************************************

BUCKET_NAME=$1
EMR_VERSION=$2
REGION=$3
SPARK_DEF_PATH="/usr/lib/spark/conf/spark-defaults.conf"
SPARK_DEF_PATH_LINE1=`cat $SPARK_DEF_PATH | grep spark.driver.extraClassPath | awk '{print $2}' | sed 's/^:// ; s~jar:~jar ~g; s~/\*:~/\* ~g; s~:~/\* ~g'`
SPARK_DEF_PATH_LINE2=`cat $SPARK_DEF_PATH | grep spark.driver.extraLibraryPath | awk '{print $2}' | sed 's/^:// ; s~jar:~jar ~g; s~/\*:~/\* ~g; s~:\|$~/\* ~g'`
touch /tmp/python_version
PYTHON_VER=`which python3.5 | sed 's/\/usr\/bin\/python//'`
if [ -n "$PYTHON_VER" ]
then
 echo $PYTHON_VER > /tmp/python_version
else
 PYTHON_VER=`which python3.4 | sed 's/\/usr\/bin\/python//'`
 echo $PYTHON_VER > /tmp/python_version
fi
/bin/tar -zhcvf /tmp/jars.tar.gz --no-recursion --absolute-names --ignore-failed-read /usr/lib/hadoop/* $SPARK_DEF_PATH_LINE1 $SPARK_DEF_PATH_LINE2 /usr/lib/hadoop/client/*
aws s3 cp /tmp/jars.tar.gz s3://$BUCKET_NAME/jars/$EMR_VERSION/ --endpoint-url https://s3-$REGION.amazonaws.com --region $REGION
aws s3 cp $SPARK_DEF_PATH s3://$BUCKET_NAME/ --endpoint-url https://s3-$REGION.amazonaws.com --region $REGION
aws s3 cp /tmp/python_version s3://$BUCKET_NAME/ --endpoint-url https://s3-$REGION.amazonaws.com --region $REGION
