package com.zdawn.commons.mutex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlDBMutexLock {

	private static final Logger log = LoggerFactory.getLogger(OracleDBMutexLock.class);
	
	private DataSource dataSource;
	/**
	 * gain mutex locker time out. default 5s
	 */
	private  int  timeout = 5;
	/**
	 * 获取锁等待周期时间 单位毫秒
	 */
	private int sleepWaitTime = 20;
	/**
	 * 是否自动释放锁
	 */
	private boolean autoFreeMutex = true;
	/**
	 * 释放锁门限时间 单位秒
	 */
	private int freeThresholdTime = 10;
	/**
	 * 查询加锁sql
	 */
	private String selectLockSql="select is_locked,lock_time,now() from sys_mutex_lock where mutex_id =? for update";
	
	private String updateSql="update sys_mutex_lock set lock_time=now(),is_locked=1 where mutex_id =?";
	
	private String freeMutexSql = "update sys_mutex_lock set is_locked=2 where mutex_id =?";
	
	private String insertSql = "insert into sys_mutex_lock(mutex_id,lock_time,is_locked) values(?,now(),1)";
	/**
	 * 获取互斥锁,如果超时抛出异常
	 * @param lockName 锁名
	 * @throws Exception
	 */
	public void lockMutex(String lockName) throws LockException{
		try {
			if(queryAndLocked(lockName)) return ;
			int total = 0;
			while(total<timeout*1000){
				//等待一个周期
				try {
					Thread.sleep(sleepWaitTime);
					total = total + sleepWaitTime;
				} catch (Exception e) {}
				//再次获取锁
				if(queryAndLocked(lockName)) return;
			}
		} catch (SQLException e) {
			throw new LockException(e.getMessage());
		}
		throw new LockException("have not gain mutex");
	}
	//获得锁返回true or false
	private boolean queryAndLocked(String lockName) throws SQLException{
		boolean result = false;
		Connection con =null;
		PreparedStatement pstmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			//开启事务
			con.setAutoCommit(false);
			pstmt =con.prepareStatement(selectLockSql);
			pstmt.setString(1, lockName);
			rs = pstmt.executeQuery();
			int locked = -1;
			if(rs.next()){
				//1锁定 2空闲
				locked = rs.getInt("is_locked");
				if(locked==1 && autoFreeMutex){
					Timestamp lockTime = rs.getTimestamp("lock_time");
					Timestamp currentTime = rs.getTimestamp(3);
					if((currentTime.getTime()-lockTime.getTime())>freeThresholdTime*1000){
						locked = 2;
					}
				}
			}
			if(locked==-1){//插入锁
				updateStmt =con.prepareStatement(insertSql);
				updateStmt.setString(1, lockName);
				updateStmt.executeUpdate();
				result = true;
			}else if(locked==2){
				updateStmt =con.prepareStatement(updateSql);
				updateStmt.setString(1, lockName);
				updateStmt.executeUpdate();
				result = true;
			}
			//提交
			con.commit();
		} catch (SQLException e) {
			if(con!=null) con.rollback();
			log.error("queryAndLocked",e);
			throw e;
		} catch (Exception e) {
			if(con!=null) con.rollback();
			throw new SQLException(e.getMessage());
		}finally{
			closeStatement(updateStmt);
			closeResultSet(rs);
			closeStatement(pstmt);
			setAutoCommit(con);
			closeConnection(con);
		}
		return result;
	}
	public void freeMutex(String lockName) throws LockException{
		Connection con =null;
		PreparedStatement pstmt = null;
		try {
			con = dataSource.getConnection();
			pstmt =con.prepareStatement(freeMutexSql);
			pstmt.setString(1, lockName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error("freeMutex",e);
			throw new LockException(e.getMessage());
		}finally{
			closeStatement(pstmt);
			closeConnection(con);
		}
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setSelectLockSql(String selectLockSql) {
		this.selectLockSql = selectLockSql;
	}
	
	public void setSleepWaitTime(int sleepWaitTime) {
		this.sleepWaitTime = sleepWaitTime;
	}
	
	public void setAutoFreeMutex(boolean autoFreeMutex) {
		this.autoFreeMutex = autoFreeMutex;
	}
	
	public void setFreeThresholdTime(int freeThresholdTime) {
		this.freeThresholdTime = freeThresholdTime;
	}
	
	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}
	
	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}
	
	private void closeStatement(Statement stmt){
		try {
			if(stmt!=null) stmt.close();
		} catch (SQLException e) {
			log.error("closeStatement",e);
		}
	}
	private void closeResultSet(ResultSet rs){
			try {
				if(rs!=null) rs.close();
			} catch (SQLException e) {
				log.error("closeSResultSet",e);
			}
	}
	private void closeConnection(Connection connection){
		try {
			if(connection !=null) connection.close();
		} catch (Exception e) {
			log.error("closeConnection",e);
		}
	}
	private void setAutoCommit(Connection connection){
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			log.error("autoCommit",e);
		}
	}
}