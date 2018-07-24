case `/bin/uname` in
SunOS)
  echo "OSTYPE:SOLARIS"
  mpstat 1 5|awk '{print $1" "$(NF-3)" "$(NF-2)" "$(NF-1)" "$NF}'
       ;;
AIX)
  echo "OSTYPE:AIX"
  sar -P ALL 1 5
       ;;
Linux)
  echo "OSTYPE:LINUX"
  sar -P ALL 1 5
       ;;
HP-UX)
  echo "OSTYPE:HPUX"
       ;;
esac
