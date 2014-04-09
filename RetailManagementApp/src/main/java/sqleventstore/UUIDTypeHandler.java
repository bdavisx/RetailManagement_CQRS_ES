package sqleventstore;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDTypeHandler implements TypeHandler<UUID> {
  @Override
  public void setParameter( final PreparedStatement ps, final int index, final UUID parameter, final JdbcType jdbcType )
    throws SQLException {
    ps.setObject( index, parameter );
  }

  @Override
  public UUID getResult( final ResultSet rs, final String columnName ) throws SQLException {
    return (UUID) rs.getObject( columnName );
  }

  @Override
  public UUID getResult( final ResultSet rs, final int columnIndex ) throws SQLException {
    return (UUID) rs.getObject( columnIndex );
  }

  @Override
  public UUID getResult( final CallableStatement cs, final int columnIndex ) throws SQLException {
    return (UUID) cs.getObject( columnIndex );
  }
}
