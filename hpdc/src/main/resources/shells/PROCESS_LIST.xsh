case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  ps -ely
       ;;
AIX)
  echo "OSTYPE:AIX"
  ps -el
       ;;
Linux)
  echo "OSTYPE:LINUX"
  ps -ely
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
       ;;
esac
