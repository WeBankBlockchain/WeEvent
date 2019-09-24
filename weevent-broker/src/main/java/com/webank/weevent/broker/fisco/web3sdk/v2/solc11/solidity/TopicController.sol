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
        string topicName,
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
        string topicName
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
        string topicName
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
            bytes32[] topicList1,
            bytes32[] topicList2)
    {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        uint size = 0;
        bytes32[100] memory staticTopicList1;
        bytes32[100] memory staticTopicList2;
        (total, size, staticTopicList1,staticTopicList2) = _topicData.listTopic(pageIndex, pageSize);

        bytes32[] memory dynamicTopicList1 = new bytes32[](size);
        bytes32[] memory dynamicTopicList2 = new bytes32[](size);
        for (uint i = 0; i < size; i++) {
            dynamicTopicList1[i] = staticTopicList1[i];
            dynamicTopicList2[i] = staticTopicList2[i];
        }

        topicList1 = dynamicTopicList1;
        topicList2 = dynamicTopicList2;
    }
}
