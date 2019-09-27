// FISCO2.0.0 support highest 0.4.25
pragma solidity ^0.4.25;
// support string[]
pragma experimental ABIEncoderV2;

contract Topic {
    mapping (string => Snapshot) private topicSnapshot;
    
    struct Snapshot {
        uint sequence;
        uint block;
        uint timestamp;
        address sender;
    }

    function publishWeEvent(string topicName, string eventContent, string extensions) public returns (uint) {
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
