#!/bin/bash

nodes=$NODEPORT

function gradleBroker(){

    cd weevent-broker
    gradle build -x test
}


function updateFisco(){
    cd dist
    ls
    cp ./conf/fisco.properties ./conf/fisco.properties.default
    # set the nodes
    sed -i "/nodes=/cnodes=${nodes}" ./conf/fisco.properties
}

# deploy contract and get the address
function getContractAddress(){
    # deploy contract
    chomod +x /weevent-broker/dist/deploy-topic-control.sh
    ./weevent-broker/dist/deploy-topic-control.sh 1
    # get address
    contractAddress=$(cat ./address.txt)
    # set topic-controller.address
    echo "contract_address:"$contractAddress
    sed -i "/topic-controller.address=/ctopic-controller.address=${contractAddress}" ./conf/fisco.properties
}

function checkService() {
    # start broker service
    ./broker.sh start
    ./check-service.sh
}

# clean up useless staff
function cleanup(){
    # reset the fisco
    rm -rf  ./conf/fisco.properties
    mv ./conf/fisco.properties.default ./conf/fisco.properties
}

function main(){
gradleBroker
updateFisco
getContractAddress
checkService
cleanup
}

main
