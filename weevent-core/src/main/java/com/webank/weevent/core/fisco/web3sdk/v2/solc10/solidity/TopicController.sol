// FISCO2.0.0 support highest 0.4.25
pragma solidity ^0.4.25;
// support string[]
pragma experimental ABIEncoderV2;

import "./Topic.sol";

contract TopicController {
    // contract version 10, 11 and so on
    uint constant private VERSION = 10;

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
        }
        
        topicInfo.sender = tx.origin;
        topicInfo.timestamp = block.timestamp;
        topicInfo.block = block.number;
        
        topicMap[topicName] = topicInfo;
        topicIndex.push(topicName);

        topic.addTopicACL(topicName, tx.origin);
        return true;
    }

    function getTopicInfo(string topicName) public constant returns (bool exist, address topicSender, uint topicTimestamp, uint topicBlock,
        uint lastSequence, uint lastBlock, uint lastTimestamp, address lastSender) {
        TopicInfo memory topicInfo = topicMap[topicName];
        exist = (0 != topicInfo.timestamp);
        if (exist) {
            topicSender = topicInfo.sender;
            topicTimestamp = topicInfo.timestamp;
            topicBlock = topicInfo.block;
            
            (lastSequence, lastBlock, lastTimestamp, lastSender) = topic.getSnapshot(topicName);        
        }
    }
    
    function getTopicAddress() public constant returns (address) {
        return address(topic);
    }
    
    // page index start from 0, pageSize default 10
    function listTopicName(uint pageIndex, uint pageSize) public constant returns (uint total, uint size, string[] topics) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        total = topicIndex.length;
        uint idx = pageIndex * pageSize;
        if (topicIndex.length <= idx) {
            size = 0;
        }else{
            size = topicIndex.length >= idx + pageSize ? pageSize : topicIndex.length - idx;
            string[] memory localTopics = new string[](size);
            for (uint i = 0; i < pageSize; i++) {
                if (idx >= topicIndex.length) {
                    break;
                }

                localTopics[i] = topicIndex[idx];
                idx = idx + 1;
            }
            topics = localTopics;
        }
    }
    
    // flush data while upgrade
    function flushTopicInfo(string[] topicName, address[] topicSender, uint[] topicTimestamp, uint[] topicBlock,
        uint[] lastSequence, uint[] lastBlock, uint[] lastTimestamp, address[] lastSender) public {
        for (uint i = 0; i < topicName.length; i++) {
            string memory oneName = topicName[i];
            TopicInfo memory topicInfo = topicMap[oneName];
            if (0 == topicInfo.timestamp) {
                topicInfo.sender = topicSender[i];
                topicInfo.timestamp = topicTimestamp[i];
                topicInfo.block = topicBlock[i];
                
                topicMap[oneName] = topicInfo;
                topicIndex.push(oneName);
            }
        }

        topic.flushSnapshot(topicName, lastSequence, lastBlock, lastTimestamp, lastSender);
    }
}
