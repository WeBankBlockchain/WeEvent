pragma solidity ^0.4.4;

contract TopicData {
    // try to replace bytes32 with bytes[128] to extend key length
    mapping (bytes32 => TopicStruct) _topicMap;
    bytes32[] public _topicArray;

    struct TopicStruct {
        address _topicAddress;
        address _senderAddress;
        uint _createdTimestamp;
    }

    function putTopic(
        bytes32 topicName,
        address topicAddress,
        uint createdTimestamp
    ) 
        public
        returns (bool)
    {
        TopicStruct memory topicStruct = TopicStruct({_topicAddress : topicAddress, _senderAddress: tx.origin, _createdTimestamp: createdTimestamp});
        _topicMap[topicName] = topicStruct;
        _topicArray.push(topicName);
        return true;
    }

    function getTopic(
        bytes32 topicName
    )
        public 
        constant
        returns (
            address topicAddress,
            address senderAddress,
            uint createTimestamp) 
    {
        TopicStruct memory topicStruct = _topicMap[topicName];
        topicAddress = topicStruct._topicAddress;
        senderAddress = topicStruct._senderAddress;
        createTimestamp = topicStruct._createdTimestamp;
    }

    function getTopicAddress(
        bytes32 topicName
    )
        public
        constant
        returns (
            address topicAddress)
    {
        TopicStruct memory topicStruct = _topicMap[topicName];
        topicAddress = topicStruct._topicAddress;
    }

    function isTopicExist(
        bytes32 topicName
    )
        public
        constant
        returns (bool)
    {
        TopicStruct memory topicStruct = _topicMap[topicName];
        return 0 != topicStruct._createdTimestamp;
    }

    function listTopic(
        uint pageIndex,
        uint pageSize
    ) 
        public
        constant 
        returns (
            uint total,
            uint size,
            bytes32[100] topicList)
    {
        uint topicIndexInArray = pageIndex * pageSize;

        for (uint i = 0; i < pageSize; i++) {
            if (topicIndexInArray >= _topicArray.length) {
                break;
            }
            topicList[i] = _topicArray[topicIndexInArray];
            topicIndexInArray++;
        }

        total = _topicArray.length;
        size = i;
    }

}