pragma solidity ^0.4.4;

import "./TopicData.sol";

contract TopicController {
    uint constant private TOPIC_ALREADY_EXIST = 500100;

    TopicData private _topicData;

    event LogAddTopicNameAddress(
        uint retCode
    );

    function TopicController(
        address topicDataAddress
    )
        public
    {
        _topicData = TopicData(topicDataAddress);
    }

    function addTopicInfo(
        bytes32 topicName,
        address topicAddress
    )
        public
        returns (bool)
    {   
        if (_topicData.isTopicExist(topicName)) {
            LogAddTopicNameAddress(TOPIC_ALREADY_EXIST);
            return false;   
        } else {
            _topicData.putTopic(topicName, topicAddress, block.timestamp);
            return true;
        }
    }

    function getTopicInfo(
        bytes32 topicName
    )
        public
        constant
        returns (
            address topicAddress, 
            address senderAddress, 
            uint createdTimestamp) 
    {
        (topicAddress, senderAddress, createdTimestamp) = _topicData.getTopic(topicName);
    }

    function getTopicAddress(
        bytes32 topicName
    ) 
        public
        constant
        returns (address topicAddress)
    {
        topicAddress = _topicData.getTopicAddress(topicName);
    } 

    // page index start from 0, pageSize default 10
    function listTopicName(
        uint pageIndex,
        uint pageSize
    )
        public
        constant
        returns (
            uint total,
            bytes32[] topicList)
    {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        uint size = 0;
        bytes32[100] memory staticTopicList;
        (total, size, staticTopicList) = _topicData.listTopic(pageIndex, pageSize);

        bytes32[] memory dynamicTopicList = new bytes32[](size);
        for (uint i = 0; i < size; i++) {
            dynamicTopicList[i] = staticTopicList[i];
        }

        topicList = dynamicTopicList;
    }
}
