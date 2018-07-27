package com.collie.bgEra.hpdc

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@RestController
@RequestMapping(Array("/hpdc"))
class Ct {
    private val logger: Logger = LoggerFactory.getLogger("opdc")


    @RequestMapping(Array("/q"))
    def scheduler = {

        new Tuple2(1,"String")
    }
}