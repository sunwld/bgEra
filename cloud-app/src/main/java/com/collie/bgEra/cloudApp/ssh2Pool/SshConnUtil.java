package com.collie.bgEra.cloudApp.ssh2Pool;

import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SshConnUtil {
	private static Logger logger=Logger.getLogger("default");

	/**
	 * 根据主机名、端口、用户名、密码，获取一个到远程服务器的ssh连接的会话。
	 * @param hostname
	 * @param port
	 * @param username
	 * @param password
	 * @return SshSession 封装了ssh2 Connection和Session对象的对象
	 * @throws ConnectRemoteHostException
	 */
	public static SshSession getSshSession(String hostname, int port,String username, String password) throws ConnectRemoteHostException{

		try {
			/* 创建connection实例 */
			Connection connection = new Connection(hostname,port);

			/* 连接 */
			connection.connect();
			/* 验证.
			 * 如果抛出IOException异常，并且显示类似如下信息
			 * "Authentication method password not supported by the server at this stage."
			 * 请查看FAQ文件.
			 */
			boolean isAuthenticated = connection.authenticateWithPassword(username, password);

			if (isAuthenticated == false)
				throw new IOException("用户名密码认证失败：" + hostname);

			return new SshSession(connection);
		} catch (IOException e) {
			String message = String.format("Get SshSession failed, hostname=%s,port=%s,username=%s,password=%s", hostname,port,username,"******");
			logger.error(message, e);
			throw new ConnectRemoteHostException("连接远程服务器失败：" + hostname, e);
		}
	}

	/**
	 * 根据主机名、用户名、密码，获取一个到远程服务器的ssh连接的会话。端口号为22
	 * @param hostname
	 * @param username
	 * @param password
	 * @return SshSession 封装了ssh2 Connection和Session对象的对象
	 * @throws ConnectRemoteHostException
	 */
	public static SshSession getSshSession(String hostname, String username, String password) throws ConnectRemoteHostException{

		return getSshSession(hostname, 22, username, password);
	}

	/**
	 * 根据ssh2的Connection对象，获取Session会话对象
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	public static Session getSession(Connection connection) throws IOException{
		return connection.openSession();
	}
	/**
	 * 根据ssh2的Connection对象，获取Session会话对象
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	public static Session openSession(Connection connection) throws IOException{
		return connection.openSession();
	}

	/**
	 * 关闭Connection和Session
	 * @param session
	 * @param connection
	 */
	public static void release(Session session, Connection connection){
		if(session != null){
			session.close();
		}

		if(connection != null){
			connection.close();
		}
	}

	public static void release(SshSession sshSession){
		if(sshSession != null){
			release(sshSession.getSession(), sshSession.getConnection());
		}
	}

	/**
	 * 关闭Session
	 * @param session
	 */
	public static void release(Session session){
		release(session, null);
	}

	/**
	 * 关闭Connection
	 * @param connection
	 */
	public static void release(Connection connection){
		release(null, connection);
	}

	/**
	 * 关闭若干个Reader对象
	 * @param args
	 */
	public static void release(Reader ... args){
		if(args != null && args.length > 0){
			for(Reader reader : args){
				if(reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("run release failed",e);
						//e.printStackTrace();
					}
				}
			}
		}
	}

}
