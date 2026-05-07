package com.fons.cloud.db.common;

import java.io.Serializable;

/**
 * @author qiyuan.hong
 * @version 1.0
 * @date 2024/1/31
 */
public class CreateTableSql implements Serializable {

    private String table;
    private String createTable;

    public CreateTableSql() {
    }

    public CreateTableSql(String table, String createTable) {
        this.table = table;
        this.createTable = createTable;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCreateTable() {
        return createTable;
    }

    public void setCreateTable(String createTable) {
        this.createTable = createTable;
    }
}
