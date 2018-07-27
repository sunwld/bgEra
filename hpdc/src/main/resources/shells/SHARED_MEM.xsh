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
  ipcs -mt
  ipcs -mp
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
  ipcs -ma
       ;;
esac