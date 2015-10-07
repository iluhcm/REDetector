/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.parsers.referenceparsers;

import com.xl.database.DatabaseManager;
import com.xl.exception.DataLoadException;
import com.xl.interfaces.ProgressListener;
import com.xl.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by Administrator on 2015/10/5.
 */
public abstract class AbstractParser {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractParser.class);
    protected final String dataPath;
    protected final String tableName;
    protected final DatabaseManager databaseManager = DatabaseManager.getInstance();

    public AbstractParser(String dataPath) {
        this(dataPath, "");
    }

    public AbstractParser(String dataPath, String tableName) {
        this.dataPath = dataPath;
        this.tableName = tableName;
    }

    public void loadDataFromLocal(ProgressListener listener) throws DataLoadException {
        if (dataPath == null || dataPath.length() == 0) {
            throw new DataLoadException("Error load file.");
        }
        logger.info("Start loading " + tableName + "... at {}", Timer.getCurrentTime());
        if (listener != null) {
            listener.progressUpdated("Start loading data from " + dataPath + " to " + tableName + " table", 0, 0);
        }
        createTable();
        loadData(listener);
        if (listener != null) {
            listener.progressComplete(tableName + "_loaded", null);
        }
        int counts = databaseManager.calRowCount(tableName);
        // INSERT table (auto_id, auto_name) values (1, yourname') ON DUPLICATE KEY UPDATE auto_name='yourname'
        try {
            databaseManager
                .insertClause("insert into " + DatabaseManager.INFORMATION_TABLE_NAME + "(tableName,counts) values("
                    + tableName + ",'" + counts + "') ON DUPLICATE KEY UPDATE counts='" + counts + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("End loading " + tableName + "... at {}", Timer.getCurrentTime());
    }

    protected abstract void createTable();

    protected abstract void loadData(ProgressListener listener);

}
