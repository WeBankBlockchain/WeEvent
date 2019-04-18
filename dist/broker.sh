#!/bin/bash
current_path=`pwd`
brokerpid_path=$current_path/logs/broker.pid
eventbroker_pid=
currentbroker_pid=

JAVA_OPTS=" -Xmx2048m -Xms512m -XX:NewSize=256m -XX:MaxNewSize=1024m -XX:+DisableExplicitGC"

getTradeProtalPID(){
    #eventbroker_pid=`ps aux|grep "broker" | grep "conf" | grep -v grep|awk '{print $2}'|head -1`
    if [ -e $brokerpid_path ]; then
        eventbroker_pid=`cat $brokerpid_path`
        currentbroker_pid=`ps aux|grep "broker" | grep "$eventbroker_pid" | grep -v grep|awk '{print $2}'|head -1`
    fi
}

start(){
    getTradeProtalPID;
	if [ -n "$currentbroker_pid" ];then
        echo "broker is running, (PID=$currentbroker_pid)"
    else
        nohup java ${JAVA_OPTS} -Xbootclasspath/a:./conf -jar ./apps/*  >/dev/null 2>&1 &
        sleep 1
        eventbroker_pid=$!
        if [ -n "$eventbroker_pid" ];then
            echo "start broker success (PID=$eventbroker_pid)"
            if [ -f $brokerpid_path ]; then 
                 echo "${eventbroker_pid}" >$brokerpid_path
            else
                 touch $brokerpid_path;
                 sleep 1
                 echo "${eventbroker_pid}" >$brokerpid_path
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
        ./broker.sh start
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
    echo "     illegal param: $1"
    echo "Usage:"    
    echo "     ./broker.sh start|stop|monitor"
    ;;
esac