#!/bin/bash

echo "say hi"
ls
pwd

## update processor the config
    application_properties=./weevent-processor/src/main/resources/application-dev.properties
    brokerIp=127.0.0.1:7000

    if [[ -z ${brokerIp} ]];then
        echo "brokerIp is empty."
        echo "set brokerIp failed"
        exit 1
    else
       sed -i "/ci.broker.ip*/cci.broker.ip=${brokerIp}" ${application_properties}
    fi
    echo "set server_port success"

echo "say hi2"