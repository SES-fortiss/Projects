package org.fortiss.smg.informationbroker.impl.persistency;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.fortiss.smg.containermanager.api.devices.DeviceContainer;
import org.fortiss.smg.containermanager.api.devices.DeviceId;
import org.fortiss.smg.containermanager.api.devices.SIUnitType;
import org.fortiss.smg.informationbroker.api.DoublePoint;
import org.fortiss.smg.informationbroker.api.InformationBrokerInterface;
import org.fortiss.smg.informationbroker.impl.cache.CacheKey;
import org.fortiss.smg.informationbroker.impl.cache.DoubleCache;
import org.fortiss.smg.informationbroker.impl.cache.LocalCacheManager;
import org.fortiss.smg.sqltools.lib.utils.ResultSetHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistencyQuery implements InformationBrokerInterface {

	// private Logger logger;
	private static final Logger logger = LoggerFactory
			.getLogger(DoubleCache.class);

	private long ContainerCounter = 0;
	private PersistencyDBUtil dbutil;

	public LocalCacheManager localCacheManager;

	/**
	 * 
	 * @param dbUtil
	 * @param localCacheManager
	 * @param logger
	 */
	public PersistencyQuery(PersistencyDBUtil dbUtil,
			LocalCacheManager localCacheManager, Logger logger) {
		this.dbutil = dbUtil;
		this.localCacheManager = localCacheManager;

		init();

	}

	private void init() {

		if (dbutil.checkOrOpenDBConnection()) {
			try {

				PreparedStatement ps = dbutil
						.getCon()
						.prepareStatement(
								"SELECT max(ContainerID) FROM ContainerManager_Containers WHERE 1");
				ResultSet queryResult = ps.executeQuery();
				while (queryResult.next()) {
					if (queryResult != null) {

						ContainerCounter = queryResult.getLong(1);
						
					}
				}
				ps.close();
				dbutil.closeDBConnetion();
			} catch (SQLException e) {
				dbutil.closeDBConnetion();
				logger.warn("PersistencyQuery: SQL Statement error", e);
			}
		} else {
			logger.warn("PersistencyQuery: Could not Initialize - no DB-Connection!");
		}
	}

	public List<DoublePoint> getBoolValue(String deviceId, Long from, Long to) {
		logger.info("getBoolValue using key " + deviceId);
		List<DoublePoint> result = new ArrayList<DoublePoint>();

		if (dbutil.checkOrOpenoldDBConnection()) {
			try {
				PreparedStatement ps;
				if (from.equals(to)) {
					ps = dbutil
							.getOldCon()
							.prepareStatement(
									"SELECT value, timestamp FROM BooleanEvent_Table WHERE origin = ? ORDER BY timestamp DESC LIMIT 0,1");
					ps.setString(1, deviceId);
					logger.debug("PersistencyLog: OLD DB Query "
							+ ps.toString());
				} else {
					ps = dbutil
							.getOldCon()
							.prepareStatement(
									"SELECT value, timestamp FROM BooleanEvent_Table WHERE origin = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC");
					ps.setString(1, deviceId);
					ps.setTimestamp(2, new Timestamp(from));
					ps.setTimestamp(3, new Timestamp(to));
					logger.debug("PersistencyLog: OLD DB Query "
							+ ps.toString());
				}
				ResultSet queryResult = ps.executeQuery();

				while (queryResult.next()) {
					Double value = queryResult.getDouble("value");
					Long timestamp = queryResult.getLong("timestamp");

					DoublePoint bdp = new DoublePoint(value, 0, timestamp);

					result.add(bdp);
				}
				ps.close();
				dbutil.closeoldDBConnetion();
			} catch (SQLException e) {
				dbutil.closeDBConnetion();
				logger.warn("PersistencyQuery: SQL Statement error", e);
			}
		} else {
			logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
		}
		if (result.isEmpty()) {
			logger.warn("PersistencyQuery: Requestet Object not in Database - returning null");
		}
		return result;

	}

	private List<DoublePoint> getDoubleValueOld(String devId, SIUnitType unit,
			long from, long to) {
		logger.trace("PersistencyQuery: requesting from DB " + devId);
		List<DoublePoint> result = new ArrayList<DoublePoint>();
		if (dbutil.checkOrOpenoldDBConnection()) {
			try {
				PreparedStatement ps;
				if (from == to) {
					ps = dbutil
							.getOldCon()
							.prepareStatement(
									"SELECT value, maxAbsError, timestamp FROM DoubleEvent_Table WHERE origin = ? AND unit = ? ORDER BY timestamp DESC LIMIT 0,1");
					ps.setString(1, devId);
					ps.setString(2, unit.toString());
					logger.debug("PersistencyLog: DB Query " + ps.toString());
				} else {

					ps = dbutil
							.getOldCon()
							.prepareStatement(
									"SELECT value, maxAbsError, timestamp FROM DoubleEvent_Table WHERE origin = ? AND unit = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC");
					ps.setString(1, devId);
					ps.setString(2, unit.toString());
					ps.setTimestamp(3, new Timestamp(from));
					ps.setTimestamp(4, new Timestamp(to));

					logger.debug("PersistencyQuery: DB Query: " + ps.toString());

				}

				ResultSet queryResult = ps.executeQuery();

				while (queryResult.next()) {
					double value = queryResult.getDouble("value");

					double maxAbsError = queryResult.getDouble("maxAbsError");
					Timestamp timestamp = queryResult.getTimestamp("timestamp");

					DoublePoint point = new DoublePoint(value, maxAbsError,
							timestamp.getTime());

					result.add(point);

				}
				ps.close();
				dbutil.closeoldDBConnetion();
			} catch (SQLException e) {
				dbutil.closeDBConnetion();
				logger.warn("PersistencyQuery: SQL Statement error", e);
			}
		} else {
			logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
		}

		if (result.isEmpty()) {
			logger.warn("PersistencyQuery: Requested Object (" + devId
					+ ") not in Database - returning empty list");
			// Sebi: dirty fix, but we can't return null because then we get a
			// javax.xml.ws.soap.SOAPFaultException: Error reading
			// XMLStreamReader.at org.apache.cxf.jaxws.JaxWsClientProxy
			/*
			 * ArrayList<DoubleDataPoint> dummy = new
			 * ArrayList<DoubleDataPoint>(); DoubleDataPoint ddp = new
			 * DoubleDataPoint(); ddp.setValue(-1);
			 * ddp.setUnit(SIUnitType.NONE); ddp.setMaxAbsError(-1);
			 * ddp.setTimestamp(new Date()); dummy.add(ddp); return dummy;
			 */
		}
		return result;

	}

	public boolean isSIUnitType(String devid) {
		// TODO Auto-generated method stub
		for (SIUnitType type : SIUnitType.values()) {
			if (type.toString().equals(devid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Synchronized method to close DBConnection of the utility.
	 */
	public synchronized void closeDBConnetion() {
		dbutil.checkOrOpenDBConnection();
	}

	@Override
	public boolean isComponentAlive() {
		return (dbutil.checkOrOpenDBConnection());
	}

	
	@Override
	public List<DoublePoint> getDoubleValue(String containerId, long from, long to) {
		List<DoublePoint> result = new ArrayList<DoublePoint>();
		boolean RequestfromDB = false;

		logger.info("PersistencyQuery: getDoubleValue ");

			if (from == to) {
				logger.trace("PersistencyQuery: getDoubleValue using key "
						+ containerId);
				CacheKey key = new CacheKey(containerId);
				DoublePoint inCache = localCacheManager.doubleCache.load(key);
				if (inCache != null && from == to) {
					if (!(inCache.getTime() < from)) {
						result.add(inCache);
					} else {
						RequestfromDB = true;
					}
				} else {
					RequestfromDB = true;
				}
			} else {
				RequestfromDB = true;
			}
			logger.trace("PersistencyQuery: requesting from DB: "
					+ RequestfromDB);

			if (RequestfromDB) {
				logger.trace("PersistencyQuery: requesting from DB " + containerId);
				if (dbutil.checkOrOpenDBConnection()) {
					try {
						PreparedStatement ps;
						if (from == to) {
							ps = dbutil
									.getCon()
									.prepareStatement(
											"SELECT value, maxAbsError, timestamp FROM DoubleEvents WHERE containerid = ? ORDER BY timestamp DESC LIMIT 0,1");
							ps.setString(1, containerId);
							logger.debug("PersistencyLog: DB Query "
									+ ps.toString());
						} else {

							ps = dbutil
									.getCon()
									.prepareStatement(
											"SELECT value, maxAbsError, timestamp FROM DoubleEvents WHERE containerid = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC");
							ps.setString(1, containerId);
							ps.setLong(2, new Timestamp(from).getTime());
							ps.setLong(3, new Timestamp(to).getTime());

							/*
							 * USE INDEX (timestamp)
							 * "SELECT value, unit, maxAbsError, timestamp FROM DoubleEvent_Table, (SELECT unit AS u, MAX(timestamp) AS ts FROM DoubleEvent_Table WHERE origin = ? GROUP BY unit) AS foo WHERE timestamp = foo.ts AND unit = foo.u AND origin = ?"
							 * ); //deprecated with trigger and cached
							 * maxtimestamp! //
							 * "SELECT value, unit, maxAbsError, timestamp FROM DoubleEvent_Table, ( SELECT unit AS u, max_timestamp AS ts FROM max_timestamps_double WHERE origin = ?) AS foo WHERE timestamp = foo.ts AND unit = foo.u AND origin = ?"
							 * ); ps.setString(1, deviceId); ps.setString(2,
							 * deviceId); // TASK: this duplicate is not so //
							 * pretty..
							 */
							logger.debug("PersistencyQuery: DB Query: "
									+ ps.toString());
							// System.out.println("PersistencyQuery: DB Query: "
							// +
							// ps.toString());
						}

						ResultSet queryResult = ps.executeQuery();

						while (queryResult.next()) {
							double value = queryResult.getDouble("value");

							double maxAbsError = queryResult
									.getDouble("maxAbsError");
							Long timestamp = queryResult.getLong("timestamp");

							DoublePoint point = new DoublePoint(value,
									maxAbsError, timestamp);

							result.add(point);
							/*
							 * as at least one result was not in cache, store
							 * them there (different key - for each unit)
							 */
							if (from == to) {
								localCacheManager.doubleCache.store(
										new CacheKey(containerId), point);
							}

						}
						ps.close();
					} catch (SQLException e) {
						dbutil.closeDBConnetion();
						logger.warn("PersistencyQuery: SQL Statement error", e);
					}
				} else {
					logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
				}
			}
		
		if (result.isEmpty()) {
			logger.warn("PersistencyQuery: Requested Object (" + containerId
					+ ") not in Database - returning null");
			// Sebi: dirty fix, but we can't return null because then we get a
			// javax.xml.ws.soap.SOAPFaultException: Error reading
			// XMLStreamReader.at org.apache.cxf.jaxws.JaxWsClientProxy
			/*
			 * ArrayList<DoubleDataPoint> dummy = new
			 * ArrayList<DoubleDataPoint>(); DoubleDataPoint ddp = new
			 * DoubleDataPoint(); ddp.setValue(-1);
			 * ddp.setUnit(SIUnitType.NONE); ddp.setMaxAbsError(-1);
			 * ddp.setTimestamp(new Date()); dummy.add(ddp); return dummy;
			 */
			return null;
		} else {
			return result;
		}

	}

	public List<DoublePoint> getDoubleValueWithLong(String containerId, long from,
			long to) {
		List<DoublePoint> result = new ArrayList<DoublePoint>();
		boolean RequestfromDB = false;

		logger.trace("PersistencyQuery: getDoubleValue ");
		if (from == to) {
			logger.trace("PersistencyQuery: getDoubleValue using key " + containerId);
			CacheKey key = new CacheKey(containerId);
			DoublePoint inCache = localCacheManager.doubleCache.load(key);
			if (inCache != null && from == to) {
				if (!(inCache.getTime() < from)) {
					result.add(inCache);
				} else {
					RequestfromDB = true;
				}
			} else {
				RequestfromDB = true;
			}
			
		} else {
			RequestfromDB = true;
		}
		logger.trace("PersistencyQuery: requesting from DB: " + RequestfromDB);

		if (RequestfromDB) {
			logger.trace("PersistencyQuery: requesting from DB " + containerId);
			if (dbutil.checkOrOpenDBConnection()) {
				try {
					PreparedStatement ps;
					if (from == to) {
						ps = dbutil
								.getCon()
								.prepareStatement(
										"SELECT value, unit, maxAbsError, timestamp FROM DoubleEvents WHERE containerid = ? ORDER BY timestamp DESC LIMIT 0,1");
						ps.setString(1, containerId);
						logger.debug("PersistencyLog: DB Query "
								+ ps.toString());
					} else {

						ps = dbutil
								.getCon()
								.prepareStatement(
										"SELECT value, unit, maxAbsError, timestamp FROM DoubleEvents WHERE containerid = ? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC");
						ps.setString(1, containerId);
						ps.setLong(2, from);
						ps.setLong(3, to);

						/*
						 * USE INDEX (timestamp)
						 * "SELECT value, unit, maxAbsError, timestamp FROM DoubleEvent_Table, (SELECT unit AS u, MAX(timestamp) AS ts FROM DoubleEvent_Table WHERE origin = ? GROUP BY unit) AS foo WHERE timestamp = foo.ts AND unit = foo.u AND origin = ?"
						 * ); //deprecated with trigger and cached maxtimestamp!
						 * //
						 * "SELECT value, unit, maxAbsError, timestamp FROM DoubleEvent_Table, ( SELECT unit AS u, max_timestamp AS ts FROM max_timestamps_double WHERE origin = ?) AS foo WHERE timestamp = foo.ts AND unit = foo.u AND origin = ?"
						 * ); ps.setString(1, deviceId); ps.setString(2,
						 * deviceId); // TASK: this duplicate is not so //
						 * pretty..
						 */
						logger.info("PersistencyQuery: DB Query: "
								+ ps.toString());
						// System.out.println("PersistencyQuery: DB Query: " +
						// ps.toString());
					}

					ResultSet queryResult = ps.executeQuery();

					while (queryResult.next()) {
						double value = queryResult.getDouble("value");
						double maxAbsError = queryResult
								.getDouble("maxAbsError");
						Long timestamp = queryResult.getLong("timestamp");

						DoublePoint point = new DoublePoint(value, maxAbsError,
								timestamp);
						result.add(point);
						/*
						 * as at least one result was not in cache, store them
						 * there (different key - for each unit)
						 */
						if (from == to) {
							localCacheManager.doubleCache.store(new CacheKey(containerId), point);
						}

					}
					ps.close();
				} catch (SQLException e) {
					dbutil.closeDBConnetion();
					logger.warn("PersistencyQuery: SQL Statement error", e);
				}
			} else {
				logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
			}
		}
		if (result.isEmpty()) {
			logger.warn("PersistencyQuery: Requested Object (" + containerId
					+ ") not in Database - returning null");
			// Sebi: dirty fix, but we can't return null because then we get a
			// javax.xml.ws.soap.SOAPFaultException: Error reading
			// XMLStreamReader.at org.apache.cxf.jaxws.JaxWsClientProxy
			/*
			 * ArrayList<DoubleDataPoint> dummy = new
			 * ArrayList<DoubleDataPoint>(); DoubleDataPoint ddp = new
			 * DoubleDataPoint(); ddp.setValue(-1);
			 * ddp.setUnit(SIUnitType.NONE); ddp.setMaxAbsError(-1);
			 * ddp.setTimestamp(new Date()); dummy.add(ddp); return dummy;
			 */
			return null;
		} else {
			return result;
		}
	}

	
	
	
	@Override
	public List<DoublePoint> getDoubleValueBefore(String containerId, long date) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see http
	 *      ://stackoverflow.com/questions/6514876/most-effecient-conversion-
	 *      of-resultset-to-json
	 */
	@Override
	public List<Map<String, Object>> getSQLResults(String sql) {

		if (dbutil.checkOrOpenDBConnection()) {
			try {

				ResultSet rs = dbutil.getCon().createStatement()
						.executeQuery(sql);
				if (rs == null) {
					logger.info("SQL error for: {}", sql);
				} else {
					return ResultSetHelper.convert(rs);
				}

			} catch (SQLException e) {
				logger.info("SQL error for: " + sql, e.fillInStackTrace());
				return null;
			}
		} else {
			logger.info("DB Error");
			return null;
		}
		return null;
	}

	public ResultSet getSQLResultSet(String sql) {

		if (dbutil.checkOrOpenDBConnection()) {
			try {

				ResultSet rs = dbutil.getCon().createStatement()
						.executeQuery(sql);
				if (rs == null) {
					logger.info("SQL error for: {}", sql);
				} else {
					return rs;
				}

			} catch (SQLException e) {
				logger.info("SQL error for: " + sql, e.fillInStackTrace());
				return null;
			}
		} else {
			logger.info("DB Error");
			return null;
		}
		return null;
	}

	@Override
	public boolean executeQuery(String sql) {
		if (dbutil.checkOrOpenDBConnection()) {
			try {
				logger.debug("Executing:" + sql);
				dbutil.getCon().createStatement().execute(sql);
				/*
				 * whenever the execution took place with no exceptions return
				 * true dbutil.getCon().createStatement().execute(sql);
				 */
				return true;
			} catch (SQLException e) {
				logger.warn("SQL error for: " + sql + "\n \n " + e + "\n"
						+ e.fillInStackTrace());
				return false;
			}
		} else {
			logger.info("DB Error");
			return false;
		}
	}

	@Override
	public List<Map<String, Object>> getSQLResultsoldDB(String sql) {

		if (dbutil.checkOrOpenoldDBConnection()) {
			try {

				ResultSet rs = dbutil.getOldCon().createStatement()
						.executeQuery(sql);
				if (rs == null) {
					logger.info("SQL error for: {}", sql);
				} else {
					return ResultSetHelper.convert(rs);
				}

			} catch (SQLException e) {
				logger.info("SQL error for: " + sql, e.fillInStackTrace());
				return null;
			}
		} else {
			logger.info("DB Error");
			return null;
		}
		return null;
	}

	@Override
	public boolean executeQueryoldDB(String sql) {
		if (dbutil.checkOrOpenoldDBConnection()) {
			try {
				dbutil.getOldCon().createStatement().execute(sql);
				/*
				 * whenever the execution took place with no exceptions return
				 * truedbutil.getCon().createStatement().execute(sql);
				 */
				return true;
			} catch (SQLException e) {
				logger.info("SQL error for: " + sql, e.fillInStackTrace());
				return false;
			}
		} else {
			logger.info("DB Error");
			return false;
		}
	}

	@Override
	public Map<Long, Double> getLastseen(String containerId) {
		Map<Long, Double> result = new HashMap<Long, Double>();
		if (dbutil.checkOrOpenDBConnection()) {
			try {
				PreparedStatement ps;

				ps = dbutil
						.getCon()
						.prepareStatement(
								"SELECT timestamp , value  FROM DoubleEvents WHERE containerid = ? AND ORDER BY timestamp DESC LIMIT 0,1");
				ps.setString(1, containerId);
				logger.debug("PersistencyLog: DB Query " + ps.toString());
				ResultSet queryResult = ps.executeQuery();

				while (queryResult.next()) {
					result.put(queryResult.getLong("timestamp"),
							queryResult.getDouble("value"));
				}
				ps.close();
				dbutil.closeDBConnetion();
			} catch (SQLException e) {
				dbutil.closeDBConnetion();
				e.printStackTrace();
				logger.warn("PersistencyQuery: SQL Statement error", e);
			}
		} else {
			logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
		}
		if (result.size() == 0) {
			logger.warn("PersistencyQuery: Requestet Object not in Database - returning null");
		}
		return result;

	}

	/*
	 * This method will autoincrement den ContainerID (this will not be done in
	 * DB!) because the object is currently not known and will be stored later
	 * Hence also a temp. value is maintained and compared - e.g. race condition
	 * the maintained value (ContainerCounter) will be used
	 * returns the next available ConainterID
	 */
	@Override
	public long getContainerID(String devID, String wrapperID) throws TimeoutException {
		// TODO Auto-generated method stub
		Long result = -1l;
		if (dbutil.checkOrOpenDBConnection()) {
			try {
				PreparedStatement ps;
				ps = dbutil
						.getCon()
						.prepareStatement(
								"SELECT ContainerID FROM ContainerManager_Containers WHERE DevID = ? AND WrapperID = ? LIMIT 0,1");
				ps.setString(1, devID);
				ps.setString(2, wrapperID);
				ResultSet queryResult = ps.executeQuery();
				while (queryResult.next()) {
					result = queryResult.getLong("ContainerID");
				}
				ps.close();

				if (result <= 0) {
					ps = dbutil
							.getCon()
							.prepareStatement(
									"SELECT max(ContainerID) FROM ContainerManager_Containers WHERE 1");
					queryResult = ps.executeQuery();
					while (queryResult.next()) {
						if (queryResult != null) {
							result = queryResult.getLong(1);
						}
					}
					ps.close();
					dbutil.closeDBConnetion();

				}
				else {
					return result;
				}
				


			} catch (SQLException e) {
				dbutil.closeDBConnetion();
				logger.warn("PersistencyQuery: SQL Statement error", e);
			}
		} else {
			logger.warn("PersistencyQuery: Could not execute Query - no DB-Connection!");
		}
		logger.info("Result from ContainerID: " + result + " counter currently: " + ContainerCounter);
		//return the next available ContainerID
//		if (result > 0 && result <= ContainerCounter) {
//			return result;
//		}
				
		ContainerCounter++;
		return ContainerCounter;
		
	}

}
