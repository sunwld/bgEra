FILESYSTEMS_STR="%s"
case `/bin/uname` in
SunOS)
  df -Pk|egrep "$FILESYSTEMS_STR"|awk '{print $(NF)" "$(NF-4)" "$(NF-2)}'
       ;;
AIX)
  df -Pk|egrep "$FILESYSTEMS_STR"|awk '{print $(NF)" "$(NF-4)" "$(NF-2)}'
       ;;
Linux)
  df -Pk|egrep "$FILESYSTEMS_STR"|awk '{print $(NF)" "$(NF-4)" "$(NF-2)}'
       ;;
HP-UX)
  df -Pk|egrep "$FILESYSTEMS_STR"|awk '{print $(NF)" "$(NF-4)" "$(NF-2)}'
       ;;
esac 