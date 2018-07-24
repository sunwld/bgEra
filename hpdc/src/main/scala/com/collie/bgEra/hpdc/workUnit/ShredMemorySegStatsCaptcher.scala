package com.collie.bgEra.hpdc.workUnit

import com.collie.bgEra.cloudApp.dtsf.WorkUnitRunable
import com.collie.bgEra.cloudApp.dtsf.bean.WorkUnitInfo

class ShredMemorySegStatsCaptcher extends WorkUnitRunable {
  private val TOPIC = "hpdc-sharedmem"
  private val SHELL = "SHARED_MEM"
  override def runWork(workUnitInfo: WorkUnitInfo): Unit = {
    /**
      * OSTYPE:LINUX
      *
      * ------ Shared Memory Segments --------
      * key        shmid      owner      perms      bytes      nattch     status
      * 0x0113432a 196608     root       600        1000       6
      */


    /**
      * OSTYPE:AIX
      * IPC status from /dev/mem as of Tue Jul 24 16:19:40 BEIST 2018
      * T        ID     KEY        MODE       OWNER    GROUP  CREATOR   CGROUP NATTCH     SEGSZ  CPID  LPID   ATIME    DTIME    CTIME
      * Shared Memory:
      * m         0 0x5500517c --rw-r--r--     root   system     root   system      1    124064 2491096 48955546 23:57:02  9:52:00 23:57:02
      * m   1048578 0x78000042 --rw-rw-rw-     root   system     root   system      1  33554432 2162754 3213414 23:59:07 16:15:59 23:59:07
      * m   1048579 0x78000041 --rw-rw-rw-     root   system     root   system      1  33554432 2162754 3213414 23:59:07 16:15:59 23:59:07
      * m 160432132 0xfa00250a --rw-rw----     root      dba     root      dba      3  30435368 64029360 64029360  0:01:20  0:01:37 16:06:11
      * m 334495749 0xfa002507 --rw-rw----     root      dba     root      dba      3  30435368 64029360 58917474  0:01:20  0:01:37 16:06:11
      * m   1048584 0x0000cace --rw-rw-rw-     root   system     root   system      0         2 2360176 2360176 20:00:16 20:00:16  0:00:53
      * m 100663306 0x95ad1b88 --rw-r-----   oracle      dba   oracle      dba    557 68719611904 7274958 6949542 16:19:30 16:19:36  0:25:12
      */

    /**
      * OSTYPE:SOLARIS
      * IPC status from <running system> as of Tuesday, July 24, 2018 04:19:42 PM CST
      * T         ID      KEY        MODE        OWNER    GROUP  CREATOR   CGROUP NATTCH      SEGSZ   CPID  LPID   ATIME    DTIME    CTIME
      * Shared Memory:
      * m  486539323   0x70425e70 --rw-r-----   oracle asmadmin   oracle asmadmin   1434      16384 23161 18364 16:19:19 16:19:41  1:54:43
      * m  486539322   0x0        --rw-r-----   oracle asmadmin   oracle asmadmin   1434 106837311488 23161 18364 16:19:19 16:19:41  1:54:43
      * m  486539321   0x0        --rw-r-----   oracle asmadmin   oracle asmadmin   1434  536870912 23161 18364 16:19:19 16:19:41  1:54:43
      * m 1795162165   0x58371e3c --rw-r-----     grid oinstall     grid oinstall     31      16384 21518  2679 15:20:15 15:20:15  1:54:04
      * m  301989940   0x0        --rw-r-----     grid oinstall     grid oinstall     31 1132462080 21518  2679 15:20:15 15:20:15  1:54:04
      * m  301989939   0x0        --rw-r-----     grid oinstall     grid oinstall     31    6963200 21518  2679 15:20:15 15:20:15  1:54:04
      */

  }
}
