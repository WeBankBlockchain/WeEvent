#!/bin/bash 

pidfile=./logs/governance.pid
eventgovernance_pid=
currentgovernance_pid=

JAVA_OPTS="-Xverify:none -XX:TieredStopAtLevel=1 -Xms512m -Xmx2048m -XX:NewSize=256m -XX:MaxNewSize=1024m -XX:PermSize=128m -XX:+DisableExplicitGC"

getTradeProtalPID(){
    if [ -e $pidfile ]; then
        eventgovernance_pid=`cat $pidfile`
        currentgovernance_pid=`ps aux|grep "governance" | grep "$eventgovernance_pid" | grep -v grep|awk '{print $2}'|head -1`
    fi
}

start(){
    getTradeProtalPID;
    if [ -n "$currentgovernance_pid" ];then
        echo "governance is running, (PID=$currentgovernance_pid)"
    else

        nohup java ${JAVA_OPTS} -Xbootclasspath/a:./conf:./html -Djava.security.egd=file:/dev/./urandom -jar ./apps/*  >/dev/null 2>&1 &
        sleep 3
        eventgovernance_pid=$!
        if [ -n "$eventgovernance_pid" ];then
            echo "start governance success (PID=$eventgovernance_pid)"
            if [ -f $pidfile ]; then
                 echo "${eventgovernance_pid}" > $pidfile
            else
                 touch $pidfile;
                 sleep 3
                 echo "${eventgovernance_pid}" > $pidfile
            fi
            
            if [ `crontab -l | grep -w governance | wc -l` -eq 0 ]; then
                 crontab -l > cron.backup
                 echo "* * * * * cd `pwd`; ./governance.sh monitor >> ./logs/monitor.log 2>&1" >> cron.backup
                 crontab cron.backup
                 rm cron.backup
            fi

            if [ `crontab -l | grep -w governance | wc -l` -gt 0 ]; then
                 echo "add the crontab job success"
                 exit 0
            else
                 echo "add the crontab job fail"
                 exit 1
            fi
        else
            echo "start governance fail"
            exit 1
        fi
    fi
}

stop(){
    getTradeProtalPID;
    kill_cmd="kill -9 ${currentgovernance_pid}"
    if [ -n "$currentgovernance_pid" ];then
        eval ${kill_cmd}
      
        echo "stop governance success"
        if [ `crontab -l | grep -w governance | wc -l` -gt 0 ]; then
            crontab -l>cron.backup
            sed -i '/governance/d' cron.backup
            crontab cron.backup
            rm cron.backup
        fi

        if [ `crontab -l | grep -w governance | wc -l` -gt 0 ]; then
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
    getTradeProtalPID
    if [ -n "$currentgovernance_pid" ]; then
        echo "`date`: governance is running(PID=$eventgovernance_pid)"
    else
        echo "`date`: governance is not running,restart governance now"
        start
    fi   
}

if [ $# -lt 1 ]; then
    echo "Usage:"
    echo "     governance.sh start|stop"
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
    echo "     governance.sh start|stop|monitor"
    ;;
esac
