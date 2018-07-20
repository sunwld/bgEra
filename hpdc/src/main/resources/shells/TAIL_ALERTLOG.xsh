ALERT_FILE_NAME="%s"
case `/bin/uname` in
SunOS)
  echo "**********Alertlog tail50**********"
  tail -50l $ALERT_FILE_NAME
       ;;
AIX)
  echo "**********Alertlog tail50**********"
  tail -n 50 $ALERT_FILE_NAME
       ;;
Linux)
  echo "**********Alertlog tail50**********"
  tail -n 50 $ALERT_FILE_NAME
       ;;
HP-UX)
  echo "**********Alertlog tail50**********"
  tail -n 50 $ALERT_FILE_NAME
       ;;
esac 
