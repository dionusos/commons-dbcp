/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.dbcp2;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * A {@link DelegatingConnection} that pools {@link PreparedStatement}s.
 * <p>
 * The {@link #prepareStatement} and {@link #prepareCall} methods, rather than
 * creating a new PreparedStatement each time, may actually pull the statement
 * from a pool of unused statements.
 * The {@link PreparedStatement#close} method of the returned statement doesn't
 * actually close the statement, but rather returns it to the pool.
 * (See {@link PoolablePreparedStatement}, {@link PoolableCallableStatement}.)
 *
 * @see PoolablePreparedStatement
 * @author Rodney Waldhoff
 * @author Dirk Verbeeck
 * @since 2.0
 */
public class PoolingConnection extends DelegatingConnection<Connection>
        implements KeyedPooledObjectFactory<PStmtKey, DelegatingPreparedStatement> {

    /**
     * Statement types.
     *
     * @since 2.0 protected enum.
     * @since 2.4.0  public enum.
     */
    public enum StatementType {

        /**
         * Callable statement.
         */
        CALLABLE_STATEMENT,

        /**
         * Prepared statement.
         */
        PREPARED_STATEMENT
    }

    /** Pool of {@link PreparedStatement}s. and {@link CallableStatement}s */
    private KeyedObjectPool<PStmtKey, DelegatingPreparedStatement> pstmtPool = null;

    /**
     * Constructor.
     * @param conn the underlying {@link Connection}.
     */
    public PoolingConnection(final Connection conn) {
        super(conn);
    }


    /**
     * {@link KeyedPooledObjectFactory} method for activating
     * pooled statements.
     *
     * @param key ignored
     * @param p wrapped pooled statement to be activated
     */
    @Override
    public void activateObject(final PStmtKey key,
            final PooledObject<DelegatingPreparedStatement> p) throws Exception {
        p.getObject().activate();
    }

    /**
     * Closes and frees all {@link PreparedStatement}s or
     * {@link CallableStatement}s from the pool, and close the underlying
     * connection.
     */
    @Override
    public synchronized void close() throws SQLException {
        try {
            if (null != pstmtPool) {
                final KeyedObjectPool<PStmtKey,DelegatingPreparedStatement> oldpool = pstmtPool;
                pstmtPool = null;
                try {
                    oldpool.close();
                } catch(final RuntimeException e) {
                    throw e;
                } catch(final Exception e) {
                    throw new SQLException("Cannot close connection", e);
                }
            }
        } finally {
            try {
                getDelegateInternal().close();
            } finally {
                setClosedInternal(true);
            }
        }
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     */
    protected PStmtKey createKey(final String sql) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog);
    }

    protected PStmtKey createKey(final String sql, final int autoGeneratedKeys) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, autoGeneratedKeys);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param columnIndexes column indexes
     */
    protected PStmtKey createKey(final String sql, final int columnIndexes[]) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, columnIndexes);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     */
    protected PStmtKey createKey(final String sql, final int resultSetType, final int resultSetConcurrency) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param resultSetHoldability result set holdability
     */
    protected PStmtKey createKey(final String sql, final int resultSetType, final int resultSetConcurrency,
            final int resultSetHoldability) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param resultSetHoldability result set holdability
     * @param stmtType statement type
     */
    protected PStmtKey createKey(final String sql, final int resultSetType, final int resultSetConcurrency,
            final int resultSetHoldability, final StatementType stmtType) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency, resultSetHoldability,  stmtType);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param stmtType statement type
     */
    protected PStmtKey createKey(final String sql, final int resultSetType, final int resultSetConcurrency, final StatementType stmtType) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, resultSetType, resultSetConcurrency, stmtType);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the SQL string used to define the statement
     * @param stmtType statement type
     */
    protected PStmtKey createKey(final String sql, final StatementType stmtType) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, stmtType, null);
    }

    /**
     * Creates a PStmtKey for the given arguments.
     * @param sql the sql string used to define the statement
     * @param columnNames column names
     */
    protected PStmtKey createKey(final String sql, final String columnNames[]) {
        String catalog = null;
        try {
            catalog = getCatalog();
        } catch (final SQLException e) {
            // Ignored
        }
        return new PStmtKey(normalizeSQL(sql), catalog, columnNames);
    }

    /**
     * {@link KeyedPooledObjectFactory} method for destroying
     * PoolablePreparedStatements and PoolableCallableStatements.
     * Closes the underlying statement.
     *
     * @param key ignored
     * @param p the wrapped pooled statement to be destroyed.
     */
    @Override
    public void destroyObject(final PStmtKey key,
            final PooledObject<DelegatingPreparedStatement> p)
            throws Exception {
        p.getObject().getInnermostDelegate().close();
    }

    /**
     * {@link KeyedPooledObjectFactory} method for creating
     * {@link PoolablePreparedStatement}s or {@link PoolableCallableStatement}s.
     * The <code>stmtType</code> field in the key determines whether
     * a PoolablePreparedStatement or PoolableCallableStatement is created.
     *
     * @param key the key for the {@link PreparedStatement} to be created
     * @see #createKey(String, int, int, StatementType)
     */
    @SuppressWarnings("resource")
    @Override
    public PooledObject<DelegatingPreparedStatement> makeObject(final PStmtKey key)
            throws Exception {
        if (null == key) {
            throw new IllegalArgumentException("Prepared statement key is null or invalid.");
        }
        if (key.getStmtType() == StatementType.PREPARED_STATEMENT ) {
            final PreparedStatement statement = (PreparedStatement) key.createStatement(getDelegate());
            @SuppressWarnings({"rawtypes", "unchecked"}) // Unable to find way to avoid this
            final PoolablePreparedStatement pps = new PoolablePreparedStatement(statement, key, pstmtPool, this);
            return new DefaultPooledObject<DelegatingPreparedStatement>(pps);
        }
        final CallableStatement statement = (CallableStatement) key.createStatement(getDelegate());
        final PoolableCallableStatement pcs = new PoolableCallableStatement(statement, key, pstmtPool, this);
        return new DefaultPooledObject<DelegatingPreparedStatement>(pcs);
    }

    /**
     * Normalizes the given SQL statement, producing a
     * canonical form that is semantically equivalent to the original.
     */
    protected String normalizeSQL(final String sql) {
        return sql.trim();
    }

    /**
     * {@link KeyedPooledObjectFactory} method for passivating
     * {@link PreparedStatement}s or {@link CallableStatement}s.
     * Invokes {@link PreparedStatement#clearParameters}.
     *
     * @param key ignored
     * @param p a wrapped {@link PreparedStatement}
     */
    @Override
    public void passivateObject(final PStmtKey key,
            final PooledObject<DelegatingPreparedStatement> p) throws Exception {
        final DelegatingPreparedStatement dps = p.getObject();
        dps.clearParameters();
        dps.passivate();
    }

    /**
     * Creates or obtains a {@link CallableStatement} from the pool.
     * @param sql the sql string used to define the CallableStatement
     * @return a {@link PoolableCallableStatement}
     * @throws SQLException
     */
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        try {
            return (CallableStatement) pstmtPool.borrowObject(createKey(sql, StatementType.CALLABLE_STATEMENT));
        } catch (final NoSuchElementException e) {
            throw new SQLException("MaxOpenCallableStatements limit reached", e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new SQLException("Borrow callableStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link CallableStatement} from the pool.
     * @param sql the sql string used to define the CallableStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @return a {@link PoolableCallableStatement}
     * @throws SQLException
     */
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return (CallableStatement) pstmtPool.borrowObject(createKey(sql, resultSetType,
                            resultSetConcurrency, StatementType.CALLABLE_STATEMENT));
        } catch (final NoSuchElementException e) {
            throw new SQLException("MaxOpenCallableStatements limit reached", e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new SQLException("Borrow callableStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link CallableStatement} from the pool.
     * @param sql the sql string used to define the CallableStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param resultSetHoldability result set holdability
     * @return a {@link PoolableCallableStatement}
     * @throws SQLException
     */
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType,
            final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return (CallableStatement) pstmtPool.borrowObject(createKey(sql, resultSetType,
                            resultSetConcurrency, resultSetHoldability, StatementType.CALLABLE_STATEMENT));
        } catch (final NoSuchElementException e) {
            throw new SQLException("MaxOpenCallableStatements limit reached", e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new SQLException("Borrow callableStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql));
        } catch(final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(final RuntimeException e) {
            throw e;
        } catch(final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql, autoGeneratedKeys));
        }
        catch (final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        }
        catch (final RuntimeException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @param columnIndexes column indexes
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(final String sql, final int columnIndexes[])
            throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql, columnIndexes));
        } catch(final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(final RuntimeException e) {
            throw e;
        } catch(final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql,resultSetType,resultSetConcurrency));
        } catch(final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(final RuntimeException e) {
            throw e;
        } catch(final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @param resultSetType result set type
     * @param resultSetConcurrency result set concurrency
     * @param resultSetHoldability result set holdability
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType,
            final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
        } catch(final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(final RuntimeException e) {
            throw e;
        } catch(final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    /**
     * Creates or obtains a {@link PreparedStatement} from the pool.
     * @param sql the sql string used to define the PreparedStatement
     * @param columnNames column names
     * @return a {@link PoolablePreparedStatement}
     */
    @Override
    public PreparedStatement prepareStatement(final String sql, final String columnNames[])
            throws SQLException {
        if (null == pstmtPool) {
            throw new SQLException(
                    "Statement pool is null - closed or invalid PoolingConnection.");
        }
        try {
            return pstmtPool.borrowObject(createKey(sql, columnNames));
        } catch(final NoSuchElementException e) {
            throw new SQLException("MaxOpenPreparedStatements limit reached", e);
        } catch(final RuntimeException e) {
            throw e;
        } catch(final Exception e) {
            throw new SQLException("Borrow prepareStatement from pool failed", e);
        }
    }

    public void setStatementPool(
            final KeyedObjectPool<PStmtKey,DelegatingPreparedStatement> pool) {
        pstmtPool = pool;
    }

    @Override
    public String toString() {
        if (pstmtPool != null ) {
            return "PoolingConnection: " + pstmtPool.toString();
        }
        return "PoolingConnection: null";
    }

    /**
     * {@link KeyedPooledObjectFactory} method for validating
     * pooled statements. Currently always returns true.
     *
     * @param key ignored
     * @param p ignored
     * @return {@code true}
     */
    @Override
    public boolean validateObject(final PStmtKey key,
            final PooledObject<DelegatingPreparedStatement> p) {
        return true;
    }
}
