#!/bin/bash

echo ""
echo "=========================================="
echo "lcarsde application starter installation"
echo "=========================================="
echo ""
echo "This program requires:"
echo "* Python 3.8"
echo "* Python 3 PyGObject"
echo "* Python pycairo"
echo "* Python numpy"
echo "* Python pyalsaaudio"
echo "* Alsa audio for audio widgets"
echo ""


cp ./src/lcarsde-status-bar.py /usr/bin/lcarsde-status-bar.py
cp -R ./resources/etc/* /etc/

pip install ./src
