pragma solidity ^0.4.4;

contract Topic {
    uint _sequence_start = 1;

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

        LogWeEvent(topicName, _sequence_start++, block.number, eventContent,extensions);
        return true;
    }
}
