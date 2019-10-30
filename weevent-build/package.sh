#!/bin/bash
# generate WeEvent-x.x.x.tar.gz package from github project.
# depend internet online and tools as followings:
# 1. git
# 2. gradle 4.10
# 3. java 1.8
# 4. nodejs 10.16
################################################################################
weevent_version=1.1.0
function usage(){
    echo "Usage:"
    echo "    package master: ./package.sh --version ${weevent_version}"
    echo "    package tag: ./package.sh --tag v${weevent_version} --version ${weevent_version}"
    echo "    package local: ./package.sh --tag local --version ${weevent_version}"
}

version=""
tag="master"

current_path=$(pwd)
top_path=$(dirname ${current_path})
out_path=""

while [[ $# -ge 2 ]] ; do
    case "$1" in
    --tag) param="$1 = $2;";tag=$2;shift 2;;
    --version) param="$1 = $2;";version="$2";shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
    esac
done

function yellow_echo(){
    local what=$*
    if true;then
        echo -e "\e[1;33m${what} \e[0m"
    fi
}

function execute_result(){
    if [[ $? -ne 0 ]];then
        echo "$1 fail"
        exit 1
    fi
}

# confirm whether to override input path
function confirm(){
    if [[ -d $1 ]]; then
        read -p "$out_path already exist, continue? [Y/N]" cmd_input
        if [[ "Y" != "$cmd_input" && "y" != "$cmd_input" ]]; then
            echo "input $cmd_input, install skipped"
            exit 1
        fi
    fi
}

# chmod & dos2unix
function set_permission(){
    cd ${out_path}
    find -type f -regex  ".*\.\(sh\|ini\|properties\|xml\)" | xargs dos2unix
    find -type f -regex ".*\.\(sh\)" | xargs -t -i chmod +x {}
}

# build broker, governance, client, web
function build_weevent(){
    cd ${top_path}

    if [[ "$tag" != "local" ]];then
        yellow_echo "package github[${tag}]"

        # switch tag
        git checkout ${tag}; git pull
        execute_result "git checkout ${tag}"
    else
        yellow_echo "package local path"
    fi

    # node.js build html and css
    yellow_echo "build web in node.js"
    cd ${top_path}/weevent-governance/web
    if [[ -e build-web.sh ]];then
        chmod +x build-web.sh
        dos2unix build-web.sh
    fi
    ./build-web.sh

    # gradle clean then build
    yellow_echo "build java in gradle"
    cd ${top_path}
    ./gradlew clean
    execute_result "clean weevent"
    ./gradlew build -x test
    execute_result "build weevent"
}

