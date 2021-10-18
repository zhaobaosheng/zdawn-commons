package com.zdawn.commons.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class FloatType extends AbstractType {

	public Object get(ResultSet rs, String name) throws SQLException {
		Float value = rs.getFloat(name);
		if(rs.wasNull()) value = null;
		return value;
	}

	public Object get(ResultSet rs, int index) throws SQLException {
		Float value = rs.getFloat(index);
		if(rs.wasNull()) value = null;
		return value;
	}

	public void set(PreparedStatement ps, Object value, int index)
			throws SQLException {
		if(value!=null){
			ps.setFloat(index,(Float)value);
		}else{
			ps.setNull(index,Types.FLOAT);
		}
	}

}
