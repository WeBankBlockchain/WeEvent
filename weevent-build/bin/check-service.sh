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


    list_group_response=$(curl -s "http://127.0.0.1:8080/weevent/admin/listGroup")
    if [[ ${list_group_response} == {*success*} ]];then
        yellow_echo "broker service is ok"
    else
        yellow_echo "broker service is error"
    fi 
    
}

function check_governance(){
    echo "check governance service"
 curl -s  http://127.0.0.1:8080/weevent-governance/user/login -X POST -d "username=admin&password=AC0E7D037817094E9E0B4441F9BAE3209D67B02FA484917065F71B16109A1A78" | grep 'Authorization' >>/dev/null
    if [[ $? -eq 0 ]];then
        yellow_echo "governance service is ok"
    else
        yellow_echo "governance service is error"
    fi
 }
function check_processor(){
    echo "check processor service"
    curl -s  -d 'payload={\"a\":\"1\"}&condition=a<10' http://127.0.0.1:8080/processor/checkWhereCondition | grep "errorCode" >>/dev/null
    if [[ $? -eq 0 ]];then
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

