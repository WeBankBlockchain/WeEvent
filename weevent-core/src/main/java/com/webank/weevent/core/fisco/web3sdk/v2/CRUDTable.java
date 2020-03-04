package com.webank.weevent.core.fisco.web3sdk.v2;


import java.util.Arrays;
import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.crud.CRUDService;
import org.fisco.bcos.web3j.precompile.crud.Table;
import org.fisco.bcos.web3j.precompile.exception.PrecompileMessageException;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.JsonRpc2_0Web3j;

/**
 * CRUD table in FISCO-BCOS.
 * version supported key-value store.
 * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/en/docs/sdk/sdk.html?highlight=CRUDService#web3sdk-api
 *
 * @author matthewliu
 * @since 2019/12/26
 */
@Slf4j
public class CRUDTable {
    public final static String TableKey = "key";
    public final static String TableValue = "value";
    public final static String TableVersion = "version";

    protected String groupId;
    protected CRUDService crud;
    protected String tableName;
    protected Table table;

    public CRUDTable(Web3j web3j, Credentials credentials, String tableName) throws BrokerException {
        this.groupId = String.valueOf(((JsonRpc2_0Web3j) web3j).getGroupId());
        this.crud = new CRUDService(web3j, credentials);
        this.tableName = tableName;

        log.info("table's groupId: {}", this.groupId);

        ensureTable();
    }

    /*
     * Table in CRUD, it's a key-value store.
     * Table -> key, value
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/docs/manual/console.html#desc
     */
    protected void ensureTable() throws BrokerException {
        try {
            Table table = this.crud.desc(this.tableName);
            if (TableKey.equals(table.getKey())) {
                List<String> fields = Arrays.asList(table.getValueFields().split(","));
                if (fields.size() == 2 && fields.contains(TableValue) && fields.contains(TableVersion)) {
                    this.table = table;
                    return;
                }
            }

            log.error("miss fields in CRUD table, {}/{}/{}", TableKey, TableValue, TableVersion);
            throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        } catch (PrecompileMessageException e) {
            log.error("detect PrecompileMessageException in web3sdk", e);
            log.info("not exist table in CRUD, create it: {}", this.tableName);

            createTable();
        } catch (BrokerException e) {
            throw e;
        } catch (Exception e) {
            log.error("ensure table in CRUD failed, " + this.tableName, e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }

    protected void createTable() throws BrokerException {
        Table table = new Table(this.tableName, TableKey, TableValue + "," + TableVersion);
        try {
            int result = this.crud.createTable(table);
            if (result == 0) {
                log.info("create table in CRUD success, {}", this.tableName);
                this.table = table;
                return;
            }

            log.error("create table in CRUD failed, " + this.tableName);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (Exception e) {
            log.error("create table in CRUD failed, " + this.tableName, e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }
}
