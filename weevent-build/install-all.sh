#!/bin/bash
#
# install weevent service, support FISCO-BCOS 1.3 and 2.0
#
################################################################################

out_path=""
block_chain_version=
block_chain_channel=
block_chain_node_path=
broker_port=8081

nginx_port=8080

governance=
governance_port=
mysql_ip=
mysql_port=
mysql_user=
mysql_password=

installPWD=$PWD

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

function ini_get(){
    local file="config.ini"
    local section=$1
    local param=$2
    local no_exit=$3
    local value=$($installPWD/build/crudini-0.9/crudini --get $file $section $param 2> /dev/null)
    if [ $? -ne 0 ];then
        if [ "${no_exit}" = "true" ];then
            #{ echo >&2 "ERROR - ini config get failed, section is $section param is $param."; exit 1; }
            echo "no_exit"
        else
            echo "error_message"
            error_message "ERROR - ini config get failed, section is $section param is $param."
        fi
    fi

    echo "$value"
}

function set_global_param(){
    block_chain_version=$(ini_get "fisco-bcos" "version")
    block_chain_rpc=$(ini_get "fisco-bcos" "channel")
    block_chain_node_path=$(ini_get  "fisco-bcos" "node_path")
    block_chain_node_path=`realpath $web3sdk_conf_path`

    nginx_port=$(ini_get "nginx" "port")
    
    broker_port=$(ini_get  "broker" "port")
    
    governance_enable=$(ini_get  "governance" "enable")
    governance_port=$(ini_get "governance" "port")

    mysql_ip=$(ini_get "governance" "mysql_ip")
    mysql_port=$(ini_get  "governance" "mysql_port")
    mysql_user=$(ini_get "governance" "mysql_user")
    mysql_password=$(ini_get "governance" "mysql_password")
}

function check_port(){
    lsof -i:${1} &>> $installPWD/install.log
    
    if [ $? -eq 1 ]
    then 
        echo "$1 port is okay"  &>> $installPWD/install.log
    else 
        echo "$1 port is use" 
        exit 1
    fi
}

function check_param(){
    if [ -d $web3sdk_conf_path ]; then
        check_port $broker_port
        check_port $nginx_port
        echo "param ok" &>> $installPWD/install.log
    else
        echo "path not exist, $web3sdk_conf_path"
        exit 1;
    fi
}

### check the result and print it
function check_result(){
    if [[ $? -eq 0 ]];then
        yellow_echo $1
    else
        exit 1;
   fi
}

### set the module and params
function set_module(){
    params="--out_path $out_path --listen_port $broker_port --block_chain_node_path $block_chain_node_path --channel_info $block_chain_channel --version $block_chain_version"
    
    yellow_echo $params &>> $installPWD/install.log
    cd $installPWD/modules/broker
    ./install-broker.sh $params
    check_result "broker install success"

    nginxpath=$out_path"/nginx"
    params="--nginx_path $nginxpath --nginx_port $nginx_port --broker_port $broker_port --governance_port $governance_port"

    cd $installPWD/modules/nginx
    ./install-nginx.sh $params
    check_result "nginx install success"

    if $governance_enable;then 
        governancepath=$out_path"/governance"
        params="--out_path $governancepath --server_port $governance_port --broker_port $broker_port --mysql_ip $mysql_ip --mysql_port $mysql_port  --mysql_user $mysql_user --mysql_pwd $mysql_password"

        yellow_echo "$params" &>> $installPWD/install.log
        cd $installPWD/modules/governance
        ./install-governance.sh $params
        check_result "governance install success"
    fi
    
    cd $installPWD 
}

function install_crudini(){
    echo "install crudini" &>> $installPWD/install.log
    mkdir -p $installPWD/build/
    tar -zxf $installPWD/third-packages/crudini-0.9.tar.gz -C $installPWD/build/  
       
    $installPWD/build/crudini-0.9/crudini --version &>> $installPWD/install.log
 
    sleep 1
    if [ $? -ne 0 ];then
        echo "install crudini failed, skip"
        exit 1
    fi
}

function update_check_server(){
    sed -i "s/8080\/weevent/$broker_port\/weevent/" check-service.sh
    sed -i "s/8082\/weevent-governance/$governance_port\/weevent-governance/" check-service.sh
}

function main(){
    #crudini
    install_crudini

    # set the params
    set_global_param
    # check the dir is exist or not
    check_param

    # confirm
    if [ -d $2 ]; then
        read -p "$2 already exist, continue? [Y/N]" cmd_input
        if [ "Y" != "$cmd_input" ]; then
            echo "input $cmd_input, install skipped"
            exit 1
        fi
    fi
    mkdir -p $2
    if [ $? -ne 0 ];then
        echo "create dir $2 fail !!! "
        exit 1
    fi
    out_path=$2
    
    if [[ $2 == .* ]];then
       out_path=`cd $2; pwd`
    fi
    
    # set module and the params
    set_module
    # set the check service port
    update_check_server

    cp start-all.sh $out_path
    cp stop-all.sh $out_path
    cp check-service.sh $out_path
    cp uninstall-all.sh $out_path
}

# Usage message
if [ $# -lt 2 ]; then
    echo "Usage:"
    echo "    ./install-all.sh -p install_path"
    exit 1
fi

main $1 $2
