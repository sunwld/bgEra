case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  ipcs -ma
       ;;
AIX)
  echo "OSTYPE:AIX"
  ipcs -ma
       ;;
Linux)
  echo "OSTYPE:LINUX"
  ipcs -m
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
  ipcs -ma
       ;;
esac