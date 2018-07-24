case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  netstat -s
       ;;
AIX)
  echo "OSTYPE:AIX"
  netstat -s
       ;;
Linux)
  echo "OSTYPE:LINUX"
  netstat -s
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
  netstat -s
       ;;
esac