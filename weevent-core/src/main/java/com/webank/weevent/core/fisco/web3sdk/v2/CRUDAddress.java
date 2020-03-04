package com.webank.weevent.core.fisco.web3sdk.v2;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.precompile.crud.Condition;
import org.fisco.bcos.web3j.precompile.crud.Entry;
import org.fisco.bcos.web3j.protocol.Web3j;

/**
 * Contract address in CRUD table.
 *
 * @author matthewliu
 * @since 2020/01/03
 */
@Slf4j
public class CRUDAddress extends CRUDTable {
    // contract address in CRUD
    public final static String TableName = "WeEvent";
    // only one record in table
    public final static String ContractAddress = "topic_control_address";

    public CRUDAddress(Web3j web3j, Credentials credentials) throws BrokerException {
        super(web3j, credentials, TableName);
    }

    /*
     * list all address from CRUD table
     *
     * @return address list
     */
    public Map<Long, String> listAddress() throws BrokerException {
        log.info("associated groupId: {}", this.groupId);

        List<Map<String, String>> records;
        try {
            this.table.setKey(ContractAddress);
            Condition condition = this.table.getCondition();
            records = this.crud.select(this.table, condition);
            log.info("records in CRUD, {}", records);
        } catch (Exception e) {
            log.error("select from CRUD table failed", e);
            throw new BrokerException(ErrorCode.UNKNOWN_SOLIDITY_VERSION);
        }

        Map<Long, String> addresses = new HashMap<>();
        for (Map<String, String> record : records) {
            addresses.put(Long.valueOf(record.get(TableVersion)), record.get(TableValue));
        }
        return addresses;
    }

    public boolean addAddress(Long version, String address) throws BrokerException {
        log.info("associated groupId: {}", this.groupId);

        // check exist manually to avoid duplicate record
        Map<Long, String> topicControlAddresses = listAddress();
        if (topicControlAddresses.containsKey(version)) {
            log.info("already exist in CRUD, {} {}", version, address);
            return false;
        }

        try {
            this.table.setKey(ContractAddress);
            Entry record = this.table.getEntry();
            record.put(TableValue, address);
            record.put(TableVersion, String.valueOf(version));
            // notice: record's key can be duplicate in CRUD
            int result = this.crud.insert(this.table, record);
            if (result == 1) {
                log.info("add contract address into CRUD success");
                return true;
            }

            log.error("add contract address into CRUD failed, {}", result);
            return false;
        } catch (Exception e) {
            log.error("add contract address into CRUD failed", e);
            return false;
        }
    }
}