function copy_install_file(){
    cd ${current_path}
    cp ${current_path}/config.properties ${current_path}/install-all.sh ${out_path}
    cp -r ${current_path}/bin ${out_path}
    cp -r ${current_path}/third-packages ${out_path}

    mkdir -p ${out_path}/modules/broker
    cp ${current_path}/modules/broker/install-broker.sh ${out_path}/modules/broker
    cp -r ${top_path}/weevent-broker/dist/* ${out_path}/modules/broker

    mkdir -p ${out_path}/modules/governance
    cp ${current_path}/modules/governance/install-governance.sh ${out_path}/modules/governance
    cp -r ${top_path}/weevent-governance/dist/* ${out_path}/modules/governance

    mkdir -p ${out_path}/modules/processor
    cp ${current_path}/modules/processor/install-processor.sh ${out_path}/modules/processor
    cp -r ${top_path}/weevent-processor/dist/* ${out_path}/modules/processor



    mkdir -p ${out_path}/modules/nginx
    cp ${current_path}/modules/nginx/install-nginx.sh ./modules/nginx/nginx.sh ${out_path}/modules/nginx
    cp -r ${current_path}/modules/nginx/conf ${out_path}/modules/nginx
}

# switch to prod.properties, remove dev.properties
function switch_to_prod(){
    cd ${current_path}

    rm -rf ${out_path}/modules/broker/conf/application-dev.properties
    if [[ -e ${out_path}/modules/broker/conf/application.properties ]]; then
        sed -i 's/dev/prod/' ${out_path}/modules/broker/conf/application.properties
    fi

    rm -rf ${out_path}/modules/governance/conf/application-dev.properties
    if [[ -e ${out_path}/modules/governance/conf/application.properties ]]; then
        sed -i 's/dev/prod/' ${out_path}/modules/governance/conf/application.properties
    fi

    rm -rf ${out_path}/modules/processor/conf/application-dev.properties
    if [[ -e ${out_path}/modules/processor/conf/application.properties ]]; then
        sed -i 's/dev/prod/' ${out_path}/modules/processor/conf/application.properties
    fi
}

function tar_broker(){
    local target=$1
    yellow_echo "generate ${target}"

    cp -r ${out_path}/modules/broker ${current_path}/broker-${version}
    # no need install shell
    rm -rf ${current_path}/broker-${version}/install-broker.sh

    # do not tar the top dir
    cd ${current_path}/broker-${version}
    tar -czpvf ${target} *
    mv ${target} ${current_path}

    rm -rf ${current_path}/broker-${version}
}

function tar_governance(){
    local target=$1
    yellow_echo "generate ${target}"

    cp -r ${out_path}/modules/governance ${current_path}/governance-${version}
    # no need install shell
    rm -rf ${current_path}/governance-${version}/install-governance.sh

    # do not tar the top dir
    cd ${current_path}/governance-${version}
    tar -czpvf ${target} *
    mv ${target} ${current_path}

    rm -rf ${current_path}/governance-${version}
}


function tar_processor(){
    local target=$1
    yellow_echo "generate ${target}"

    cp -r ${out_path}/modules/processor ${current_path}/processor-${version}
    # no need install shell
    rm -rf ${current_path}/processor-${version}/install-processor.sh

    # do not tar the top dir
    cd ${current_path}/processor-${version}
    tar -czpvf ${target} *
    mv ${target} ${current_path}

    rm -rf ${current_path}/processor-${version}
}

function tar_weevent(){
    local target=$1
    yellow_echo "generate ${target}"

    # thin spring boot jar, merge comm jars into one lib to reduce tar size
    mkdir -p ${out_path}/modules/lib
    for commonjar in $(ls ${out_path}/modules/broker/lib/);
    do
        # copy common jar into modules lib
        if [[ (-e ${out_path}/modules/governance/lib/${commonjar}) && (-e ${out_path}/modules/processor/lib/${commonjar}) ]]; then
            cp ${out_path}/modules/broker/lib/${commonjar} ${out_path}/modules/lib
            rm ${out_path}/modules/governance/lib/${commonjar}
            rm ${out_path}/modules/processor/lib/${commonjar}
            rm ${out_path}/modules/broker/lib/${commonjar}
        fi
    done

    # tar
    cd ${current_path}
    tar -czpvf weevent-${version}.tar.gz $(basename ${out_path})
}

# package weevent-$version
function package(){
    confirm ${out_path}
    mkdir -p ${out_path}
    execute_result "mkdir ${out_path}"

    yellow_echo "begin to package weevent-${version}"

    # generate jar and web
    build_weevent

    # copy file from build path
    yellow_echo "prepare install file"
    copy_install_file
    switch_to_prod
    set_permission

    # tar broker module
    tar_broker weevent-broker-${version}.tar.gz

    # tar governance module
    tar_governance weevent-governance-${version}.tar.gz

    # tar processor module
    tar_processor weevent-processor-${version}.tar.gz

    # tar weevent
    tar_weevent weevent-${version}.tar.gz

    # remove temporary path
    rm -rf ${out_path}
}

function main(){
    if [[ -z "${version}" ]];then
        usage
        exit 1
    fi

    # package in current path
    out_path=${current_path}/weevent-${version}
    package

    # reset to original path
    cd ${current_path}
    yellow_echo "package success"
}

main
