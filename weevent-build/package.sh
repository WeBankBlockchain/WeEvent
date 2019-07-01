#!/bin/bash
# generate WeEvent-x.x.x.tar.gz package from github project.
# depend internet online and tools as followings:
# 1. git
# 2. gradle 4.10
# 3. java 1.8
# 4. npm 10.16
################################################################################

version=""
tag="master"

current_path=`pwd`
top_path=`dirname ${current_path}`
out_path=""

while [ $# -ge 2 ] ; do
    case "$1" in
    --tag) param="$1 = $2;";tag=$2;shift 2;;
    --version) param="$1 = $2;";version="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

function usage(){
    echo "Usage:"
    echo "    package master: ./package.sh --version 1.0.0"
    echo "    package tag: ./package.sh --tag v1.0.0 --version 1.0.0"
}

function yellow_echo(){
    local what=$*
    if true;then
        echo -e "\e[1;33m${what} \e[0m"
    fi
}

function execute_result(){
    if [ $? -ne 0 ];then
        echo "$1 fail"
        exit 1
    fi
}

# confirm whether to override input path
function confirm(){
    if [ -d $1 ]; then
        read -p "$out_path already exist, continue? [Y/N]" cmd_input
        if [ "Y" != "$cmd_input" ]; then
            echo "input $cmd_input, install skipped"
            exit 1
        fi
    fi
}

# chmod & dos2unix
function set_permission(){
    cd ${out_path}

    find -name "*.sh" -exec chmod +x {} \;
    find -name "*.sh" -exec dos2unix {} \;
    find -name "*.ini" -exec dos2unix {} \;
    find -name "*.properties" -exec dos2unix {} \;
}

#gradle build broker, governance, client, web
function build_weevent(){
    cd ${top_path}

    #switch tag
    git checkout ${tag}
    execute_result "git checkout ${tag}"

    #npm build html and css
    cd ${top_path}/weevent-governance/web
    ./build-web.sh

    #gradle build
    gradle clean build -x test
    execute_result "build weevent"
}

function copy_install_file(){
    cd ${current_path}

    cp ./README.md ./config.ini ./install-all.sh ./start-all.sh ./check-service.sh ./stop-all.sh ./uninstall-all.sh ${out_path}
    cp -r ./third-packages ${out_path}

    mkdir -p ${out_path}/modules/broker
    cp ./modules/broker/install-broker.sh ${out_path}/modules/broker
    cp -r ${top_path}/weevent-broker/dist/* ${out_path}/modules/broker

    mkdir -p ${out_path}/modules/governance
    cp ./modules/governance/install-governance.sh ${out_path}/modules/governance
    cp -r ${top_path}/weevent-governance/dist/* ${out_path}/modules/governance

    mkdir -p ${out_path}/modules/nginx
    cp ./modules/nginx/install-nginx.sh ./modules/nginx/nginx.sh ${out_path}/modules/nginx
    cp -r ./modules/nginx/conf ${out_path}/modules/nginx
}

# switch to prod,remove dev properties
function switch_to_prod(){
    cd ${current_path}

    if [ -e ${out_path}/modules/broker/conf/application-dev.properties ]; then
        rm ${out_path}/modules/broker/conf/application-dev.properties
    fi

    if [ -e ${out_path}/modules/broker/conf/application.properties ]; then
        sed -i 's/dev/prod/' ${out_path}/modules/broker/conf/application.properties
    fi

    if [ -e ${out_path}/modules/governance/conf/application-dev.yml ]; then
        rm ${out_path}/modules/governance/conf/application-dev.yml
    fi

    if [ -e ${out_path}/modules/governance/conf/application.yml ]; then
        sed -i 's/dev/prod/' ${out_path}/modules/governance/conf/application.yml
    fi
}

function tar_broker(){
    local target=$1

    yellow_echo "generate ${target}"
    cd ${out_path}/modules

    cp -r broker broker-${version}
    tar -czpvf ${target} broker-${version}
    mv ${target} ${current_path}
}

function tar_governance(){
    local target=$1

    yellow_echo "generate ${target}"
    cd ${out_path}/modules

    cp -r governance governance-${version}
    tar -czpvf ${target} governance-${version}
    mv ${target} ${current_path}
}

function tar_nginx(){
    local target=$1

    yellow_echo "generate ${target}"
    cd ${out_path}/modules

    mkdir -p ./nginx/third-packages
    cp ${current_path}/third-packages/nginx-1.14.2.tar.gz ./nginx/third-packages
    cp ${current_path}/third-packages/pcre-8.20.tar.gz ./nginx/third-packages
    cp -r nginx nginx-${version}
    tar -czpvf ${target} nginx-${version}
    mv ${target} ${current_path}
}

# package weevent-$version
function package(){
    confirm ${out_path}
    mkdir -p ${out_path}
    execute_result "mkdir ${out_path}"

    yellow_echo "begin to package weevent-${version}"

    yellow_echo "build weevent [${tag}]"
    #generate jar and web
    build_weevent

    #copy file from build path
    yellow_echo "prepare install file"
    copy_install_file
    switch_to_prod
    set_permission

    #package weevent
    yellow_echo "generate weevent-${version}.tar.gz"
    cd ${current_path}
    tar -czpvf weevent-${version}.tar.gz `basename ${out_path}`

    #tar broker module
    tar_broker weevent-broker-${version}.tar.gz

    #tar governance module
    tar_governance weevent-governance-${version}.tar.gz

    #tar nginx module
    tar_nginx weevent-nginx-${version}.tar.gz

    #remove template
    rm -rf ${out_path}
}

function main(){
    if [ -z "$version" ];then
        usage
        exit 1
    fi

    out_path=${current_path}/weevent-${version}
    package

    cd ${current_path}
    yellow_echo "package success"
}

main
