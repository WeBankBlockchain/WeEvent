// FISCO2.0.0 support highest 0.4.25
pragma solidity ^0.4.25;
// return Snapshot
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

    function getSnapshot(string topicName) public constant returns (uint sequence, uint block, uint timestamp, address sender) {
        Snapshot memory snapshot = topicSnapshot[topicName];
        sequence = snapshot.sequence;
        block = snapshot.block;
        timestamp = snapshot.timestamp;
        sender = snapshot.sender;
    }
}
