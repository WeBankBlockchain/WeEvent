#!/bin/bash

source ~/.bashrc
#check javajdk
function check_javajdk(){
    java_version=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}'`
    system_version=`cat /etc/os-release | awk -F'[= "]' '{print $3}' | head -1`
    oepnjdk=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $1 }'`
    if [[ ${java_version} -le 18 && "${system_version}" == "CentOS" && "${openjdk}" == "oepnjdk" ]];then
        echo "in CentOS, Open JDK's verison must be 1.9 or larger"
        exit -1
    fi
}
check_javajdk

pidfile=./logs/broker.pid
eventbroker_pid=
currentbroker_pid=

JAVA_OPTS="-Xverify:none -XX:TieredStopAtLevel=1 -Xms512m -Xmx2048m -XX:NewSize=256m -XX:MaxNewSize=1024m -XX:PermSize=128m -XX:+DisableExplicitGC"

getTradeProtalPID(){
    #eventbroker_pid=`ps aux|grep "broker" | grep "conf" | grep -v grep|awk '{print $2}'|head -1`
    if [ -e $pidfile ]; then
        eventbroker_pid=`cat $pidfile`
        currentbroker_pid=`ps aux|grep "broker" | grep "$eventbroker_pid" | grep -v grep|awk '{print $2}'|head -1`
    fi
}

start(){
    getTradeProtalPID;
	if [ -n "$currentbroker_pid" ];then
        echo "broker is running, (PID=$currentbroker_pid)"
    else
        nohup java ${JAVA_OPTS} -Xbootclasspath/a:./conf -jar ./apps/*  >/dev/null 2>&1 &
        sleep 3
        eventbroker_pid=$!
        if [ -n "$eventbroker_pid" ];then
            echo "start broker success (PID=$eventbroker_pid)"
            if [ -f $pidfile ]; then
                 echo "${eventbroker_pid}" > $pidfile
            else
                 touch $pidfile;
                 sleep 3
                 echo "${eventbroker_pid}" > $pidfile
            fi
           
            if [ `crontab -l | grep -w broker | wc -l` -eq 0 ]; then
                 crontab -l > cron.backup
                 echo "* * * * * cd `pwd`; ./broker.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
                 crontab cron.backup
                 rm cron.backup
            fi 

            if [ `crontab -l | grep -w broker | wc -l` -gt 0 ]; then
                 echo "add the crontab job success"
                 exit 0
            else
                 echo "add the crontab job fail"
                 exit 1
            fi
        else
            echo "start broker fail "
            exit 1
        fi
    fi
}

stop(){
    getTradeProtalPID;
    kill_cmd="kill -9 ${currentbroker_pid}"
    if [ -n "$currentbroker_pid" ];then
        eval ${kill_cmd}
        
        echo "stop broker success"
        if [ `crontab -l | grep -w broker | wc -l` -gt 0 ]; then
            crontab -l>cron.backup
            sed -i '/broker/d' cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [ `crontab -l | grep -w broker | wc -l` -gt 0 ]; then
            echo "remove the crontab job fail"
            exit 1
        else
            echo "remove the crontab job success"
        fi
    else
        echo "broker is not running "
        exit 1
    fi
}

monitor(){
    getTradeProtalPID
    if [ -n "$currentbroker_pid" ]; then
        echo "`date`: broker is running(PID=$eventbroker_pid)"
    else
        echo "`date`: broker is not running,restart broker now..."
        start
    fi   
}

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
    echo "    illegal param: $1"
    echo "Usage:"    
    echo "    ./broker.sh start|stop|monitor"
    ;;
esac
