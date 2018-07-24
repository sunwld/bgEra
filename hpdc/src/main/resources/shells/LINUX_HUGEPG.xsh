case `/bin/uname` in
SunOS)
        ;;
AIX)
        ;;
Linux)
  cat /proc/meminfo|egrep "Hugepages|HugePages"
        ;;
HP-UX)
        ;;
esac