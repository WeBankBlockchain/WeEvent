#!/bin/bash

current_path=$(pwd)

nginxd=${current_path}/sbin/nginx
nginx_config=${current_path}/conf/nginx.conf
pid_file=${current_path}/logs/nginx.pid
current_pid=

[[ -x ${nginxd} ]] || { echo "not exist nginx, [${nginxd}]"; exit 1; }

[[ -f ${nginx_config} ]] || { echo "not exist nginx configuration, [${nginx_config}]"; exit 1; }

get_current_pid(){
   if [[ -e ${pid_file} ]]; then
       pid=$(cat ${pid_file})
       current_pid=$(ps aux | grep "nginx" | grep "${pid}" | grep -v grep | awk '{print $2}')
   fi
}

start(){
    get_current_pid
    if [[ -n "${current_pid}" ]]; then
        echo "nginx already running"
        exit 0
    fi
    ${nginxd} -c ${nginx_config}
    i=0
    while :
    do
        sleep 1
        get_current_pid
        if [[ -n "${current_pid}" ]]; then
            echo "start nginx success (PID=${current_pid})"
            break
        fi

        if [[ i -eq 15 ]]; then
            echo "start nginx fail"
            exit 1
        fi
        i=$(( $i + 1 ))
    done

    if [[ $(crontab -l | grep -w nginx | wc -l) -eq 0 ]]; then
        crontab -l > cron.backup
        echo "* * * * * cd $(pwd); ./nginx.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
        crontab cron.backup
        rm cron.backup
    fi

    if [[ $(crontab -l | grep -w nginx | wc -l) -gt 0 ]]; then
        echo "add the crontab job success"
        exit 0
    else
        echo "add the crontab job fail"
        exit 1
    fi
}

stop(){
    get_current_pid
    if [[ -z "${current_pid}" ]]; then
        echo "nginx is not running"
        exit 1
    else     
        kill -QUIT $(cat ${pid_file})
        echo "stop nginx success"
    fi
    
    if [[ $(crontab -l | grep -w nginx | wc -l) -gt 0 ]]; then
        crontab -l>cron.backup
        sed -i '/nginx/d' cron.backup
        crontab cron.backup
        rm cron.backup
    fi

    if [[ $(crontab -l | grep -w nginx | wc -l) -gt 0 ]]; then
        echo "remove the crontab job fail"
        exit 1
    else
        echo "remove the crontab job success"
    fi
}

monitor(){
    get_current_pid
    if [[ -n "${current_pid}" ]]; then
        echo "$(date): nginx is running(PID=${current_pid})"
    else
        echo "$(date): nginx is not running, restart nginx now..."
        ./nginx.sh start
    fi
}

if [[ $# -lt 1 ]]; then
    echo "Usage:"
    echo "     nginx.sh start|stop"
    exit 1
fi

# command list
case "$1" in
start)
    start
    ;;
stop)
    stop
    ;;
monitor)
    monitor
    ;;
*)
    echo "     illegal param: $1"
    echo "Usage:"   
    echo "     ./nginx.sh start|stop|monitor"
    ;;
esac