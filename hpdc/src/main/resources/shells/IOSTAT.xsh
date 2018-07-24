case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  sar -d 1 5
       ;;
AIX)
  echo "OSTYPE:AIX"
  sar -d 1 5
       ;;
Linux)
  echo "OSTYPE:LINUX"
  iostat -xdk 1 5
       ;;
HP-UX)
   echo "OSTYPE:HPUX"
   sar -d 1 5
        ;;
esac
