package com.collie.bgEra.cloudApp.ssh2Pool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SshSession {
	private Logger logger = Logger.getLogger("default");
	private Connection connection;
	private Session session;

	public SshSession(Connection connection, Session session) {
		this.connection = connection;
		this.session = session;
	}

	public SshSession(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public Session getSession() {
		return session;
	}

	public void closeSession() {
		SshConnUtil.release(session);
		session = null;
	}

	public void openSession() throws IOException {
		if(session == null){
			this.session = SshConnUtil.openSession(connection);
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
			closeSession();
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
			SshConnUtil.release(stdoutReader,stderrReader);
			closeSession();
		}

		return result;

	}

	@Override
	public String toString() {
		return String.format("ip : %s, port : %d", connection.getHostname(),connection.getPort());
	}



}
