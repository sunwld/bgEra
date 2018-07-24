case `/bin/uname` in
SunOS)
   echo "OSTYPE:SOLARIS"
   vmstat -s|grep "pages swapped"
        ;;
AIX)
   echo "OSTYPE:AIX"
   vmstat -s|grep "paging space"
        ;;
Linux)
   echo "OSTYPE:LINUX"
   vmstat -s|grep "pages swapped"
        ;;
HP-UX)
   echo "OSTYPE:HPUX"
        ;;
esac