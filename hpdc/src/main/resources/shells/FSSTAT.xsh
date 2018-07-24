case `/bin/uname` in
SunOS)
   echo "OSTYPE:SOLARIS"
   df -Pk
        ;;
AIX)
   echo "OSTYPE:AIX"
   df -Pk
        ;;
Linux)
   echo "OSTYPE:LINUX"
   df -Pk
        ;;
HP-UX)
   echo "OSTYPE:HPUX"
   df -Pk
        ;;
esac