#!/usr/bin/env bash

##################
# Project common
##################
PROJECT_PREFIX=docker.epmc-bdcc.projects.epam.com/dlab-aws
DOCKER_IMAGE=ssn
DOCKER_IMAGE_SOURCE_DIR=${0%/*}/../../src

##################
# Docker Common
##################
KEY_DIR=${0%/*}/keys
KEY_NAME=BDCC-DSS-POC
OVERWRITE_FILE=${0%/*}/overwrite.ini
REQUEST_ID=$RANDOM
LOG_DIR=$(pwd)

##################
# Internal vars
##################
REBUILD=false
REQUEST_ID=$RANDOM

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
    docker run -it \
        -v $KEY_DIR:/root/keys \
        -v $OVERWRITE_FILE:/root/conf/overwrite.ini \
        -v $RESPONSE_DIR:/response -e \
        "request_id=$REQUEST_ID" $PROJECT_PREFIX-$DOCKER_IMAGE --ACTION $1 | tee -a  $LOG_DIR/$REQUEST_ID_out.log
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
