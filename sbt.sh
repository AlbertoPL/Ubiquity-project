#!/bin/sh

java -Dfile.encoding=UTF8 -Xmx512M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -jar `dirname $0`/sbt-0.11.0-launch.jar "$@"
