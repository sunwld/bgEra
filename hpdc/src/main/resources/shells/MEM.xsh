MEM_TOTAL=0
MEM_FREE=0
CACHE_INUSE=0
SWAP_TOTAL=0
SWAP_FREE=0
case `/bin/uname` in
SunOS)
    MEM_TOTAL=`/usr/sbin/prtconf|grep Memory|awk '{printf($(NF-1)*1024*1024)}'`
    MEM_FREE=`vmstat 1 2|sed '1,3d'|awk '{printf($5*1024)}'`
    CACHE_INUSE=`echo ::memstat | mdb -k|egrep "Page cache|ZFS File Data|Free \(cachelist\)"|awk '{print $(NF-1)}'|awk '{sum += $1} END{printf("%0.0f\n",sum*1024*1024)}'`
    SWAP_STR=`/usr/sbin/swap -lk|sed -n '2p'`
    SWAP_TOTAL=`echo $SWAP_STR|awk '{print $(NF-1)}'|tr -d "K"|awk '{printf($1*1024)}'`
    SWAP_FREE=`echo $SWAP_STR|awk '{print $(NF)}'|tr -d "K"|awk '{printf($1*1024)}'`
       ;;
AIX)
    MEM_STR=`svmon -G|grep memory`
    MEM_TOTAL=$((`echo $MEM_STR|awk '{print $2}'`*4096))
    MEM_FREE=$((`echo $MEM_STR|awk '{print $4}'`*4096))
    MEM_STR_PIN=`svmon -G|grep -E "^pin"`
    MEM_STR_INUSE=`svmon -G|grep -E "^in use"`
    CACHE_INUSE=$((`echo $MEM_STR_PIN|awk '{print $4}'`*4096+`echo $MEM_STR_INUSE|awk '{print $5}'`*4096))
    SWAP_STR=`lsps -s|grep -v Total`
    SWAP_TOTAL=`echo $SWAP_STR|awk '{printf("%0.0f\n",$1*1024*1024)}'`
    SWAP_INUSEP=`echo $SWAP_STR|awk '{printf("%0.0f\n",100-$2)}'`
    SWAP_FREE=$(($SWAP_TOTAL*$SWAP_INUSEP/100))
       ;;
Linux)
    MEM_STR=`free -k|grep "Mem"`
    SWAP_STR=`free -k|grep "Swap"`
    MEM_TOTAL=`echo $MEM_STR|awk '{printf("%0.0f\n",$2*1024)}'`
    MEM_FREE=`echo $MEM_STR|awk '{printf("%0.0f\n",$4*1024)}'`
    CACHE_INUSE=`echo $MEM_STR|awk '{printf("%0.0f\n",$7*1024)}'`
    SWAP_TOTAL=`echo $SWAP_STR|awk '{printf("%0.0f\n",$2*1024)}'`
    SWAP_FREE=`echo $SWAP_STR|awk '{printf("%0.0f\n",$4*1024)}'`
       ;;
HP-UX)
    MEM_STR=`>dcmp_mem.tmp;top -h -d1 -s1 -n1 -fdcmp_mem.tmp;cat dcmp_mem.tmp|grep "Memory"`
    MEM_TOTAL=`/usr/sbin/swapinfo|grep memory|awk '{printf("%0.0f\n",$2*1024)}'`
    MEM_FREE=`echo $MEM_STR|awk -F "," '{print $3}'|awk '{printf("%0.0f\n",$1*1024)}'`
    ACT_INUSE=`echo $MEM_STR|awk -F "," '{print $1}'|awk '{printf("%0.0f\n",$2*1024)}'`
    CACHE_INUSE=`echo "$MEM_TOTAL-$ACT_INUSE"|bc`
    SWAP_STR=`/usr/sbin/swapinfo|grep "dev"`
    SWAP_TOTAL=`echo $SWAP_STR|awk '{printf("%0.0f\n",$2*1024)}'`
    SWAP_FREE=`echo $SWAP_STR|awk '{printf("%0.0f\n",$4*1024)}'`
       ;;
esac 
echo "MEM_TOTAL=$MEM_TOTAL"
echo "MEM_FREE=$MEM_FREE"
echo "CACHE_INUSE=$CACHE_INUSE"
echo "SWAP_TOTAL=$SWAP_TOTAL"
echo "SWAP_FREE=$SWAP_FREE"
