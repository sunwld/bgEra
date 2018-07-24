case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  netstat -in
       ;;
AIX)
  echo "OSTYPE:AIX"
  netstat -in
       ;;
Linux)
  echo "OSTYPE:LINUX"
  netstat -in
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
  netstat -in
       ;;
esac
