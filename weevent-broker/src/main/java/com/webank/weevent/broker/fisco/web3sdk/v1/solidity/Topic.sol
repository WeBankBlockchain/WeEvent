pragma solidity ^0.4.4;

contract Topic {
    uint sequenceNumber = 0;
    uint blockNumber = 0;
    event LogWeEvent(
        string topicName,
        uint eventSeq,
        uint eventBlockNumer,
        string eventContent,
        string extensions
    );

    function publishWeEvent(
        string topicName,
        string eventContent,
        string extensions
    )
        public
        returns (bool)
    {
        blockNumber = block.number;
        sequenceNumber = sequenceNumber + 1;
        LogWeEvent(topicName, sequenceNumber, block.number, eventContent, extensions);
        return true;
    }

    function getBlockNumber()
        public
        constant
        returns (uint){
        return blockNumber;
    }

    function getSequenceNumber()
        public
        constant
        returns (uint){
        return sequenceNumber;
    }
}
