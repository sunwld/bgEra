package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

class ProcessListStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-processlist"
  private val SHELL = "PROCESS_LIST"
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
    /**
      * OSTYPE:LINUX
      * S   UID   PID  PPID  C PRI  NI   RSS    SZ WCHAN  TTY          TIME CMD
      * S     0     1     0  0  80   0  5084 48023 ep_pol ?        00:03:40 systemd
      * S     0     2     0  0  80   0     0     0 kthrea ?        00:00:00 kthreadd
      * S     0     3     2  0  80   0     0     0 smpboo ?        00:00:01 ksoftirqd/0
      * S     0     5     2  0  60 -20     0     0 worker ?        00:00:00 kworker/0:0H
      * S     0     7     2  0 -40   -     0     0 smpboo ?        00:00:05 migration/0
      * S     0     8     2  0  80   0     0     0 rcu_gp ?        00:00:00 rcu_bh
      * S     0     9     2  0  80   0     0     0 rcu_gp ?        00:25:22 rcu_sched
      * S     0    10     2  0 -40   -     0     0 smpboo ?        00:00:11 watchdog/0
      * *** ***
      * S     0 29476     2  0  80   0     0     0 worker ?        00:00:00 kworker/8:0
      * S  1002 29628     1  0  80   0 543040 2084874 futex_ ?     00:00:12 java
      * S     0 29966     2  0  60 -20     0     0 worker ?        00:00:00 kworker/15:1H
      * S     0 30674     2  0  80   0     0     0 worker ?        00:00:00 kworker/10:2
      * S     0 30675     2  0  80   0     0     0 worker ?        00:00:00 kworker/5:2
      * S     0 30697     2  0  60 -20     0     0 loop_t ?        00:00:02 loop0
      * *** ***
      * R     0 59402     2  0  80   0     0     0 -      ?        00:00:18 kworker/6:0
      * S     0 60016     2  0  80   0     0     0 worker ?        00:00:00 kworker/15:0
      * S     0 62664     2  0  80   0     0     0 worker ?        00:00:00 kworker/12:2
      * S     0 63268     2  0  80   0     0     0 worker ?        00:00:10 kworker/2:0
      * S     0 64225     2  0  80   0     0     0 worker ?        00:00:00 kworker/14:0
      */

    /**
      * OSTYPE:AIX
      * F S UID      PID     PPID   C PRI NI ADDR    SZ    WCHAN    TTY  TIME CMD
      * 200003 A   0        1        0   0  60 20 10e018e480   764               - 83:13 init
      * 40401 A   0  2162754        1   4  62 20 c0a8c480 19276 f1000a0506e98fb0      - 864:28 topasrec
      * 240001 A   0  3342454  3932826   0  60 20 7deaff480  1160 f1000e002b0da0c8      -  0:00 sshd
      * 240001 A 111  3670018        1   6  63 20 17e6dfe590 97752 f1000e00005e18c8      - 263:32 oracle
      * 240001 A   0  3801118  3932826   0  60 20 12f1fa3480  1160 f1000e00062c48c8      -  0:00 sshd
      * 240001 A 111  3997786        1   0  60 20 1529dd4590 97404 f1000e0001fbf0c8      - 91:26 oracle
      * 240001 A 111  4260050        1   0  60 20 31688590 96108 f1000e0003bf70c8      -  0:01 oracle
      * 240001 A 111  4325536        1   0  60 20 1e7e56d590 95180               -  1:53 oracle
      * 240001 A 111  4391108        1   0  60 20 14febc1590 96104 f1000e00043ea0c8      -  0:00 oracle
      * 40001 A 160  4718682 53870758   0  60 20 166b5e5480  1528               -  0:00 sshd
      * 240001 A 111  6488098        1   0  60 20 11b639f590 95148               -  0:00 oracle
      * 240001 A 111  7536808        1   2  61 20 1c72142590 98692 f1000e000365a8c8      -  3:08 oracle
      * 240001 A   0  7667942  3932826   0  60 20 1a7c721480  1164 f1000e000052fcc8      -  0:00 sshd
      * 40001 A 160  7864510 16974218   0  60 20 1d4a755480  1532               -  0:00 sshd
      * 40001 A 160  8323162 65405428   0  60 20 1ab8d2f480  1532               -  0:00 sshd
      * *** ***
      * 240001 A 111  9964192        1   0  60 20 1970919590 98728 f1000e002b4ef8c8      - 168:05 oracle
      * 240001 A 111 10029596        1   0  60 20 11c3790590 98344 f1000e00036648c8      - 11:43 oracle
      * 240001 A 111 10750674        1   0  60 20 3c5cb1590 96104 f1000e00025fa8c8      -  0:00 oracle
      * 240001 A 111 10947238        1   0  60 20 1610be7590 99716 f1000e00037660c8      -  0:22 oracle
      * 40001 A 160 11602584  5112774   0  60 20 a99228480  1540               -  0:00 sshd
      * 240001 A   0 13568692  3932826   0  60 20 ee0464480  1160 f1000e000fdc70c8      -  0:00 sshd
      * 240001 A 111 16190000        1   0  60 20 1b1633f590 98092 f1000e00004480c8      -  0:23 oracle
      * 240001 A   0 17173154  3932826   0  60 20 364ebf480  1160 f1000e000fe994c8      -  0:00 sshd
      */

    /**
      * OSTYPE:SOLARIS
      * S    UID   PID  PPID   C PRI NI   RSS     SZ    WCHAN TTY         TIME CMD
      * T      0     0     0   0   0 SY     0      0          ?           0:18 sched
      * S      0     5     0   0   0 SD     0      0        ? ?        5866:31 zpool-rp
      * S      0     6     0   0   0 SD     0      0        ? ?         130:23 kmem_tas
      * S      0     1     0   0  40 20   968   3168        ? ?          53:39 init
      * S      0     2     0   0   0 SY     0      0        ? ?           0:00 pageout
      * S      0     3     0   0   0 SY     0      0        ? ?        16507:01 fsflush
      * S      0     7     0   0   0 SY     0      0        ? ?          28:43 intrd
      * S      0     8     0   0   0 SY     0      0        ? ?        20853:07 vmtasks
      * S      0   855     1   0  40 20  1192   9264        ? ?           0:17 cron
      * S      0    15     1   0  40 20 101112 144704        ? ?          41:51 svc.star
      * S      0    17     1   0  40 20 18248  51440        ? ?         184:03 svc.conf
      * S      0   863   861   0  40 20  3400  14688        ? ?          18:10 automoun
      * S      0   864     1   0  40 20 13000  51440        ? ?          17:49 inetd
      * S     17    52     1   0  40 20  4272   6600        ? ?          38:58 netcfgd
      * S      0   109     1   0  40 20   984  10584        ? ?          77:05 in.mpath
      * S      0  2032     1   0  40 20  5224  10616        ? ?          45:15 sendmail
      * *** ***
      * S    101 28333     1   0  39 20 63150616 105110992        ? ?           0:00 oracle
      * S    101 24928     1   0  39 20 63151176 105112784        ? ?           0:00 oracle
      * S    101 23330     1   0  39 20 63154832 105114920        ? ?           2:25 oracle
      * S    101 10475     1   0  39 20 63151880 105112208        ? ?          13:45 oracle
      * S    101  2648     1   0  39 20 63146840 105110992        ? ?           0:00 oracle
      * *** ***
      * S    101 24913     1   0  39 20 63150048 105110992        ? ?           0:00 oracle
      * S    100 17800     1   0  40 20 34904  67008        ? ?         257:25 tnslsnr
      * S    101 23906 23815   0  40 20 14677832 105132144        ? ?         115:14 oracle
      * S    101 24930     1   0  39 20 63155344 105114920        ? ?          56:44 oracle
      * S    101 28859     1   0  39 20 63155016 105113552        ? ?           3:27 oracle
      */

  }
}
