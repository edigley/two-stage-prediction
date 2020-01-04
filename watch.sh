#!/bin/bash
echo "      pID  %CPU     RSS     ST     ET   IND"
ps aux | grep farsite4P | egrep -v "(sh -c|timeout|grep)" | awk '{print " -> "$2"  "$3"  "$6"  "$9"  "$10"  "$13}' | sed 's#output/settings_##g' | sed 's#\.txt##g'
#ps aux | egrep "(farsite|prediction)"
