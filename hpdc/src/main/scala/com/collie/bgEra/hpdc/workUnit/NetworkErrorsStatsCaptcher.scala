package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

class NetworkErrorsStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-neterrors"
  private val SHELL = "NETWORK_ERRORS"
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {

    /**
      * linux、aix关键字 error fail drop timeout bad
      * solaris 关键字 Err Error Fail Drop Overflow Timeout
      */

    /**
    Ip:
        374800206 total packets received
        0 forwarded
        0 incoming packets discarded
        369405514 incoming packets delivered
        366714684 requests sent out
        6 outgoing packets dropped
        10 dropped because of missing route
        4 fragments received ok
        8 fragments created
    Icmp:
        631 ICMP messages received
        4 input ICMP message failed.
        ICMP input histogram:
            destination unreachable: 81
            echo requests: 1
            echo replies: 549
        639 ICMP messages sent
        0 ICMP messages failed
        ICMP output histogram:
            destination unreachable: 81
            echo request: 557
            echo replies: 1
    IcmpMsg:
            InType0: 549
            InType3: 81
            InType8: 1
            OutType0: 1
            OutType3: 81
            OutType8: 557
    Tcp:
        247991 active connections openings
        171042 passive connection openings
        81105 failed connection attempts
        177914 connection resets received
        126 connections established
        369404024 segments received
        374283894 segments send out
        11357 segments retransmited
        0 bad segments received.
        178600 resets sent
    Udp:
        622 packets received
        64 packets to unknown port received.
        0 packet receive errors
        186138 packets sent
        0 receive buffer errors
        0 send buffer errors
    UdpLite:
    TcpExt:
        475 invalid SYN cookies received
        8 resets received for embryonic SYN_RECV sockets
        1964 TCP sockets finished time wait in fast timer
        3024734 delayed acks sent
        24 delayed acks further delayed because of locked socket
        Quick ack mode was activated 4286 times
        31994792 packets directly queued to recvmsg prequeue.
        402860 bytes directly in process context from backlog
        149409760 bytes directly received in process context from prequeue
        302439654 packet headers predicted
        256327 packets header predicted and directly queued to user
        6088541 acknowledgments not containing data payload received
        283602943 predicted acknowledgments
        634 times recovered from packet loss by selective acknowledgements
        Detected reordering 2 times using FACK
        4 congestion windows fully recovered without slow start
        597 congestion windows recovered without slow start by DSACK
        84 congestion windows recovered without slow start after partial ack
        TCPLostRetransmit: 2
        68 timeouts after SACK recovery
        636 fast retransmits
        2 forward retransmits
        5 retransmits in slow start
        328 other TCP timeouts
        TCPLossProbes: 11843
        TCPLossProbeRecovery: 6959
        4 SACK retransmits failed
        4337 DSACKs sent for old packets
        7776 DSACKs received
        146 DSACKs for out of order packets received
        165588 connections reset due to unexpected data
        48 connections reset due to early user close
        206 connections aborted due to timeout
        TCPDSACKIgnoredOld: 2
        TCPDSACKIgnoredNoUndo: 3102
        TCPSpuriousRTOs: 70
        TCPSackShifted: 57
        TCPSackMerged: 26
        TCPSackShiftFallback: 11615
        TCPDeferAcceptDrop: 6
        TCPRcvCoalesce: 2555160
        TCPOFOQueue: 39525
        TCPChallengeACK: 1
        TCPSpuriousRtxHostQueues: 71
        TCPAutoCorking: 1129839
        TCPFromZeroWindowAdv: 215
        TCPToZeroWindowAdv: 215
        TCPWantZeroWindowAdv: 1497
        TCPSynRetrans: 24
        TCPOrigDataSent: 337709147
        TCPHystartTrainDetect: 328
        TCPHystartTrainCwnd: 7383
        TCPHystartDelayDetect: 8
        TCPHystartDelayCwnd: 229
        TCPACKSkippedSeq: 209
    IpExt:
        InMcastPkts: 622
        OutMcastPkts: 36
        InBcastPkts: 2556
        InOctets: 89442953652
        OutOctets: 59599425660
        InMcastOctets: 106284
        OutMcastOctets: 5746
        InBcastOctets: 838368
        InNoECTPkts: 393945662
      */


    /**
    OSTYPE:AIX
    icmp:
            4069305 calls to icmp_error
            0 errors not generated because old message was icmp
            Output histogram:
                    echo reply: 320796
                    destination unreachable: 4069305
                    echo: 628309
                    timestamp reply: 1
            0 messages with bad code fields
            0 messages < minimum length
            0 bad checksums
            0 messages with bad length
            Input histogram:
                    echo reply: 628110
                    destination unreachable: 4070754
                    echo: 320796
                    time exceeded: 769
                    timestamp: 1
                    address mask request: 2
            320797 message responses generated
    igmp:
            0 messages received
            0 messages received with too few bytes
            0 messages received with bad checksum
            0 membership queries received
            0 membership queries received with invalid field(s)
            0 membership reports received
            0 membership reports received with invalid field(s)
            0 membership reports received for groups to which we belong
            13 membership reports sent
    tcp:
            2229689213 packets sent
                    3064597202 data packets (768862559 bytes)
                    20559568 data packets (3867788765 bytes) retransmitted
                    377786932 ack-only packets (330392300 delayed)
                    0 URG only packets
                    613 window probe packets
                    3042688217 window update packets
                    38050400 control packets
                    0 large sends
                    0 bytes sent using largesend
                    0 bytes is the biggest largesend
            4245933464 packets received
                    2431443797 acks (for 805530233 bytes)
                    212569079 duplicate acks
                    0 acks for unsent data
                    3660288969 packets (3900157208 bytes) received in-sequence
                    1432325 completely duplicate packets (32376620 bytes)
                    2 old duplicate packets
                    13077 packets with some dup. data (943675 bytes duped)
                    6875968 out-of-order packets (4164489396 bytes)
                    556 packets (556 bytes) of data after window
                    556 window probes
                    1199111682 window update packets
                    38052 packets received after close
                    0 packets with bad hardware assisted checksum
                    9 discarded for bad checksums
                    0 discarded for bad header offset fields
                    0 discarded because packet too short
                    38818 discarded by listeners
                    64 discarded due to listener's queue full
                    2772981763 ack packet headers correctly predicted
                    3379408554 data packet headers correctly predicted
            2220648 connection requests
            14792113 connection accepts
            16943469 connections established (including accepts)
            17362363 connections closed (including 114020 drops)
            0 connections with ECN capability
            0 times responded to ECN
            69120 embryonic connections dropped
            2085852369 segments updated rtt (of 2092185559 attempts)
            0 segments with congestion window reduced bit set
            0 segments with congestion experienced bit set
            0 resends due to path MTU discovery
            7285 path MTU discovery terminations due to retransmits
            384370 retransmit timeouts
                    157 connections dropped by rexmit timeout
            6283635 fast retransmits
                    6374 when congestion window less than 4 segments
            11520587 newreno retransmits
            95194 times avoided false fast retransmits
            143 persist timeouts
                    0 connections dropped due to persist timeout
            2356199 keepalive timeouts
                    0 keepalive probes sent
                    9225 connections dropped by keepalive
            0 times SACK blocks array is extended
            0 times SACK holes array is extended
            72 packets dropped due to memory allocation failure
            127 connections in timewait reused
            0 delayed ACKs for SYN
            0 delayed ACKs for FIN
            0 send_and_disconnects
            0 spliced connections
            0 spliced connections closed
            0 spliced connections reset
            0 spliced connections timeout
            0 spliced connections persist timeout
            0 spliced connections keepalive timeout
            0 TCP checksum offload disabled during retransmit
            2661 Connections dropped due to bad ACKs
            0 fastpath loopback connection
            0 fastpath loopback sent packet (0 byte)
            0 fastpath loopback received packet (0 byte)
    udp:
            1225766753 datagrams received
            0 incomplete headers
            0 bad data length fields
            0 bad checksums
            4069894 dropped due to no socket
            2573886 broadcast/multicast datagrams dropped due to no socket
            574 socket buffer overflows
            1219122399 delivered
            3600198959 datagrams output
    ip:
            2376045235 total packets received
            0 bad header checksums
            0 with size smaller than minimum
            0 with data size < data length
            0 with header length < data size
            0 with data length < header length
            0 with bad options
            0 with incorrect version number
            713353414 fragments received
            0 fragments dropped (dup or out of space)
            2692 fragments dropped after timeout
            3818111594 packets reassembled ok
            1177681522 packets for this host
            4071184 packets for unknown/unsupported protocol
            0 packets forwarded
            256663 packets not forwardable
            0 redirects sent
            1542682435 packets sent from this host
            4117 packets sent with fabricated ip header
            0 output packets dropped due to no bufs, etc.
            0 output packets discarded due to no route
            2927703919 output datagrams fragmented
            3620576670 fragments created
            3816 datagrams that can't be fragmented
            3825995 IP Multicast packets dropped due to no receiver
            0 successful path MTU discovery cycles
            0 path MTU rediscovery cycles attempted
            0 path MTU discovery no-response estimates
            0 path MTU discovery response timeouts
            0 path MTU discovery decreases detected
            0 path MTU discovery packets sent
            0 path MTU discovery memory allocation failures
            0 ipintrq overflows
            0 with illegal source
            0 packets processed by threads
            0 packets dropped by threads
            0 packets dropped due to the full socket receive buffer
            0 dead gateway detection packets sent
            0 dead gateway detection packet allocation failures
            0 dead gateway detection gateway allocation failures
            0 incoming packets dropped due to MLS filters
            0 packets not sent due to MLS filters

    ipv6:
            630 total packets received
            Input histogram:
                    TCP: 10
                    UDP: 589
                    ICMP v6: 25
            0 with size smaller than minimum
            0 with data size < data length
            0 with incorrect version number
            0 with illegal source
            0 input packets without enough memory
            0 fragments received
            0 fragments dropped (dup or out of space)
            0 fragments dropped after timeout
            0 packets reassembled ok
            624 packets for this host
            0 packets for unknown/unsupported protocol
            0 packets forwarded
            6 packets not forwardable
            0 too big packets not forwarded
            624 packets sent from this host
            0 packets sent with fabricated ipv6 header
            0 output packets dropped due to no bufs, etc.
            0 output packets without enough memory
            85512 output packets discarded due to no route
            0 output datagrams fragmented
            0 fragments created
            0 packets dropped due to the full socket receive buffer
            0 packets not delivered due to bad raw IPv6 checksum
            0 incoming packets dropped due to MLS filters
            0 packets not sent due to MLS filters
    icmpv6:
            589 calls to icmp6_error
            0 errors not generated because old message was icmpv6
            Output histogram:
                    unreachable: 25
                    packets too big: 0
                    time exceeded: 0
                    parameter problems: 0
                    redirects: 0
                    echo requests: 0
                    echo replies: 0
                    group queries: 0
                    group reports: 0
                    group terminations: 0
                    router solicitations: 0
                    router advertisements: 0
                    neighbor solicitations: 0
                    neighbor advertisements: 0
            0 messages with bad code fields
            0 messages < minimum length
            0 bad checksums
            0 messages with bad length
            Input histogram:
                    unreachable: 25
                    packets too big: 0
                    time exceeded: 0
                    parameter problems: 0
                    echo requests: 0
                    echo replies: 0
                    group queries: 0
                            bad group queries: 0
                    group reports: 0
                            bad group reports: 0
                            our groups' reports: 0
                    group terminations: 0
                    bad group terminations: 0
                    router solicitations: 0
                    bad router solicitations: 0
                    router advertisements: 0
                    bad router advertisements: 0
                    neighbor solicitations: 0
                    bad neighbor solicitations: 0
                    neighbor advertisements: 0
                    bad neighbor advertisements: 0
                    redirects: 0
                    bad redirects: 0
                    mobility calls when not started: 0
                    home agent address discovery requests: 0
                    bad home agent address discovery requests: 0
                    home agent address discovery replies: 0
                    bad home agent address discovery replies: 0
                    prefix solicitations: 0
                    bad prefix solicitations: 0
                    prefix advertisements: 0
                    bad prefix advertisements: 0
            0 message responses generated
      */

    /**
    OSTYPE:SOLARIS

    RAWIP   rawipInDatagrams    =   278     rawipInErrors       =     0
            rawipInCksumErrs    =     0     rawipOutDatagrams   =    96
            rawipOutErrors      =     0

    UDP     udpInDatagrams      =104368638721       udpInErrors         =     0
            udpOutDatagrams     =95694807515        udpOutErrors        =     0

    TCP     tcpRtoAlgorithm     =     4     tcpRtoMin           =   200
            tcpRtoMax           = 60000     tcpMaxConn          =    -1
            tcpActiveOpens      =7793184    tcpPassiveOpens     =12748670
            tcpAttemptFails     =  2503     tcpEstabResets      = 24792
            tcpCurrEstab        =  1395     tcpOutSegs          =302439064228
            tcpOutDataSegs      =2090573928 tcpOutDataBytes     =3659633890
            tcpRetransSegs      =1234204    tcpRetransBytes     =1111530620
            tcpOutAck           =3568237586 tcpOutAckDelayed    =1532150194
            tcpOutUrg           =     3     tcpOutWinUpdate     =1071585
            tcpOutWinProbe      =    78     tcpOutControl       =41272944
            tcpOutRsts          =190984     tcpOutFastRetrans   =     0
            tcpInSegs           =184109315641
            tcpInAckSegs        =     0     tcpInAckBytes       =526925481
            tcpInDupAck         =22702309   tcpInAckUnsent      =     0
            tcpInInorderSegs    =345952460  tcpInInorderBytes   =4244050517
            tcpInUnorderSegs    =176815     tcpInUnorderBytes   =239242850
            tcpInDupSegs        =111216     tcpInDupBytes       =17444459
            tcpInPartDupSegs    =     2     tcpInPartDupBytes   =  1193
            tcpInPastWinSegs    =     1     tcpInPastWinBytes   =1765840631
            tcpInWinProbe       =     0     tcpInWinUpdate      =    29
            tcpInClosed         =  3249     tcpRttNoUpdate      =22877777
            tcpRttUpdate        =3070016804 tcpTimRetrans       =795414
            tcpTimRetransDrop   =  3695     tcpTimKeepalive     =19143379
            tcpTimKeepaliveProbe=3679717    tcpTimKeepaliveDrop =  2915
            tcpListenDrop       =     0     tcpListenDropQ0     =     0
            tcpHalfOpenDrop     =     0     tcpOutSackRetrans   =276971

    IPv4    ipForwarding        =     2     ipDefaultTTL        =   255
            ipInReceives        =436389720  ipInHdrErrors       =     0
            ipInAddrErrors      =     0     ipInCksumErrs       =     2
            ipForwDatagrams     =     0     ipForwProhibits     = 12951
            ipInUnknownProtos   =     0     ipInDiscards        =     4
            ipInDelivers        =2142824096 ipOutRequests       =2297231777
            ipOutDiscards       =1243817    ipOutNoRoutes       =     2
            ipReasmTimeout      =    15     ipReasmReqds        =2293219011
            ipReasmOKs          =2301299282 ipReasmFails        =115947
            ipReasmDuplicates   = 12075     ipReasmPartDups     =     2
            ipFragOKs           =1015010638 ipFragFails         =     0
            ipFragCreates       =1853742743 ipRoutingDiscards   =     0
            tcpInErrs           =     0     udpNoPorts          =32356273
            udpInCksumErrs      =  6538     udpInOverflows      =576256
            rawipInOverflows    =     0     ipsecInSucceeded    =     0
            ipsecInFailed       =     0     ipInIPv6            =     0
            ipOutIPv6           =     0     ipOutSwitchIPv6     =     0

    IPv6    ipv6Forwarding      =     2     ipv6DefaultHopLimit =     0
            ipv6InReceives      =207293994  ipv6InHdrErrors     =     0
            ipv6InTooBigErrors  =     0     ipv6InNoRoutes      =     0
            ipv6InAddrErrors    =     0     ipv6InUnknownProtos =     0
            ipv6InTruncatedPkts =     0     ipv6InDiscards      =     0
            ipv6InDelivers      =207281793  ipv6OutForwDatagrams=     0
            ipv6OutRequests     =207293994  ipv6OutDiscards     =     0
            ipv6OutNoRoutes     =     0     ipv6OutFragOKs      =     0
            ipv6OutFragFails    =     0     ipv6OutFragCreates  =     0
            ipv6ReasmReqds      =     0     ipv6ReasmOKs        =     0
            ipv6ReasmFails      =     0     ipv6InMcastPkts     =     0
            ipv6OutMcastPkts    =     0     ipv6ReasmDuplicates =     0
            ipv6ReasmPartDups   =     0     ipv6ForwProhibits   =     0
            udpInCksumErrs      =     0     udpInOverflows      =     0
            rawipInOverflows    =     0     ipv6InIPv4          =     0
            ipv6OutIPv4         =     0     ipv6OutSwitchIPv4   =     0

    ICMPv4  icmpInMsgs          = 28116     icmpInErrors        =     0
            icmpInCksumErrs     =     0     icmpInUnknowns      =     5
            icmpInDestUnreachs  = 24778     icmpInTimeExcds     =  1323
            icmpInParmProbs     =     0     icmpInSrcQuenchs    =     0
            icmpInRedirects     =    41     icmpInBadRedirects  =    41
            icmpInEchos         =  1927     icmpInEchoReps      =     1
            icmpInTimestamps    =     0     icmpInTimestampReps =     0
            icmpInAddrMasks     =     8     icmpInAddrMaskReps  =     0
            icmpInFragNeeded    =     0     icmpOutMsgs         = 81750
            icmpOutDrops        =     0     icmpOutErrors       =     0
            icmpOutDestUnreachs = 29366     icmpOutTimeExcds    = 50443
            icmpOutParmProbs    =     0     icmpOutSrcQuenchs   =     0
            icmpOutRedirects    =     0     icmpOutEchos        =     0
            icmpOutEchoReps     =  1927     icmpOutTimestamps   =     0
            icmpOutTimestampReps=     0     icmpOutAddrMasks    =     0
            icmpOutAddrMaskReps =     8     icmpOutFragNeeded   =     0
            icmpInOverflows     =  4873

    ICMPv6  icmp6InMsgs         = 24402     icmp6InErrors       =     0
            icmp6InDestUnreachs = 12201     icmp6InAdminProhibs =     0
            icmp6InTimeExcds    =     0     icmp6InParmProblems =     0
            icmp6InPktTooBigs   =     0     icmp6InEchos        =     0
            icmp6InEchoReplies  =     0     icmp6InRouterSols   =     0
            icmp6InRouterAds    =     0     icmp6InNeighborSols =     0
            icmp6InNeighborAds  =     0     icmp6InRedirects    =     0
            icmp6InBadRedirects =     0     icmp6InGroupQueries =     0
            icmp6InGroupResps   =     0     icmp6InGroupReds    =     0
            icmp6InOverflows    =     0
            icmp6OutMsgs        = 12201     icmp6OutErrors      =     0
            icmp6OutDestUnreachs= 12201     icmp6OutAdminProhibs=     0
            icmp6OutTimeExcds   =     0     icmp6OutParmProblems=     0
            icmp6OutPktTooBigs  =     0     icmp6OutEchos       =     0
            icmp6OutEchoReplies =     0     icmp6OutRouterSols  =     0
            icmp6OutRouterAds   =     0     icmp6OutNeighborSols=     0
            icmp6OutNeighborAds =     0     icmp6OutRedirects   =     0
            icmp6OutGroupQueries=     0     icmp6OutGroupResps  =     0
            icmp6OutGroupReds   =     0

    IGMP:
              0 messages received
              0 messages received with too few bytes
              0 messages received with bad checksum
              0 membership queries received
              0 membership queries received with invalid field(s)
              0 membership reports received
              0 membership reports received with invalid field(s)
              0 membership reports received for groups to which we belong
             12 membership reports sent

    SCTP    sctpRtoAlgorithm    =  vanj     sctpRtoMin          =  1000
            sctpRtoMax          = 60000     sctpRtoInitial      =  3000
            sctpMaxAssocs       =    -1     sctpValCookieLife   = 60000
            sctpMaxInitRetr     =     8     sctpCurrEstab       =     0
            sctpActiveEstab     =     0     sctpPassiveEstab    =     0
            sctpAborted         =     0     sctpShutdowns       =     0
            sctpOutOfBlue       =     0     sctpChecksumError   =     0
            sctpOutCtrlChunks   =     0     sctpOutOrderChunks  =     0
            sctpOutUnorderChunks=     0     sctpRetransChunks   =     0
            sctpOutAck          =     0     sctpOutAckDelayed   =     0
            sctpOutWinUpdate    =     0     sctpOutFastRetrans  =     0
            sctpOutWinProbe     =     0     sctpInCtrlChunks    =     0
            sctpInOrderChunks   =     0     sctpInUnorderChunks =     0
            sctpInAck           =     0     sctpInDupAck        =     0
            sctpInAckUnsent     =     0     sctpFragUsrMsgs     =     0
            sctpReasmUsrMsgs    =     0     sctpOutSCTPPkts     =     0
            sctpInSCTPPkts      =     0     sctpInInvalidCookie =     0
            sctpTimRetrans      =     0     sctpTimRetransDrop  =     0
            sctpTimHearBeatProbe=     0     sctpTimHearBeatDrop =     0
            sctpListenDrop      =     0     sctpInClosed        =     0


    SDP     sdpActiveOpens      =     0     sdpCurrEstab        =     0
            sdpPrFails          =     0     sdpRejects          =     0
            sdpInSegs           =     0
            sdpOutSegs          =     0
            sdpInDataBytes      =     0
            sdpOutDataBytes     =     0
      */


  }
}
