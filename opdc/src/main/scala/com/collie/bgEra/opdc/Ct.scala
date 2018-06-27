package com.collie.bgEra.opdc

import java.sql.{PreparedStatement, ResultSet}
import java.util.Properties

import com.alibaba.druid.pool.DruidDataSource
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ResponseBody
import redis.clients.jedis.{HostAndPort, JedisCluster, JedisPoolConfig}
//import com.collie.bgEra.commons.appm.ZApplicationManager
import javax.sql.DataSource
import org.springframework.web.bind.annotation.{RequestMapping, RestController}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate

@RestController
@RequestMapping(Array("/opdc"))
class Ct {
    private val logger: Logger = LoggerFactory.getLogger("appm")

//    @Autowired
//    var redisTemplate: RedisTemplate[_,_] = _

    @Autowired
    val service : TestService = null
    implicit def ds2DruidDS(ds:DataSource) = ds.asInstanceOf[DruidDataSource]

      /**
      * 孙文龙
      * @return
      */
    @RequestMapping(Array("/users"))
    @ResponseBody
    def getUsers: Any = {
        logger.info("dddddd")
        val connPros = new Properties()
        connPros.setProperty("druid.driverClassName","oracle.jdbc.OracleDriver")
        connPros.setProperty("druid.username","scifmation")
        connPros.setProperty("druid.password","kxht#123")
        connPros.setProperty("druid.url","jdbc:oracle:thin:@133.96.9.118:7521:orcl")
        connPros.setProperty("druid.name","testDS")
        val dataSource = new DruidDataSource()
        dataSource.configFromPropety(connPros)

        val conn = dataSource.getConnection()
        val statement: PreparedStatement = conn.prepareStatement("SELECT * from mt_user")
        val set: ResultSet = statement.executeQuery()
        while(set.next()){
            val name = set.getString("USERNAME")
            val passwd = set.getString("PASSWORD")
            println(s"username: ${name} , password : ${passwd}")
        }
        set.close()
        conn.close()
        statement.close()
        dataSource.close()
        "success"
    }
}