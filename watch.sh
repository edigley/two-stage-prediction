#!/bin/bash
ps aux | grep farsite | egrep -v "(sh -c|timeout|grep)"
