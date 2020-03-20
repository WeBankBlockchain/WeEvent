#!/bin/bash

para=""
current_path=$(pwd)

while [[ $# -ge 2 ]] ; do
    case "$1" in
    --out_path) para="$1 =$2";out_path="$2";shift 2;;
    --zookeeper_port) para="$1 =$2";zookeeper_port="$2";shift 2;;
    *) echo "unknow parameter $1." ; exit 1; break;;
    esac
done

configzookeeper(){
    sed -i '158s#\\#"-Dzookeeper.admin.enableServer=false" \\#' ${current_path}/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    chmod +x ${current_path}/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    cp ${current_path}/apache-zookeeper-3.6.0-bin/conf/zoo_sample.cfg ${current_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    sed -i '$a\dataDir=/tmp/zk_data' ${current_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    sed -i '$a\dataLogDir=/tmp/zk_logs' ${current_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    zookeeper_pre="clientPort="
    zookeeper_string=${zookeeper_pre}${zookeeper_port}
    sed -i 's/^clientPort=.*$/'$(echo ${zookeeper_pre}${zookeeper_port})'/' ${current_path}/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
}

function copy_file(){
    mkdir -p ${out_path}
    cp -r ./* ${out_path}/
    rm -f ${out_path}/install-zookeeper.sh
}

configzookeeper
copy_file
