#!/bin/bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.10/libexec/openjdk.jdk/Contents/Home
exec kotlin-language-server "$@"
