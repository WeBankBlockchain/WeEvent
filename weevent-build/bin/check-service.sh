#!/bin/bash

function yellow_echo () {
    local what=$*
    if true;then
        echo -e "\e[1;33m${what} \e[0m"
    fi
}

# check broker
function check_broker(){
    echo "check broker service"

    existTopic=$(curl -s "http://127.0.0.1:8080/weevent/rest/exist?topic=hello")
    if [[ "${existTopic}" == "true" || "${existTopic}" == "false" ]];then
        yellow_echo "broker service is ok"
    else
        yellow_echo "broker service is error"
    fi 
    
}

function check_governance(){
    echo "check governance service"
    curl -s "http://127.0.0.1:8080/weevent-governance/topic/getTopics?pageIndex=0&pageSize=10" | grep 302000 >>/dev/null
    if [[ $? -eq 0 ]];then
        yellow_echo "governance service is ok"
    else
        yellow_echo "governance service is error"
    fi
 }
function check_processor(){
    echo "check processor service"
    result= $(curl -H "Content-type: application/json" -X POST -d '{"id":"1","ruleName":"airCondition","status":"0"}' http://127.0.0.1:8080/processor/insert)
    if [[ "${result}" == 0 || "${result.errorCode}" == 1 ]];then
        yellow_echo "processor service is ok"
    else
        yellow_echo "processor service is error"
    fi
 }


check_broker
if [[ -d "governance" ]]; then
    check_governance
fi

if [[ -d "processor" ]]; then
    check_processor
fi

