pragma solidity ^0.4.4;

contract Topic {
    uint _sequence_start = 1;

    event LogWeEvent(
        bytes32 topicName,
        uint eventSeq,
        uint eventBlockNumer,
        string eventContent
    );

    function publishWeEvent(
        bytes32 topicName,
        string eventContent
    )
        public
        returns (bool) 
    {
        LogWeEvent(topicName, _sequence_start++, block.number, eventContent);
        return true;
    }
}
