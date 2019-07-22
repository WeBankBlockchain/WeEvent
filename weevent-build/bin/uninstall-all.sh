#!/bin/bash
current_path=`pwd`

broker_pid_file=${current_path}/broker/logs/broker.pid
nginx_pid_file=${current_path}/nginx/logs/nginx.pid

function main(){
    cd ${current_path}
	
    if [[ -e ${current_path}/install-all.sh ]];then
        echo "[${current_path}] is source code package, please run uninstall.sh in installed path"
    	exit 1
    fi
	
    if [[ ! -e ${current_path}/start-all.sh ]] || [[ ! -d ${current_path}/broker ]] || [[ ! -d ${current_path}/nginx ]];then
        echo "[${current_path}] is not the installed path, skip"
        exit 1
    fi

    read -p "really want to uninstall WeEvent?[Y/N]" cmd_input
    if [[ "Y" != "${cmd_input}" && "y" != "$cmd_input" ]]; then
        echo "input ${cmd_input}, exit"
        exit 1
    fi

    if [[ -e ${broker_pid_file} ]]; then
        pid=`cat ${broker_pid_file}`
        current_broker_pid=`ps aux|grep "broker" | grep "${pid}" | grep -v grep | awk '{print $2}'`
    fi
     
    if [[ -e ${nginx_pid_file} ]]; then
       pid=`cat ${nginx_pid_file}`
       current_nginx_pid=`ps aux|grep "nginx" | grep "${pid}" | grep -v grep | awk '{print $2}'`
    fi

    if [[ -n "${current_broker_pid}" ]] || [[ -n "${current_nginx_pid}" ]];then
        read -p "WeEvent is running, stop it first? [Y/N]" cmd_input
        if [[ "Y" != "${cmd_input}" && "y" != "$cmd_input" ]]; then
            echo "input ${cmd_input}, exit"
            exit 1
        fi
        
        ./stop-all.sh
        if [[ $? -ne 0 ]];then
            echo "stop WeEvent fail, exit"
            exit 1
        fi	
    fi

    #uninstall;
    rm -rf ${current_path}
    if [[ $? -ne 0 ]];then
        echo "uninstall fail"
        exit 1
    fi
    echo "uninstall WeEvent success"
}

main