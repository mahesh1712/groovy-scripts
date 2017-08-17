#!/bin/bash
#$1 Project Location from which build.xml have to pick
#$2 Name of the build.xml
#$3 Database backup parameter ( 0=No backup, 1 = backup)

if [ -d "${WORKSPACE}/build" ]; then
        rm -Rf ${WORKSPACE}/build
fi

if [ -f "${WORKSPACE}/build.xml" ]; then
        rm -f ${WORKSPACE}/build.xml
fi

if [ -f "${WORKSPACE}/phpmd.xml" ]; then
        rm -f ${WORKSPACE}/phpmd.xml
fi

mkdir ${WORKSPACE}/build
cp $2 ${WORKSPACE}/build.xml

if [ "0" -eq "$3" ]; then
        if [[ -f ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php && -f ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php ]]; then
          mv ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php ${DEPLOYER}/deploy.php
        fi

        if [[ -f ${DEPLOYER}/deploy.php && -f ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php ]]; then
          mv ${DEPLOYER}/deploy.php ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php
          mv ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php ${DEPLOYER}/deploy.php
        fi
fi

if [ "1" -eq "$3" ]; then
        if [[ -f ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php && -f ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php ]]; then
          mv ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php ${DEPLOYER}/deploy.php
        fi

        if [[ -f ${DEPLOYER}/deploy.php && -f ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php ]]; then
          mv ${DEPLOYER}/deploy.php ${DEPLOYER}/deploy_second_time_onwards_without_database_backup.php
          mv ${DEPLOYER}/deploy_second_time_onwards_with_database_backup.php ${DEPLOYER}/deploy.php
        fi
fi

if [ -f "${JENKINS_HOME}/$1/phpmd.xml" ]; then
        cp ${JENKINS_HOME}/$1/phpmd.xml ${WORKSPACE}/phpmd.xml
fi
