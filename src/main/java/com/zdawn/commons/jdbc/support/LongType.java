package com.zdawn.commons.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LongType extends AbstractType {

	public Object get(ResultSet rs, String name) throws SQLException {
		Long value = rs.getLong(name);
		if(rs.wasNull()) value = null;
		return value;
	}

	public Object get(ResultSet rs, int index) throws SQLException {
		Long value = rs.getLong(index);
		if(rs.wasNull()) value = null;
		return value;
	}

	public void set(PreparedStatement ps, Object value, int index)
			throws SQLException {
		if(value!=null){
			ps.setLong(index,(Long)value);
		}else{
			ps.setNull(index,Types.BIGINT);
		}
	}

}
