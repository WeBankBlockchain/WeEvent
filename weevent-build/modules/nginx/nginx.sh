#!/bin/bash

current_path=`pwd`
nginxd=$current_path/sbin/nginx
nginx_config=$current_path/conf/nginx.conf
nginx_pid=$current_path/logs/nginx.pid
currentnginx_pid=

[ -x $nginxd ] || { echo "not exist nginx, [$nginxd]"; exit 1; }

[ -f $nginx_config ] || { echo "not exist nginx configuration, [$nginx_config]"; exit 1; }

getcurrent_pid(){
   if [ -e $nginx_pid ]; then
       file_pid=`cat $nginx_pid`
       currentnginx_pid=`ps aux|grep "nginx" | grep "$file_pid" | grep -v grep|awk '{print $2}'|head -1`
   fi
}

start(){
    getcurrent_pid;
    if [ -n "$currentnginx_pid" ]; then
        echo "nginx already running"
        exit 1
    else
        $nginxd -c $nginx_config
    fi
   
    sleep 1
    getcurrent_pid;
    if [ -z "$currentnginx_pid" ]; then
        echo "start nginx fail"
        exit 1
    else
        echo "start nginx success (PID=$currentnginx_pid)"
        if [ `crontab -l | grep -w nginx | wc -l` -eq 0 ]; then
            crontab -l > cron.backup
            echo "* * * * * cd `pwd`; ./nginx.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [ `crontab -l | grep -w nginx | wc -l` -gt 0 ]; then
            echo "add the crontab job success"
            exit 0
        else
            echo "add the crontab job fail"
            exit 1
        fi
    fi
}

stop(){
    getcurrent_pid;
    if [ -z "$currentnginx_pid" ]; then
        echo "nginx is not running"
        exit 1
    else     
        kill -QUIT `cat $nginx_pid`
        echo "stop nginx success"
    fi
    
    if [ `crontab -l | grep -w nginx | wc -l` -gt 0 ]; then
        crontab -l>cron.backup
        sed -i '/nginx/d' cron.backup
        crontab cron.backup
        rm cron.backup
    fi

    if [ `crontab -l | grep -w nginx | wc -l` -gt 0 ]; then
        echo "remove the crontab job fail"
        exit 1
    else
        echo "remove the crontab job success"
    fi
}

monitor(){
    getcurrent_pid;
    if [ -n "$currentnginx_pid" ]; then
        echo "`date`: nginx is running(PID=$currentnginx_pid)"
    else
        echo "`date`: nginx is not running,restart nginx row"
        ./nginx.sh start
    fi
}

if [ $# -lt 1 ]; then
    echo "Usage:"
    echo "     nginx.sh start|stop"
    exit 1
fi

#do operation
case "${1}" in
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