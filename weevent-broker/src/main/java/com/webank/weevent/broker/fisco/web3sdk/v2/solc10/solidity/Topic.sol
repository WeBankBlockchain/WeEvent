// FISCO2.0.0 support highest 0.4.25
pragma solidity ^0.4.25;
// support string[]
pragma experimental ABIEncoderV2;

contract Topic {
    uint constant private SUCCESS = 0;
    uint constant private PUBLISH_NO_PERMISSION = 0;
    uint constant private OPERATOR_NO_PERMISSION = 101016;
    uint constant private OPERATOR_ALREADY_EXIST = 101017;
    uint constant private OPERATOR_NOT_EXIST = 101018;

    mapping (string => Snapshot) private topicSnapshot;
    mapping (string => ACL) private ACLMap;

    struct Snapshot {
        uint sequence;
        uint block;
        uint timestamp;
        address sender;
    }

    struct ACL {
        address owner;
        address[] operators;
    }

    function addTopicACL(string topicName, address ownerAddress) public returns (bool) {
        address[] operators;
        operators.push(ownerAddress);
        ACLMap[topicName] = ACL(ownerAddress, operators);
        return true;
    }

    function checkACLPermission(string topicName) internal constant returns (bool) {
        ACL memory acl = ACLMap[topicName];
        return (acl.owner == tx.origin);
    }

    function checkOperatorPermission(string topicName, address operatorAddress) internal constant returns (bool) {
        ACL memory acl = ACLMap[topicName];
        if (acl.owner == operatorAddress) {
            return true;
        }
        address[] memory operatorArray = acl.operators;
        for (uint index = 0; index < operatorArray.length; index++) {
            if (operatorArray[index] == operatorAddress) {
                return true;
            }
        }
        return false;
    }

    function addOperator(string topicName, address operatorAddress) public returns (uint) {
        if (!checkACLPermission(topicName)) {
            return OPERATOR_NO_PERMISSION;
        }
        if (checkOperatorPermission(topicName, operatorAddress)) {
            return OPERATOR_ALREADY_EXIST;
        }
        ACLMap[topicName].operators.push(operatorAddress);
        return SUCCESS;
    }

    function delOperator(string topicName, address operatorAddress) public returns (uint) {
        if (!checkACLPermission(topicName)) {
            return OPERATOR_NO_PERMISSION;
        }
        if (!checkOperatorPermission(topicName, operatorAddress)) {
            return OPERATOR_NOT_EXIST;
        }
        uint operatorArrayLength = ACLMap[topicName].operators.length;
        for (uint index = 0; index < operatorArrayLength; index++) {
            if (ACLMap[topicName].operators[index] == operatorAddress) {
                break;
            }
        }
        if (index == operatorArrayLength-1) {
            delete ACLMap[topicName].operators[operatorArrayLength-1];
        } else {
            // remove empty element
            for (uint i = index; i < operatorArrayLength-1; i++){
                ACLMap[topicName].operators[i] = ACLMap[topicName].operators[i+1];
            }
        }
        ACLMap[topicName].operators.length--;
        return SUCCESS;
    }

    function listOperator(string topicName) public constant returns (uint code, address[] operatorArray) {
        code = SUCCESS;
        if (!checkACLPermission(topicName)) {
            code = OPERATOR_NO_PERMISSION;
        }
        operatorArray = ACLMap[topicName].operators;
    }

    function publishWeEvent(string topicName, string eventContent, string extensions) public returns (uint) {
        if (!checkOperatorPermission(topicName, tx.origin)) {
           return PUBLISH_NO_PERMISSION;
        }

        Snapshot memory snapshot = topicSnapshot[topicName];
        
        snapshot.sequence = snapshot.sequence + 1;
        snapshot.block = block.number;
        snapshot.timestamp = block.timestamp;
        snapshot.sender = tx.origin;
        
        topicSnapshot[topicName] = snapshot;
        
        return snapshot.sequence;
    }

    function getSnapshot(string topicName) public constant returns (uint lastSequence, uint lastBlock, uint lastTimestamp, address lastSender) {
        Snapshot memory snapshot = topicSnapshot[topicName];
        
        lastSequence = snapshot.sequence;
        lastBlock = snapshot.block;
        lastTimestamp = snapshot.timestamp;
        lastSender = snapshot.sender;
    }
    
    // flush data while upgrade
    function flushSnapshot(string[] topicName, uint[] lastSequence, uint[] lastBlock, uint[] lastTimestamp, address[] lastSender) public {
        for (uint i = 0; i < topicName.length; i++) {
            string memory oneName = topicName[i];
            Snapshot memory snapshot = topicSnapshot[oneName];
            if (0 == snapshot.timestamp) {
                snapshot.sequence = lastSequence[i];
                snapshot.block = lastBlock[i];
                snapshot.timestamp = lastTimestamp[i];
                snapshot.sender = lastSender[i];
                
                topicSnapshot[oneName] = snapshot;
            }
        }
    }    
}
