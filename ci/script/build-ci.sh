#!/bin/bash

nodes="127.0.0.1:20230"

function updatefisco(){
    cp ./weevent-broker/conf/fisco.properties ./weevent-broker/conf/fisco.properties.default
    # set the nodes
    sed -i "/nodes=/cnodes=${nodes}" ./weevent-broker/conf/fisco.properties
}

# deploy contract and get the address
function getContractAddress(){
    # deploy contract
    ./weevent-broker/deploy-contract.sh
    # get address
    contractAddress=$(cat ./address.txt)
    # set topic-controller.address
    echo "contract_address:"$contractAddress
    sed -i "/topic-controller.address=/ctopic-controller.address=${contractAddress}" ./weevent-broker/conf/fisco.properties
}

function checkService() {
    # start broker service
    ./weevent-broker/broker.sh start
    ./weevent-broker/check-service.sh
}

# clean up useless staff
function cleanup(){
    # reset the fisco
    rm -rf  ./weevent-broker/conf/fisco.properties
    mv ./weevent-broker/conf/fisco.properties.default ./weevent-broker/conf/fisco.properties
}

function main(){
updatefisco
getContractAddress
checkService
cleanup
}

main