package com.collie.bgEra.cloudApp.ssh2Pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class Ssh2SessionPool extends GenericObjectPool<Ssh2Session> {
  public Ssh2SessionPool(PooledObjectFactory<Ssh2Session> factory) {
    super(factory);
  }

  public Ssh2SessionPool(PooledObjectFactory<Ssh2Session> factory, GenericObjectPoolConfig config) {
    super(factory, config);
  }

  public Ssh2SessionPool(PooledObjectFactory<Ssh2Session> factory, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
    super(factory, config, abandonedConfig);
  }

  @Override
  public Ssh2Session borrowObject() throws Exception {
    return borrowObject(getMaxWaitMillis());
  }

  @Override
  public Ssh2Session borrowObject(long borrowMaxWaitMillis) throws Exception {
    Ssh2Session ssh2Session = super.borrowObject(borrowMaxWaitMillis);
    ssh2Session.setPool(this);
    return ssh2Session;
  }
}
