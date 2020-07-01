#!/bin/bash

para=""

while [[ $# -ge 2 ]] ; do
    case "$1" in
    --out_path) para="$1 =$2";out_path="$2";shift 2;;
    --zookeeper_port) para="$1 =$2";zookeeper_port="$2";shift 2;;
    *) echo "unknow parameter $1." ; exit 1; break;;
    esac
done

configzookeeper(){
    sed -i '/nohup/s/\\/"-Dzookeeper.admin.enableServer=false" \\/g' ${out_path}/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    chmod +x ${out_path}/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    cp ${out_path}/apache-zookeeper-3.6.0-bin/conf/zoo_sample.cfg ${out_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    sed -i '/dataDir=/cdataDir='${out_path}/apache-zookeeper-3.6.0-bin/zk_data ${out_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    zookeeper_pre="clientPort="
    zookeeper_string=${zookeeper_pre}${zookeeper_port}
    sed -i 's/^clientPort=.*$/'$(echo ${zookeeper_pre}${zookeeper_port})'/' ${out_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
}

function copy_file(){
    mkdir -p ${out_path}
    cp -r ./* ${out_path}/
    rm -f ${out_path}/install-zookeeper.sh
}

copy_file
configzookeeper
