package com.collie.bgEra.cloudApp.ssh2Pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class SshConnFactory implements PooledObjectFactory<SshSession>{
	
	private String ip;
	private int port;
	private String username;
	private String password;

	public SshConnFactory(String ip, int port, String username, String password) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	@Override
	public void activateObject(PooledObject<SshSession> arg0) throws Exception {
	}

	@Override
	public void destroyObject(PooledObject<SshSession> arg0) throws Exception {
		SshConnUtil.release(arg0.getObject());
	}

	@Override
	public PooledObject<SshSession> makeObject() throws Exception {
		SshSession sshSession = SshConnUtil.getSshSession(ip, port, username, password);
		return new DefaultPooledObject<SshSession>(sshSession);
	}

	@Override
	public void passivateObject(PooledObject<SshSession> arg0) throws Exception {
	}

	@Override
	public boolean validateObject(PooledObject<SshSession> arg0) {
		SshSession sshSession = arg0.getObject();
		return sshSession.validateConn();
	}

	@Override
	public String toString() {
		return "SshConnFactory [ip=" + ip + ", port=" + port + "]";
	}
}
