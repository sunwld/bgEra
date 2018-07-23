package com.collie.bgEra.cloudApp.ssh2Pool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class Ssh2Session {
	private Logger logger = LoggerFactory.getLogger("ssh2Pool");
	private Connection connection;
	private Session session;
	private Ssh2SessionPool pool;

	public Ssh2Session(String hostname, int port, String username, String password) throws ConnectRemoteHostException {
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

			if (isAuthenticated == false){
				throw new IOException("username or password authentication failed：" + hostname);
			}
			this.connection = connection;
		} catch (IOException e) {
			String message = String.format("Get Ssh2Session failed, hostname=%s,port=%s,username=%s,password=%s", hostname,port,username,"******");
			logger.error(message, e);
			throw new ConnectRemoteHostException("Failed to connect to remote host：" + hostname, e);
		}
	}
	public Ssh2Session(String hostname, String username, String password) throws ConnectRemoteHostException {
		this(hostname,22,username,password);
	}


	public void setPool(Ssh2SessionPool pool) {
		this.pool = pool;
	}

	public Connection getConnection() {
		return connection;
	}

	public Session getSession() {
		return session;
	}

	private void openSession() throws IOException {
		if(session == null){
			this.session = connection.openSession();
		}
	}

	public void close(){
		if(pool == null){
			logger.debug("destory a ssh2 connection:" + this);
			destory();
		}else {
			logger.debug("release a ssh2 connection back to pool:" + this);
			pool.returnObject(this);
			this.pool = null;
		}
	}

	public void destory(){
		destorySession();
		destoryConnection();
		this.pool = null;
	}

	private void destorySession(){
		if(session != null){
			session.close();
			session = null;
		}
	}
	private void destoryConnection(){
		if(connection != null){
			connection.close();
			connection = null;
		}
	}

	/**
	 *
	 */
	public boolean validateConn(){
		boolean flag = false;
		String command = "echo \"abc\"";
		try {
			if(session == null){
				openSession();
			}
			session.execCommand(command);
			session.getStdout();
			session.getStderr();

			flag = true;
		} catch (Exception e) {
			flag = false;
			logger.error("ssh connection Authentication Failed, command is:" + command, e);
		}finally {
			destorySession();
		}
		return flag;
	}

	/**
	 * 到远程服务器执行命令或脚本，将执行结果放到Map中并返回
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public SshResult execCommand(String command){
		//用来存放结果
		SshResult result = new SshResult();
		//用于存放执行命令或脚本产生的stdout数据和stderr数据
		BufferedReader stdoutReader = null;
		BufferedReader stderrReader = null;

		try {
			if(session == null){
				openSession();
			}
			session.execCommand(command);

			//获取结果码

			//获取封装了stdout和stderr的结果的输入流
			//这里new StreamGobbler(session.getStdout())和new StreamGobbler(session.getStderr())是并行线程
			InputStream stdout = new StreamGobbler(session.getStdout());
			InputStream stderr = new StreamGobbler(session.getStderr());

			stdoutReader = new BufferedReader(new InputStreamReader(stdout));
			stderrReader = new BufferedReader(new InputStreamReader(stderr));

			//将结果转为String，并放到结果集中
			List<String> resList = new ArrayList<String>();
			while (true) {
				String line = stdoutReader.readLine();
				if (line == null){
					break;
				}
				resList.add(line);
			}
			result.setStrout(resList);

			resList = new ArrayList<String>();
			while (true) {
				String line = stderrReader.readLine();
				if (line == null){
					break;
				}
				resList.add(line);
			}

			result.setStderr(resList);
			result.setExitCode(session.getExitStatus());
		} catch (Exception e) {
			result.setExitCode(1);
			logger.error("execu command Exception:" + command, e);
		}finally {
			release(stdoutReader,stderrReader);
			destorySession();
		}
		return result;
	}

	/**
	 * 关闭若干个Reader对象
	 * @param args
	 */
	public void release(Reader... args){
		if(args != null && args.length > 0){
			for(Reader reader : args){
				if(reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						logger.error("ssh2Session release" + reader + "failed",e);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return String.format("ip : %s, port : %d", connection.getHostname(),connection.getPort());
	}
}
