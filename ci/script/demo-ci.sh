#!/bin/bash

echo "say hi"
ls
pwd
brokerIp=${BROKER_INFO}
nodes=${FISCOBCOS_NODE_INFO}

echo ${brokerIp}
echo ${nodes}

echo "==============================================="
  ## broker update the certificate
  #rm -rf  ./weevent-broker/src/main/resources/v2/*
  #mv ./ci/certificate/*  ./weevent-broker/src/main/resources/v2/

  ## update processor the config
    application_properties=./weevent-processor/src/main/resources/application-dev.properties
    #brokerIp=127.0.0.1:7000

    if [[ -z ${brokerIp} ]];then
        echo "brokerIp is empty."
        echo "set brokerIp failed"
        exit 1
    else
       sed -i "/ci.broker.ip*/cci.broker.ip=${brokerIp}" ${application_properties}
    fi
    echo "set brokerIp success"

	cat ./weevent-processor/src/main/resources/application-dev.properties


  ## update the governance
#	application_properties=./weevent-governance/src/main/resources/application-dev.properties
#    if [[ -z ${brokerIp} ]];then
#        echo "brokerIp is empty."
#        echo "set brokerIp failed"
#        exit 1
#    else
#       sed -i "/ci.broker.ip*/cci.broker.ip=${brokerIp}" ${application_properties}
#    fi
#    echo "set brokerIp success"
#	cat ./weevent-governance/src/main/resources/application-dev.properties
#
#  ## update the broker ip
#	application_properties=./weevent-broker/src/main/resources/application-dev.properties
#    if [[ -z ${brokerIp} ]];then
#        echo "brokerIp is empty."
#        echo "set brokerIp failed"
#        exit 1
#    else
#       sed -i "/ci.broker.ip*/cci.broker.ip=${brokerIp}" ${application_properties}
#    fi
#    echo "set brokerIp success"
#
#	cat ./weevent-governance/src/main/resources/application-dev.properties
#
#  ## update the broker fisco ip
#  fisco_properties=./weevent-broker/src/main/resources/fisco.properties
#
#    if [[ -z ${nodes} ]];then
#        echo "nodes is empty."
#        echo "set nodes failed"
#        exit 1
#    else
#       sed -i "/nodes*/cnodes=${nodes}" ${fisco_properties}
#    fi
#    echo "set nodes success"
#
#	cat ./weevent-broker/src/main/resources/fisco.properties
echo "say hi2"