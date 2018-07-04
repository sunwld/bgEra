package com.collie.bgEra.cloudApp.appm

class AppClusterFatalException(message: String, cause: Throwable) extends Exception(message: String, cause: Throwable) {
  def this(message: String){
    this(message,null)
  }
}
