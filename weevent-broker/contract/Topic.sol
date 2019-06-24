pragma solidity ^0.4.4;

contract Topic {
    uint _sequence_start = 0;
    uint _block_number = 0;
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
        _block_number = block.number;
        _sequence_start = _sequence_start + 1;
        LogWeEvent(topicName, _sequence_start, block.number, eventContent, extensions);
        return true;
    }

    function getBlockNumber()
        public
        constant
        returns (uint){
        return _block_number;
    }

    function getSequenceNumber()
        public
        constant
        returns (uint){
        return _sequence_start;
    }
}
