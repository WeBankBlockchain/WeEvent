package com.webank.weevent.core.fisco.web3sdk.v2;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.precompiled.crud.TableCRUDService;
import org.fisco.bcos.sdk.model.PrecompiledConstant;
import org.fisco.bcos.sdk.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;


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

    protected Client client;
    protected TableCRUDService crud;
    //    protected List<Map<String, String>> table = new ArrayList<>();
    protected String tableName;


    public CRUDTable(Client client, String tableName) throws BrokerException {
        this.client = client;
        this.crud = new TableCRUDService(client, client.getCryptoSuite().createKeyPair());
        this.tableName = tableName;

        log.info("table's groupId: {}", client.getGroupId());

        ensureTable();
    }

    /*
     * Table in CRUD, it's a key-value store.
     * Table -> key, value
     * https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-2.0/docs/manual/console.html#desc
     */
    protected void ensureTable() throws BrokerException {
        try {
            List<Map<String, String>> tableDesc = this.crud.desc(this.tableName);
            if (tableDesc.size() == 0
                    || StringUtils.isBlank(tableDesc.get(0).get(PrecompiledConstant.KEY_FIELD_NAME))) {
                throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
            }
            if (tableDesc.get(0).get(PrecompiledConstant.KEY_FIELD_NAME).equals(TableKey)) {
                // get field
                List<String> fields = Arrays.asList(tableDesc.get(0).get(PrecompiledConstant.VALUE_FIELD_NAME).split(","));
                if (fields.size() == 2 && fields.contains(TableValue) && fields.contains(TableVersion)) {
//                    this.table = tableDesc;
                    return;
                }
            }
            log.error("miss fields in CRUD table, {}/{}/{}", TableKey, TableValue, TableVersion);
            throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        } catch (ContractException e) {
            log.error("detect ContractException in web3sdk", e);
            log.info("not exist table in CRUD, create it: {}", this.tableName);

            createTable();
        }
    }

    protected void createTable() throws BrokerException {
        List<String> keyFiledName = Arrays.asList(TableValue, TableVersion);

        try {
            RetCode retCode = this.crud.createTable(this.tableName, TableKey, keyFiledName);
            if (retCode.getCode() == PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                log.info("create table in CRUD success, {}", this.tableName);
//                Map<String, String> tableDesc = new HashMap<>();
//                tableDesc.put(PrecompiledConstant.KEY_FIELD_NAME, TableKey);
//                tableDesc.put(PrecompiledConstant.VALUE_FIELD_NAME, TableValue + ", " + TableVersion);
//                this.table.add(tableDesc);
                return;
            }
            log.error("create table in CRUD failed, " + this.tableName);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        } catch (ContractException e) {
            log.error("create table in CRUD failed, " + this.tableName, e);
            throw new BrokerException(ErrorCode.TRANSACTION_EXECUTE_ERROR);
        }
    }
}
