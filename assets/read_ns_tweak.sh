#!/system/bin/sh

# outputfile to save result
OUT_FILE="/data/data/mobi.cyann.nstools/nstweak.prop"

# empty the file
echo "" > $OUT_FILE

# bld
STAT=`cat /sys/class/misc/backlightdimmer/enabled`
if [ "$STAT" == "1" ] ; then
	echo "bld.status=1" >> $OUT_FILE
	echo "bld.delay=`cat /sys/class/misc/backlightdimmer/delay`" >> $OUT_FILE
elif [ "$STAT" == "0" ] ; then
	echo "bld.status=0" >> $OUT_FILE
	echo "bld.delay=`cat /sys/class/misc/backlightdimmer/delay`" >> $OUT_FILE
else
	echo "bld.status=-1" >> $OUT_FILE
	echo "bld.delay=0" >> $OUT_FILE
fi

# screen dimmer
STAT=`cat /sys/class/misc/screendimmer/enabled`
if [ "$STAT" == "1" ] ; then
	echo "screendimmer.status=1" >> $OUT_FILE
	echo "screendimmer.delay=`cat /sys/class/misc/screendimmer/delay`" >> $OUT_FILE
elif [ "$STAT" == "0" ] ; then
	echo "screendimmer.status=0" >> $OUT_FILE
	echo "screendimmer.delay=`cat /sys/class/misc/screendimmer/delay`" >> $OUT_FILE
else
	echo "screendimmer.status=-1" >> $OUT_FILE
	echo "screendimmer.delay=0" >> $OUT_FILE
fi
