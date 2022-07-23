#!/usr/bin/env bash

echo ""
echo "=========================================="
echo "status-bar installation"
echo "=========================================="
echo ""
echo "This program requires:"
echo "* JDK >= 8"
echo "* libgtk-3-dev"
echo "* libpango1.0-dev"
echo "* libxml2-dev"
echo "* libpango1.0-0"
echo "* libxml2"
echo "* libxcrypt-compat (Arch)"
echo ""
echo "Recommended:"
echo "* fonts-ubuntu"
echo ""

./gradlew build

cp "./build/bin/native/releaseExecutable/status-bar.kexe" "/usr/bin/lcarsde-status-bar.kexe"
cp -r "./src/nativeMain/resources/usr/*" "/usr/"
cp -r "./src/nativeMain/resources/etc/*" "/etc/"
