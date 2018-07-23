package com.collie.bgEra.cloudApp.ssh2Pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class SshConnFactory implements PooledObjectFactory<Ssh2Session>{
	
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
	public void activateObject(PooledObject<Ssh2Session> arg0) throws Exception {
	}

	@Override
	public void destroyObject(PooledObject<Ssh2Session> arg0) throws Exception {
		arg0.getObject().destory();
	}

	@Override
	public PooledObject<Ssh2Session> makeObject() throws Exception {
		Ssh2Session sshSession = new Ssh2Session(ip, port, username, password);
		return new DefaultPooledObject<Ssh2Session>(sshSession);
	}

	@Override
	public void passivateObject(PooledObject<Ssh2Session> arg0) throws Exception {
	}

	@Override
	public boolean validateObject(PooledObject<Ssh2Session> arg0) {
		Ssh2Session sshSession = arg0.getObject();
		return sshSession.validateConn();
	}

	@Override
	public String toString() {
		return "SshConnFactory [ip=" + ip + ", port=" + port + "]";
	}
}
