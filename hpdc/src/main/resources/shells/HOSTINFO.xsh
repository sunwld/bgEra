case `/bin/uname` in
SunOS)
  echo "**********Filesystem**********"
  df -Pk|awk '{print $NF}'|sed '1d'
  echo "**********/etc/hosts**********"
  cat /etc/hosts
  echo "**********Networks**********"
  netstat -in
       ;;
AIX)
  echo "**********Filesystem**********"
  df -Pk|awk '{print $NF}'|sed '1d'
  echo "**********/etc/hosts**********"
  cat /etc/hosts
  echo "**********Networks**********"
  netstat -in
       ;;
Linux)
  echo "**********Filesystem**********"
  df -Pk|awk '{print $NF}'|sed '1d'
  echo "**********/etc/hosts**********"
  cat /etc/hosts
  echo "**********Networks**********"
  netstat -in
       ;;
HP-UX)
  echo "**********Filesystem**********"
  df -Pk|awk '{print $NF}'|sed '1d'
  echo "**********/etc/hosts**********"
  cat /etc/hosts
  echo "**********Networks**********"
  netstat -in
       ;;
esac 