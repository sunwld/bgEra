USRP=0
SYSP=0
WAITP=0
IDLEP=0
case `/bin/uname` in
SunOS)
  CPU_STR=`sar -u 1 3|egrep "^[0-9]*:[0-9]*:[0-9]*"|sed '1d'|awk '{usr+=$(NF-3);sys+=$(NF-2);wio+=$(NF-1);idle+=$(NF)} END{print usr","sys","wio","idle}'`
  USRP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$1/3)}'`
  SYSP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$2/3)}'`
  WAITP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$3/3)}'`
  IDLEP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$4/3)}'`
       ;;
AIX)
  CPU_STR=`sar -u 1 3|egrep "^[0-9]*:[0-9]*:[0-9]*"|sed '1d'|awk '{usr+=$(NF-4);sys+=$(NF-3);wio+=$(NF-2);idle+=$(NF-1)} END{print usr","sys","wio","idle}'`
  USRP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$1/3)}'`
  SYSP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$2/3)}'`
  WAITP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$3/3)}'`
  IDLEP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$4/3)}'`
       ;;
Linux)
  CPU_STR=`sar -u 1 3|egrep "^[0-9]*:[0-9]*:[0-9]*"|sed '1d'|awk '{usr+=$(NF-5);sys+=$(NF-3);wio+=$(NF-2);idle+=$(NF)} END{print usr","sys","wio","idle}'`
  USRP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$1/3)}'`
  SYSP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$2/3)}'`
  WAITP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$3/3)}'`
  IDLEP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$4/3)}'`
       ;;
HP-UX)
  CPU_STR=`sar -u 1 3|egrep "^[0-9]*:[0-9]*:[0-9]*"|sed '1d'|awk '{usr+=$(NF-3);sys+=$(NF-2);wio+=$(NF-1);idle+=$(NF)} END{print usr","sys","wio","idle}'`
  USRP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$1/3)}'`
  SYSP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$2/3)}'`
  WAITP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$3/3)}'`
  IDLEP=`echo $CPU_STR|awk -F"," '{printf("%0.2f\n",$4/3)}'`
       ;;
esac
echo "USRP=$USRP"
echo "SYSP=$SYSP"
echo "WAITP=$WAITP"
echo "IDLEP=$IDLEP"
