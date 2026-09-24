#!/bin/bash
set -euo pipefail

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$script_dir"

if [ -z "${MVN:-}" ]; then
    MVN=$(command -v mvn) || { echo "请通过 MVN 指定 Maven 可执行文件" >&2; exit 1; }
fi

install_goal=org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file
"$MVN" -B -ntp "$@" "$install_goal" -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.1.0.7.0 -Dpackaging=jar -Dfile=ojdbc6.jar -DgeneratePom=true
"$MVN" -B -ntp "$@" "$install_goal" -DgroupId=org.jtester -DartifactId=jtester -Dversion=1.1.8 -Dpackaging=jar -DpomFile=jtester-1.1.8.pom -Dfile=jtester-1.1.8.jar -Dsources=jtester-1.1.8-sources.jar
"$MVN" -B -ntp "$@" "$install_goal" -DgroupId=mockit -DartifactId=jmockit -Dversion=0.999.10 -Dpackaging=jar -DpomFile=jmockit-0.999.10.pom -Dfile=jmockit-0.999.10.jar -Dsources=jmockit-0.999.10-sources.jar
