#!/bin/bash
JAVA_HOME=
APP_PARAMS="-Xbootclasspath/a:./conf:./html -Djava.security.egd=file:/dev/./urandom -cp ./apps/* -Dloader.path=./lib,../lib org.springframework.boot.loader.PropertiesLauncher"

if [[ -z ${JAVA_HOME} ]];then
   echo "JAVA_HOME is empty, please set it first"
   exit 1
fi

###############################################################################
# The following is common logic for start a java application. DO NOT EDIT IT SOLELY.
###############################################################################
JAVA_OPTS="-Xverify:none -XX:+DisableExplicitGC -XX:TieredStopAtLevel=1 -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection"


server_name=$(basename $0|awk -F"." '{print $1}')
pid_file=./logs/${server_name}.pid
current_pid=
#operating system total physical memory, unit MB.
max_total_memory=2048

get_pid(){
    if [[ -e ${pid_file} ]]; then
        pid=$(cat ${pid_file})
        current_pid=$(ps aux|grep "${server_name}" | grep "${pid}" | grep -v grep | awk '{print $2}')
    fi
}

start(){
    get_pid
    if [[ -n "${current_pid}" ]];then
        echo "${server_name} is running, (PID=${current_pid})"
        exit 0
    fi

    total_memory=$(free -m | grep "Mem" | awk '{ print $2 }')
    if [[ "${total_memory}" -ge "${max_total_memory}" ]];then
        JAVA_OPTS+=" -Xms2048m -Xmx2048m -Xmn1024m -XX:MetaspaceSize=128M"
    fi
    
    nohup ${JAVA_HOME}/bin/java ${JAVA_OPTS} ${APP_PARAMS} >/dev/null 2>&1 &
    i=0
    while :
    do
        sleep 1
        get_pid
        if [[ -n "${current_pid}" ]];then
            echo "start ${server_name} success (PID=${current_pid})"
            break
        fi

        if [[ i -eq 15 ]];then
            echo "start ${server_name} fail"
            exit 1
        fi
        i=$(( i + 1 ))
    done

    if [[ $(crontab -l | grep -w ${server_name} | wc -l) -eq 0 ]]; then
         crontab -l > cron.backup
         echo "* * * * * cd $(pwd); ./${server_name}.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
         crontab cron.backup
         rm cron.backup
    fi

    if [[ $(crontab -l | grep -w ${server_name} | wc -l) -gt 0 ]]; then
         echo "add the crontab job success"
         exit 0
    else
         echo "add the crontab job fail"
         exit 1
    fi
}

stop(){
    get_pid
    kill_cmd="kill -9 ${current_pid}"
    if [[ -n "${current_pid}" ]];then
        eval ${kill_cmd}

        echo "stop ${server_name} success"
        if [[ $(crontab -l | grep -w ${server_name} | wc -l) -gt 0 ]]; then
            crontab -l>cron.backup
            sed -i '/'${server_name}'/d' cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [[ $(crontab -l | grep -w ${server_name} | wc -l) -gt 0 ]]; then
            echo "remove the crontab job fail"
            exit 1
        else
            echo "remove the crontab job success"
        fi
    else
        echo "${server_name} is not running"
        exit 1
    fi
}

monitor(){
    get_pid
    if [[ -n "${current_pid}" ]]; then
        echo "$(date): ${server_name} is running(PID=${current_pid})"
    else
        echo "$(date): ${server_name} is not running, restart ${server_name} now"
        start
    fi   
}

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
    echo "Usage:"
    echo "    ${server_name}.sh start|stop|monitor"
    ;;
esac
