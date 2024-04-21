#!/usr/bin/env sh

cd $(dirname $0)
echo "
Main.main(new String[0]);
while (true) {
    Thread.sleep(1000);
}" | cat Main.java /dev/stdin | jshell -
