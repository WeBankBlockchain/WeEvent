#!/bin/bash

current_path=$(pwd)
java_home_path=$(echo ${JAVA_HOME})

function updateOpenssl(){
    # update openssl version
    echo "updateOpenssl"
    cd ~
    sudo apt-get install -y openssl curl
    sudo apt-get update && sudo apt-get upgrade -y
    sudo apt-get install gcc make -y
    curl https://ftp.openssl.org/source/old/1.0.2/openssl-1.0.2j.tar.gz | tar xz && cd openssl-1.0.2j && sudo ./config && sudo make && sudo make install
    sudo ln -sf /usr/local/ssl/bin/openssl /usr/bin/openssl
    openssl version
}

function installFisco(){
    #install and start fisco bcos
    cd ~ && mkdir -p fisco && cd fisco
    curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.2.0/build_chain.sh && chmod u+x build_chain.sh
    curl -LO https://www.fisco.com.cn/cdn/fisco-bcos/releases/download/v2.2.0/fisco-bcos.tar.gz && tar -zxf fisco-bcos.tar.gz
    bash build_chain.sh -l "127.0.0.1:4" -e ./fisco-bcos -p 30300,20200,8545 -i
    bash nodes/127.0.0.1/start_all.sh

    #copy file
    cp ${HOME}/fisco/nodes/127.0.0.1/sdk/* ${current_path}/weevent-broker/src/main/resources/
    cp ${HOME}/fisco/nodes/127.0.0.1/sdk/* ${current_path}/weevent-core/src/main/resources/
}

function installZookeeper(){
    #unzip file
    cd ${current_path}/weevent-build/modules/zookeeper/
    tar -zxf apache-zookeeper-3.6.0-bin.tar.gz

    #modify configuration
    sed -i '158s#\\#"-Dzookeeper.admin.enableServer=false" \\#' ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    chmod +x ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/bin/zkServer.sh
    cp ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/conf/zoo_sample.cfg ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    sed -i '$a\dataDir=/tmp/zk_data' ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/conf/zoo.cfg
    sed -i '$a\dataLogDir=/tmp/zk_logs' ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/conf/zoo.cfg

    #start zookeeper
    cd ${current_path}/weevent-build/modules/zookeeper/apache-zookeeper-3.6.0-bin/bin/
    ./zkServer.sh start
}

function gradleBuild(){
    cd ${current_path}
    ./gradlew build -x test
}

# deploy contract and get the address
function deployContract(){
    # modify configuration
    cd ${current_path}/weevent-broker/dist/
    if [[ -e deploy-topic-control.sh ]];then
        sed -i "/JAVA_HOME=/cJAVA_HOME=${java_home_path}" deploy-topic-control.sh
    fi
    # deploy contract
    chmod +x deploy-topic-control.sh
    ./deploy-topic-control.sh
}

function startBrokerService() {
    # modify configuration
    cd ${current_path}/weevent-broker/dist/
    if [[ -e broker.sh ]];then
        sed -i "/JAVA_HOME=/cJAVA_HOME=${java_home_path}" broker.sh
    fi

    # open mqtt port
    sed -i "s/#mqtt.broker.port/mqtt.broker.port/g" ./conf/weevent.properties
    sed -i "s/#mqtt.broker.tcp.port/mqtt.broker.tcp.port/g" ./conf/weevent.properties

    # start broker service
    chmod +x broker.sh
    ./broker.sh start
}

function main(){
    updateOpenssl
    installFisco
    installZookeeper
    gradleBuild
    deployContract
    startBrokerService
}

main