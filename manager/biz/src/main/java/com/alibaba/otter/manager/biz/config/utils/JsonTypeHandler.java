package com.alibaba.otter.manager.biz.config.utils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/** 数据库 JSON 字段与配置对象之间的转换 */
public abstract class JsonTypeHandler extends BaseTypeHandler<Object> {

    protected abstract String serialize(Object value);

    protected abstract Object deserialize(String value);

    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, Object value, JdbcType jdbcType)
                                                                                                          throws SQLException {
        statement.setString(index, serialize(value));
    }

    @Override
    public Object getNullableResult(ResultSet result, String column) throws SQLException {
        return read(result.getString(column));
    }

    @Override
    public Object getNullableResult(ResultSet result, int column) throws SQLException {
        return read(result.getString(column));
    }

    @Override
    public Object getNullableResult(CallableStatement statement, int column) throws SQLException {
        return read(statement.getString(column));
    }

    private Object read(String value) {
        return value == null ? null : deserialize(value);
    }
}
