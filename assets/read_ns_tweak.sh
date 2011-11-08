#!/system/bin/sh

# outputfile to save result
OUT_FILE="/data/data/mobi.cyann.nstools/shared_prefs/mobi.cyann.nstools_preferences.xml"

# prepare the file
echo "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>" > $OUT_FILE
echo "<map>" >> $OUT_FILE

# bld
STAT=`cat /sys/class/misc/backlightdimmer/enabled`
case "$STAT" in
	1)
	echo "<string name=\"key_bld_status\">1</string>" >> $OUT_FILE
	echo "<string name=\"key_bld_delay\">`cat /sys/class/misc/backlightdimmer/delay`</string>" >> $OUT_FILE
	;;
	0)
	echo "<string name=\"key_bld_status\">0</string>" >> $OUT_FILE
	echo "<string name=\"key_bld_delay\">`cat /sys/class/misc/backlightdimmer/delay`</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_bld_status\">-1</string>" >> $OUT_FILE
	echo "<string name=\"key_bld_delay\">0</string>" >> $OUT_FILE
	;;
esac

# bln
STAT=`cat /sys/class/misc/backlightnotification/enabled`
case "$STAT" in
	1)
	echo "<string name=\"key_bln_status\">1</string>" >> $OUT_FILE
	;;
	2)
	echo "<string name=\"key_bln_status\">0</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_bln_status\">-1</string>" >> $OUT_FILE
	;;
esac

# blx
STAT=`cat /sys/class/misc/batterylifeextender/charging_limit`
case "$?" in
	0)
	echo "<string name=\"key_blx_charging_limit\">$STAT</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_blx_charging_limit\">-1</string>" >> $OUT_FILE
	;;
esac

# bln
STAT=`cat /sys/class/misc/deepidle/enabled`
case "$STAT" in
	1)
	echo "<string name=\"key_deepidle_status\">1</string>" >> $OUT_FILE
	;;
	0)
	echo "<string name=\"key_deepidle_status\">0</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_deepidle_status\">-1</string>" >> $OUT_FILE
	;;
esac

# liveoc
STAT=`cat /sys/class/misc/liveoc/oc_value`
case "$?" in
	0)
	echo "<string name=\"key_liveoc\">$STAT</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_liveoc\">-1</string>" >> $OUT_FILE
	;;
esac

# touchwake
STAT=`cat /sys/class/misc/touchwake/enabled`
case "$STAT" in
	1)
	echo "<string name=\"key_touchwake_status\">1</string>" >> $OUT_FILE
	echo "<string name=\"key_touchwake_delay\">`cat /sys/class/misc/touchwake/delay`</string>" >> $OUT_FILE
	;;
	0)
	echo "<string name=\"key_touchwake_status\">0</string>" >> $OUT_FILE
	echo "<string name=\"key_touchwake_delay\">`cat /sys/class/misc/touchwake/delay`</string>" >> $OUT_FILE
	;;
	*)
	echo "<string name=\"key_touchwake_status\">-1</string>" >> $OUT_FILE
	echo "<string name=\"key_touchwake_delay\">0</string>" >> $OUT_FILE
	;;
esac

echo "</map>" >> $OUT_FILE
