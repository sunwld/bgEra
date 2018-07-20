PRIV_IPK=-1
PRIV_IPKERR=-1
PRIV_OPK=-1
PRIV_OPKERR=-1
PUB_IPK=-1
PUB_IPKERR=-1
PUB_OPK=-1
PUB_OPKERR=-1
PUBLIC_ETH=%s
PRIVATE_ETH=%s
case `/bin/uname` in
SunOS)
  if [ -n "$PRIVATE_ETH" ]; then 
    PRIVATE_STR=`netstat -in|egrep "^$PRIVATE_ETH "|sed -n '1p'`
	  PRIV_IPK=`echo $PRIVATE_STR|awk '{print $(NF-5)}'`
	  PRIV_IPKERR=`echo $PRIVATE_STR|awk '{print $(NF-4)}'`
	  PRIV_OPK=`echo $PRIVATE_STR|awk '{print $(NF-3)}'`
	  PRIV_OPKERR=`echo $PRIVATE_STR|awk '{print $(NF-2)}'`
  fi
  PUBLIC_STR=`netstat -in|egrep "^$PUBLIC_ETH "|sed -n '1p'`
  PUB_IPK=`echo $PUBLIC_STR|awk '{print $(NF-5)}'`
  PUB_IPKERR=`echo $PUBLIC_STR|awk '{print $(NF-4)}'`
  PUB_OPK=`echo $PUBLIC_STR|awk '{print $(NF-3)}'`
  PUB_OPKERR=`echo $PUBLIC_STR|awk '{print $(NF-2)}'`
       ;;
AIX)
  if [ -n "$PRIVATE_ETH" ]; then 
	  PRIVATE_STR=`netstat -in|egrep "^$PRIVATE_ETH "|sed -n '1p'`
	  PRIV_IPK=`echo $PRIVATE_STR|awk '{print $(NF-4)}'`
	  PRIV_IPKERR=`echo $PRIVATE_STR|awk '{print $(NF-3)}'`
	  PRIV_OPK=`echo $PRIVATE_STR|awk '{print $(NF-2)}'`
	  PRIV_OPKERR=`echo $PRIVATE_STR|awk '{print $(NF-1)}'`
  fi
  PUBLIC_STR=`netstat -in|egrep "^$PUBLIC_ETH "|sed -n '1p'`
  PUB_IPK=`echo $PUBLIC_STR|awk '{print $(NF-4)}'`
  PUB_IPKERR=`echo $PUBLIC_STR|awk '{print $(NF-3)}'`
  PUB_OPK=`echo $PUBLIC_STR|awk '{print $(NF-2)}'`
  PUB_OPKERR=`echo $PUBLIC_STR|awk '{print $(NF-1)}'`
       ;;
Linux)
  if [ -n "$PRIVATE_ETH" ]; then 
	  PRIVATE_STR=`netstat -in|egrep "^$PRIVATE_ETH "|sed -n '1p'`
	  PRIV_IPK=`echo $PRIVATE_STR|awk '{print $(NF-8)}'`
	  PRIV_IPKERR=`echo $PRIVATE_STR|awk '{print $(NF-7)}'`
	  PRIV_OPK=`echo $PRIVATE_STR|awk '{print $(NF-4)}'`
	  PRIV_OPKERR=`echo $PRIVATE_STR|awk '{print $(NF-3)}'`
  fi
  PUBLIC_STR=`netstat -in|egrep "^$PUBLIC_ETH "|sed -n '1p'`
  PUB_IPK=`echo $PUBLIC_STR|awk '{print $(NF-8)}'`
  PUB_IPKERR=`echo $PUBLIC_STR|awk '{print $(NF-7)}'`
  PUB_OPK=`echo $PUBLIC_STR|awk '{print $(NF-4)}'`
  PUB_OPKERR=`echo $PUBLIC_STR|awk '{print $(NF-3)}'`
       ;;
HP-UX)
  if [ -n "$PRIVATE_ETH" ]; then 
	  PRIVATE_STR=`netstat -in|egrep "^$PRIVATE_ETH "|sed -n '1p'`
	  PRIV_IPK=`echo $PRIVATE_STR|awk '{print $(NF-4)}'`
	  PRIV_IPKERR=`echo $PRIVATE_STR|awk '{print $(NF-3)}'`
	  PRIV_OPK=`echo $PRIVATE_STR|awk '{print $(NF-2)}'`
	  PRIV_OPKERR=`echo $PRIVATE_STR|awk '{print $(NF-1)}'`
  fi
  PUBLIC_STR=`netstat -in|egrep "^$PUBLIC_ETH "|sed -n '1p'`
  PUB_IPK=`echo $PUBLIC_STR|awk '{print $(NF-4)}'`
  PUB_IPKERR=`echo $PUBLIC_STR|awk '{print $(NF-3)}'`
  PUB_OPK=`echo $PUBLIC_STR|awk '{print $(NF-2)}'`
  PUB_OPKERR=`echo $PUBLIC_STR|awk '{print $(NF-1)}'`
       ;;
esac 
echo "PUB_IPK=$PUB_IPK"
echo "PUB_IPKERR=$PUB_IPKERR"
echo "PUB_OPK=$PUB_OPK"
echo "PUB_OPKERR=$PUB_OPKERR"
echo "PRIV_IPK=$PRIV_IPK"
echo "PRIV_IPKERR=$PRIV_IPKERR"
echo "PRIV_OPK=$PRIV_OPK"
echo "PRIV_OPKERR=$PRIV_OPKERR"