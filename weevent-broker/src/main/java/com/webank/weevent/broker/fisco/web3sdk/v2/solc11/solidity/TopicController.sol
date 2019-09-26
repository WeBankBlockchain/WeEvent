// FISCO2.0.0 support highest 0.4.25
pragma solidity ^0.4.25;
// return TopicInfo[100]
pragma experimental ABIEncoderV2;

import "./Topic.sol";

contract TopicController {
    string constant private VERSION = "1.1";
    uint constant private TOPIC_ALREADY_EXIST = 500100;

    Topic private topic;
    mapping (string => TopicInfo) private topicMap;
    string[] private topicIndex;

    struct TopicInfo {
        address sender;
        uint timestamp;
        uint block;
    }
    
    constructor(address topicAddress) public {
        topic = Topic(topicAddress);
    }

    function addTopicInfo(string topicName) public returns (bool) {
        TopicInfo memory topicInfo = topicMap[topicName];       
        if (0 != topicInfo.timestamp) {
            return false;
        } else {
            topicInfo.sender = tx.origin;
            topicInfo.timestamp = block.timestamp;
            topicInfo.block = block.number;
            topicMap[topicName] = topicInfo;
            topicIndex.push(topicName);
            return true;
        }
    }

    function getTopicInfo(string topicName) public constant returns (bool exist, address sender, uint timestamp, uint block,
        uint lastSequence, uint lastBlock, uint lastTimestamp, address lastSender) {
        TopicInfo memory topicInfo = topicMap[topicName];
        exist = (0 != topicInfo.timestamp);
        if (exist) {
            sender = topicInfo.sender;
            timestamp = topicInfo.timestamp;
            block = topicInfo.block;
            
            (lastSequence, lastBlock, lastTimestamp, lastSender) = topic.getSnapshot(topicName);        
        }
    }

    function getTopicAddress() public constant returns (address) {
        return address(topic);
    }
    
    // page index start from 0, pageSize default 10
    function listTopicName(uint pageIndex, uint pageSize) public constant returns (uint total, uint size, string[100] topics) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        uint idx = pageIndex * pageSize;
        for (uint i = 0; i < pageSize; i++) {
            if (idx >= topicIndex.length) {
                break;
            }
            
            topics[i] = topicIndex[idx];
            idx = idx + 1;
        }

        total = topicIndex.length;
        size = i;
    }
}
