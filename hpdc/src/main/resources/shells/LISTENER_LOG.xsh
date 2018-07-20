LISTENER_LOG_FILE="%s"
LISTENER_LOG_OFFSET=%s
case `/bin/uname` in
SunOS)
  LISTENER_LOG_FILESIZE=`ls -l "$LISTENER_LOG_FILE"|awk '{print $5}'`
  if [ $LISTENER_LOG_OFFSET -lt 0 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE+1))
  fi
  READ_SIZE=$(($LISTENER_LOG_FILESIZE - $LISTENER_LOG_OFFSET))
  if [ $READ_SIZE -lt 0 ]; then
    LISTENER_LOG_OFFSET=0
    READ_SIZE=$LISTENER_LOG_FILESIZE
  fi
  if [ $READ_SIZE -gt 10000000 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE-10000000))
    READ_SIZE=10000000
  fi
  tail -"$READ_SIZE"c $LISTENER_LOG_FILE
  LISTENER_LOG_OFFSET=$(($LISTENER_LOG_OFFSET+$READ_SIZE))
  echo "LISTENER_LOG_OFFSET=$LISTENER_LOG_OFFSET"
       ;;
AIX)
  LISTENER_LOG_FILESIZE=`ls -l "$LISTENER_LOG_FILE"|awk '{print $5}'`
  if [ $LISTENER_LOG_OFFSET -lt 0 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE+1))
  fi
  READ_SIZE=$(($LISTENER_LOG_FILESIZE - $LISTENER_LOG_OFFSET))
  if [ $READ_SIZE -lt 0 ]; then
    LISTENER_LOG_OFFSET=0
    READ_SIZE=$LISTENER_LOG_FILESIZE
  fi
  if [ $READ_SIZE -gt 10000000 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE-10000000))
    READ_SIZE=10000000
  fi
  
  OSLEVEL=`oslevel|awk -F. '{print $1"."$2}'`
  
  if [ "$OSLEVEL" == "5.3" ]; then
  	tail -"$READ_SIZE"c $LISTENER_LOG_FILE
  else
    dd if=$LISTENER_LOG_FILE skip=$LISTENER_LOG_OFFSET bs=1 count=$READ_SIZE
  fi
  
  LISTENER_LOG_OFFSET=$(($LISTENER_LOG_OFFSET+$READ_SIZE))
  echo "LISTENER_LOG_OFFSET=$LISTENER_LOG_OFFSET"
       ;;
Linux)
  LISTENER_LOG_FILESIZE=`ls -l "$LISTENER_LOG_FILE"|awk '{print $5}'`
  if [ $LISTENER_LOG_OFFSET -lt 0 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE+1))
  fi
  READ_SIZE=$(($LISTENER_LOG_FILESIZE - $LISTENER_LOG_OFFSET))
  if [ $READ_SIZE -lt 0 ]; then
    LISTENER_LOG_OFFSET=0
    READ_SIZE=$LISTENER_LOG_FILESIZE
  fi
  if [ $READ_SIZE -gt 10000000 ]; then
    LISTENER_LOG_OFFSET=$(($LISTENER_LOG_FILESIZE-10000000))
    READ_SIZE=10000000
  fi
  dd if=$LISTENER_LOG_FILE skip=$LISTENER_LOG_OFFSET bs=1 count=$READ_SIZE
  LISTENER_LOG_OFFSET=$(($LISTENER_LOG_OFFSET+$READ_SIZE))
  echo "LISTENER_LOG_OFFSET=$LISTENER_LOG_OFFSET"
       ;;
HP-UX)
  LISTENER_LOG_FILESIZE=`ls -l "$LISTENER_LOG_FILE"|awk '{print $5}'`
  if [ $LISTENER_LOG_OFFSET -lt 0 ]; then
    LISTENER_LOG_OFFSET=`echo "$LISTENER_LOG_FILESIZE+1"|bc`
  fi
  READ_SIZE=`echo "$LISTENER_LOG_FILESIZE - $LISTENER_LOG_OFFSET"|bc`
  if [ $READ_SIZE -lt 0 ]; then
    LISTENER_LOG_OFFSET=0
    READ_SIZE=$LISTENER_LOG_FILESIZE
  fi
  if [ `echo "$LISTENER_LOG_FILESIZE/10000000"|bc` -gt 1 ]; then
    LISTENER_LOG_OFFSET=`echo "$LISTENER_LOG_FILESIZE-10000000"|bc`
    READ_SIZE=10000000
  fi
  tail -c $READ_SIZE $LISTENER_LOG_FILE
  LISTENER_LOG_OFFSET=`echo "$LISTENER_LOG_OFFSET+$READ_SIZE"|bc`
  echo "LISTENER_LOG_OFFSET=$LISTENER_LOG_OFFSET"
       ;;
esac