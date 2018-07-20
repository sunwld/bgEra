package com.collie.bgEra.cloudApp.ssh2Pool;

import java.io.IOException;

/**
 * 当通过SSH2连接远程服务器出现问题时，抛出此异常
 * @author sunwld
 *
 */
public class ConnectRemoteHostException extends IOException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ConnectRemoteHostException() {
  }

  public ConnectRemoteHostException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConnectRemoteHostException(Throwable cause) {
    super(cause);
  }

}
