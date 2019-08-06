#!/bin/bash 

pid_file=./logs/governance.pid
current_pid=

JAVA_OPTS="-Xverify:none -XX:TieredStopAtLevel=1 -Xms512m -Xmx2048m -XX:NewSize=256m -XX:MaxNewSize=1024m -XX:PermSize=128m -XX:+DisableExplicitGC"

get_pid(){
    if [[ -e ${pid_file} ]]; then
        pid=`cat ${pid_file}`
        current_pid=`ps aux|grep "governance" | grep "${pid}" | grep -v grep | awk '{print $2}'`
    fi
}

start(){
    get_pid
    if [[ -n "${current_pid}" ]];then
        echo "governance is running, (PID=${current_pid})"
        exit 0
    fi
    nohup java ${JAVA_OPTS} -Xbootclasspath/a:./conf:./html -Djava.security.egd=file:/dev/./urandom -jar ./apps/*  >/dev/null 2>&1 &
    i=0
    while :
    do
        sleep 1
        get_pid
        if [[ -n "${current_pid}" ]];then
            echo "start governance success (PID=${current_pid})"
            break
        fi

        if [[ i -eq 15 ]];then
            echo "start governance fail"
            exit 1
        fi
        i=$(( $i + 1 ))
    done

    if [[ `crontab -l | grep -w governance | wc -l` -eq 0 ]]; then
         crontab -l > cron.backup
         echo "* * * * * cd `pwd`; ./governance.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
         crontab cron.backup
         rm cron.backup
    fi

    if [[ `crontab -l | grep -w governance | wc -l` -gt 0 ]]; then
         echo "add the crontab job success"
         exit 0
    else
         echo "add the crontab job fail"
         exit 1
    fi
}

stop(){
    get_pid;
    kill_cmd="kill -9 ${current_pid}"
    if [[ -n "${current_pid}" ]];then
        eval ${kill_cmd}
      
        echo "stop governance success"
        if [[ `crontab -l | grep -w governance | wc -l` -gt 0 ]]; then
            crontab -l>cron.backup
            sed -i '/governance/d' cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [[ `crontab -l | grep -w governance | wc -l` -gt 0 ]]; then
            echo "remove the crontab job fail"
            exit 1
        else
            echo "remove the crontab job success"
        fi
    else
        echo "governance is not running"
        exit 1
    fi
}

monitor(){
    get_pid
    if [[ -n "${current_pid}" ]]; then
        echo "`date`: governance is running(PID=${current_pid})"
    else
        echo "`date`: governance is not running,restart governance now"
        start
    fi   
}

if [[ $# -lt 1 ]]; then
    echo "Usage:"
    echo "     governance.sh start|stop"
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
    echo "     governance.sh start|stop|monitor"
    ;;
esac
