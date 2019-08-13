#!/bin/bash
echo "source ~/.bashrc to confirm java jdk environment"
source ~/.bashrc >/dev/null 2>&1

#check java jdk, not support openjdk 1.8 in CentOS
function check_java_jdk(){
    java -version >>/dev/null 2>&1
    if [[ $? -ne 0 ]];then
        echo "not installed JDK"
        exit 1
    fi
    java_version=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}'`
    system_version=`cat /etc/os-release | awk -F'[= "]' '{print $3}' | head -1`
    openjdk=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $1 }'`
    if [[ ${java_version} -le 18 && "${system_version}" == "CentOS" && "${openjdk}" == "openjdk" ]];then
        echo "in CentOS, OpenJDK's version must be 1.9 or greater"
        exit 1
    fil
}
check_java_jdk

pid_file=./logs/robust.pid
current_pid=l

JAVA_OPTS="-Xverify:none -XX:TieredStopAtLevel=1 -Xms512m -Xmx2048m -XX:NewSize=256m -XX:MaxNewSize=1024m -XX:PermSize=128m -XX:+DisableExplicitGC"

get_pid(){
    if [[ -e ${pid_file} ]]; then
        pid=`cat ${pid_file}`
        current_pid=`ps aux|grep "robust" | grep "${pid}" | grep -v grep | awk '{print $2}'`
    fi
}

start(){
    get_pid
	if [[ -n "${current_pid}" ]];then
        echo "robust is running, (PID=${current_pid})"
        exit 0
    fi
    nohup java ${JAVA_OPTS} -Xbootclasspath/a:./conf -jar ./apps/*  >/dev/null 2>&1 &
    i=0
    while :
    do
        sleep 1
        get_pid
        if [[ -n "${current_pid}" ]];then
            echo "start robust success (PID=${current_pid})"
            break
        fi

        if [[ i -eq 15 ]];then
            echo "start robust fail"
            exit 1
        fi
        i=$(( $i + 1 ))
    done

    if [[ `crontab -l | grep -w robust | wc -l` -eq 0 ]]; then
         crontab -l > cron.backup
         echo "* * * * * cd `pwd`; ./robust.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
         crontab cron.backup
         rm cron.backup
    fi

    if [[ `crontab -l | grep -w robust | wc -l` -gt 0 ]]; then
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
        
        echo "stop robust success"
        if [[ `crontab -l | grep -w robust | wc -l` -gt 0 ]]; then
            crontab -l>cron.backup
            sed -i '/robust/d' cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [[ `crontab -l | grep -w robust | wc -l` -gt 0 ]]; then
            echo "remove the crontab job fail"
            exit 1
        else
            echo "remove the crontab job success"
        fi
    else
        echo "robust is not running"
        exit 1
    fi
}

monitor(){
    getTradeProtalPID
    if [[ -n "${current_pid}" ]]; then
        echo "`date`: robust is running(PID=${current_pid})"
    else
        echo "`date`: robust is not running, restart robust now..."
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
    echo "    illegal param: $1"
    echo "Usage:"    
    echo "    ./robust.sh start|stop|monitor"
    ;;
esac
