#!/usr/bin/env bash
pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd -P`
popd > /dev/null

##################
# Project common
##################
PROJECT_PREFIX=docker.epmc-bdcc.projects.epam.com/dlab-aws
DOCKER_IMAGE=ssn
DOCKER_IMAGE_SOURCE_DIR=$SCRIPTPATH/../../src

##################
# Docker Common
##################
ACTION="create"
KEY_DIR=$SCRIPTPATH/keys
KEY_NAME=BDCC-DSS-POC
OVERWRITE_FILE=$SCRIPTPATH/overwrite.ini
REQUEST_ID=$RANDOM
LOG_DIR=$(pwd)

##################
# Internal vars
##################
REBUILD=false
REQUEST_ID=$RANDOM
RESPONSE_DIR=$SCRIPTPATH

##################
# Routines
##################
function update_images {
    echo "Updating base image"
    docker build --file $DOCKER_IMAGE_SOURCE_DIR/base/Dockerfile $PROJECT_PREFIX-base $DOCKER_IMAGE_SOURCE_DIR/base

    echo "Updating working image"
    docker build --file $DOCKER_IMAGE_SOURCE_DIR/$DOCKER_IMAGE/Dockerfile $PROJECT_PREFIX-$DOCKER_IMAGE $DOCKER_IMAGE_SOURCE_DIR
}

function run_docker {
    echo docker run -it \
        -v $KEY_DIR:/root/keys \
        -v $OVERWRITE_FILE:/root/conf/overwrite.ini \
        -v $RESPONSE_DIR:/response -e \
        "request_id=$REQUEST_ID" $PROJECT_PREFIX-$DOCKER_IMAGE --action $ACTION
    docker run -it \
        -v $KEY_DIR:/root/keys \
        -v $OVERWRITE_FILE:/root/conf/overwrite.ini \
        -v $RESPONSE_DIR:/response -e \
        "request_id=$REQUEST_ID" $PROJECT_PREFIX-$DOCKER_IMAGE --action $ACTION | tee -a  $LOG_DIR/$REQUEST_ID_out.log
}

function print_help {
    echo "REUIRED:"
    echo "-a / --action ACTION: pass command to container"
    echo "OPTIONAL:"
    echo "-l / --log-dir DIR: response and log directory. Default: current dir (pwd)"
    echo "-o / --overwrite-file PATH_TO_FILE: path to overwrite conf file"
    echo "-d / --key-dir DIR: path to key dir"
    echo "-k / --key-name NAME: name of infra key. By default: BDCC-DSS-POC"
    echo "-s / --source-dir DIR: direcotry with dlab infrastucture provisioning sources"
    echo "--response_dir DIR: direcotry where response json will be places"
    echo "--rebuild : if you need to refresh images before run"
}

while [[ $# -gt 1 ]]
do
    key="$1"

    case $key in
        -a|--action)
        ACTION="$2"
        shift # past argument
        ;;
        -s|--source-dir)
        DOCKER_IMAGE_SOURCE_DIR="$2"
        shift # past argument
        ;;
        -k|--key-name)
        KEY_NAME="$2"
        shift # past argument
        ;;
        -l|--log-dir)
        LOG_DIR="$2"
        shift # past argument
        ;;
        --response_dir)
        RESPONSE_DIR="$2"
        shift # past argument
        ;;
        -d|--key-dir)
        KEY_DIR="$2"
        shift # past argument
        ;;
        -r|--rebuild)
        REBUILD=true
        ;;
        -h|--help)
        print_help
        exit
        ;;
        *)
        echo "Unkonown option $1."
        print_help
        exit
        ;;
    esac
    shift
done

if [ "$REBUILD" = "true" ]
then
    update_images
fi

run_docker
