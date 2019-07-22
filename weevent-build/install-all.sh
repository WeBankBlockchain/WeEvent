#!/bin/bash
#
# install WeEvent service, support FISCO-BCOS 1.3 and 2.0
#
################################################################################

out_path=""
block_chain_version=
block_chain_channel=
block_chain_node_path=
broker_port=8090

nginx_port=8080

governance=
governance_port=
mysql_ip=
mysql_port=
mysql_user=
mysql_password=

current_path=$PWD

function yellow_echo (){
    local what=$*
    if true;then
        echo -e "\e[1;33m${what} \e[0m"
    fi
}

function error_message(){
    local message=$1
    echo "ERROR - ${message}"
    exit 1
}

function properties_get(){
    local file="config.properties"
    local param=$1
    value=`grep -v '#' ${file} | grep ${param} | cut -d'=' -f2 | sed 's/\r//' | awk '$1=$1'`
    if [[ -z "$value" ]]; then
        echo "error_message"
        error_message "ERROR config.properties get  param $param failed."
        exit 1
    fi
    echo ${value}
}

function set_global_param(){
    block_chain_version=$(properties_get "fisco-bcos.version")
    block_chain_channel=$(properties_get "fisco-bcos.channel")
    block_chain_node_path=$(properties_get  "fisco-bcos.node_path")
    if [[ "${block_chain_node_path:0:1}" == "~" ]];then
        block_chain_node_path=`realpath -m ${HOME}/${block_chain_node_path:1}`
    else
        block_chain_node_path=`realpath -m ${block_chain_node_path}`
    fi
    nginx_port=$(properties_get "nginx.port")
    
    broker_port=$(properties_get "broker.port")
    
    governance_enable=$(properties_get  "governance.enable")
    governance_port=$(properties_get "governance.port")

    mysql_ip=$(properties_get "governance.mysql.ip")
    mysql_port=$(properties_get  "governance.mysql.port")
    mysql_user=$(properties_get "governance.mysql.user")
    mysql_password=$(properties_get "governance.mysql.password")
}

function check_port(){
    lsof -i:${1}
    if [[ $? -eq 1 ]];then
        echo "$1 port is okay"
    else 
        echo "$1 port is in used"
        exit 1
    fi
}

function check_param(){
    if [[ -d ${block_chain_node_path} ]]; then
        check_port ${broker_port}
        check_port ${nginx_port}
        echo "param ok"
    else
        echo "path not exist, ${block_chain_node_path}"
        exit 1;
    fi
}

### check the result and print it
function check_result(){
    if [[ $? -eq 0 ]];then
        yellow_echo "$1 success"
    else
        yellow_echo "$1 failed, exit"
        exit 1;
   fi
}

function install_module(){
    yellow_echo "install module broker"
    cd ${current_path}/modules/broker
    ./install-broker.sh --out_path ${out_path}/broker --listen_port ${broker_port} --block_chain_node_path ${block_chain_node_path} --channel_info ${block_chain_channel} --version ${block_chain_version}
    check_result "install broker"

    yellow_echo "install module nginx"
    cd ${current_path}/modules/nginx
    ./install-nginx.sh --nginx_path ${out_path}/nginx --nginx_port ${nginx_port} --broker_port ${broker_port} --governance_port ${governance_port} &>> ${current_path}/install.log
    check_result "install nginx"

    if [[ ${governance_enable} = "true" ]];then
        yellow_echo "install module governance"
        cd ${current_path}/modules/governance
        ./install-governance.sh --out_path ${out_path}/governance --server_port ${governance_port} --broker_port ${broker_port} --mysql_ip ${mysql_ip} --mysql_port ${mysql_port} --mysql_user ${mysql_user} --mysql_pwd ${mysql_password} &>> ${current_path}/install.log
        check_result "install governance"
    fi
}

function update_check_server(){
    sed -i "s/8080/$nginx_port/g" check-service.sh
}

function main(){
    # confirm
    if [[ -d $2 ]]; then
        read -p "$2 already exist, continue? [Y/N]" cmd_input
        if [[ "Y" != "$cmd_input" ]]; then
            echo "input $cmd_input, install skipped"
            exit 1
        fi
    fi
    mkdir -p $2
    if [[ $? -ne 0 ]];then
        echo "create path $2 fail !!! "
        exit 1
    fi
    out_path=`realpath $2`

    # set the params
    set_global_param

    # check the dir is exist or not
    check_param

    # set the check service port
    update_check_server

    # install module
    install_module

    cd ${current_path}
    cp start-all.sh check-service.sh stop-all.sh uninstall-all.sh ${out_path}
}

# Usage message
if [[ $# -lt 2 ]]; then
    echo "Usage:"
    echo "    ./install-all.sh -p target_install_path"
    exit 1
fi

main $1 $2
