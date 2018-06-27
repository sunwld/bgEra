package com.collie.bgEra.cloudApp.appm

trait AppManagerStandardSkill {

  def suspend(clusterInfo: ClusterInfo)

  def resume(clusterInfo: ClusterInfo)

  def close(clusterInfo: ClusterInfo)

  def reconstruction(clusterInfo: ClusterInfo)

  def reallocation(clusterInfo: ClusterInfo)
}

