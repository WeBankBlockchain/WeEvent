pragma solidity ^0.4.4;

contract TopicData {
    mapping (string => TopicStruct) _topicMap;
    string[] public _topicStringArray;

    struct TopicStruct {
        address _topicAddress;
        address _senderAddress;
        uint _createdTimestamp;
    }

    function putTopic(
        string topicName,
        address topicAddress,
        uint createdTimestamp
    )
        public
        returns (bool)
    {
        TopicStruct memory topicStruct = TopicStruct({_topicAddress : topicAddress, _senderAddress: tx.origin, _createdTimestamp: createdTimestamp});
        _topicMap[topicName] = topicStruct;
        _topicStringArray.push(topicName);
        return true;
    }

    function getTopic(
        string topicName
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
        string topicName
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
        string topicName
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
            bytes32[100] topicList1,
            bytes32[100] topicList2)
    {
        uint topicIndexInArray = pageIndex * pageSize;

        for (uint i = 0; i < pageSize; i++) {
            if (topicIndexInArray >= _topicStringArray.length) {
                break;
            }
            if(bytes(_topicStringArray[topicIndexInArray]).length <= 32){
                topicList1[i] = stringToBytesVer2(_topicStringArray[topicIndexInArray]);
                topicList2[i] = "";
            }else{
                bytes memory bytesTopicName = bytes(_topicStringArray[topicIndexInArray]);
                string memory subStringTopicName1 = new string(32);
                string memory subStringTopicName2 = new string(32);
                bytes memory subBytesTopicName1 = bytes(subStringTopicName1);
                bytes memory subBytesTopicName2 = bytes(subStringTopicName2);

                for (uint k = 0; k < 32; k++) subBytesTopicName1[k] = bytesTopicName[k];
                uint h = 0;
                for (k = 32; k < bytesTopicName.length; k++) {
                    subBytesTopicName2[h] = bytesTopicName[k];
                    h++;
                }
                topicList1[i] = bytesToBytes32(subBytesTopicName1);
                topicList2[i] = bytesToBytes32(subBytesTopicName2);
            }
            topicIndexInArray++;
        }

        total = _topicStringArray.length;
        size = i;
    }

    function stringToBytesVer2(string memory source) returns (bytes32 result) {
        assembly {
            result := mload(add(source, 32))
        }
    }

    function bytesToBytes32(bytes memory source) returns (bytes32 result) {
        assembly {
            result := mload(add(source, 32))
        }
    }
}

