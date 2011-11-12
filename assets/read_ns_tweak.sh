#!/system/bin/sh

# outputfile to save result
OUT_FILE="/data/data/mobi.cyann.nstools/nstweak.prop"

# prepare the file
echo "" > $OUT_FILE

# arm volt
STAT=`cat /sys/class/misc/customvoltage/max_arm_volt`
case "$?" in
	0)
	echo "key_max_arm_volt=$STAT" >> $OUT_FILE
	;;
	*)
	echo "key_max_arm_volt=-1" >> $OUT_FILE
	;;
esac

# int volt
STAT=`cat /sys/class/misc/customvoltage/max_int_volt`
case "$?" in
	0)
	echo "key_max_int_volt=$STAT" >> $OUT_FILE
	;;
	*)
	echo "key_max_int_volt=-1" >> $OUT_FILE
	;;
esac

# bld
STAT=`cat /sys/class/misc/backlightdimmer/enabled`
case "$STAT" in
	1)
	echo "key_bld_status=1" >> $OUT_FILE
	echo "key_bld_delay=`cat /sys/class/misc/backlightdimmer/delay`" >> $OUT_FILE
	;;
	0)
	echo "key_bld_status=0" >> $OUT_FILE
	echo "key_bld_delay=`cat /sys/class/misc/backlightdimmer/delay`" >> $OUT_FILE
	;;
	*)
	echo "key_bld_status=-1" >> $OUT_FILE
	echo "key_bld_delay=0" >> $OUT_FILE
	;;
esac

# bln
STAT=`cat /sys/class/misc/backlightnotification/enabled`
case "$STAT" in
	1)
	echo "key_bln_status=1" >> $OUT_FILE
	;;
	2)
	echo "key_bln_status=0" >> $OUT_FILE
	;;
	*)
	echo "key_bln_status=-1" >> $OUT_FILE
	;;
esac

# blx
STAT=`cat /sys/class/misc/batterylifeextender/charging_limit`
case "$?" in
	0)
	echo "key_blx_charging_limit=$STAT" >> $OUT_FILE
	;;
	*)
	echo "key_blx_charging_limit=-1" >> $OUT_FILE
	;;
esac

# bln
STAT=`cat /sys/class/misc/deepidle/enabled`
case "$STAT" in
	1)
	echo "key_deepidle_status=1" >> $OUT_FILE
	;;
	0)
	echo "key_deepidle_status=0" >> $OUT_FILE
	;;
	*)
	echo "key_deepidle_status=-1" >> $OUT_FILE
	;;
esac

# liveoc
STAT=`cat /sys/class/misc/liveoc/oc_value`
case "$?" in
	0)
	echo "key_liveoc=$STAT" >> $OUT_FILE
	;;
	*)
	echo "key_liveoc=-1" >> $OUT_FILE
	;;
esac

# touchwake
STAT=`cat /sys/class/misc/touchwake/enabled`
case "$STAT" in
	1)
	echo "key_touchwake_status=1" >> $OUT_FILE
	echo "key_touchwake_delay=`cat /sys/class/misc/touchwake/delay`" >> $OUT_FILE
	;;
	0)
	echo "key_touchwake_status=0" >> $OUT_FILE
	echo "key_touchwake_delay=`cat /sys/class/misc/touchwake/delay`" >> $OUT_FILE
	;;
	*)
	echo "key_touchwake_status=-1" >> $OUT_FILE
	echo "key_touchwake_delay=0" >> $OUT_FILE
	;;
esac

