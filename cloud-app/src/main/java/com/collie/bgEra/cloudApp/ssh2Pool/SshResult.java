package com.collie.bgEra.cloudApp.ssh2Pool;

import java.util.List;

public class SshResult {
  private Integer exitCode = -1;
  private List<String> strout;
  private List<String> stderr;

  public Integer getExitCode() {
    return exitCode;
  }
  public void setExitCode(Integer exitCode) {
    this.exitCode = exitCode;
  }
  public List<String> getStrout() {
    return strout;
  }
  public void setStrout(List<String> strout) {
    this.strout = strout;
  }
  public List<String> getStderr() {
    return stderr;
  }
  public void setStderr(List<String> stderr) {
    this.stderr = stderr;
  }
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("SshResult [ exitCode=" + exitCode + ", strout=\r\n");
    for(String line : strout){
      str.append(line + "\r\n");
    }
    str.append(", stderr=\r\n");
    for(String line : stderr){
      str.append(line + "\r\n");
    }
    str.append("\r\n]");
    return str.toString();
  }

}
