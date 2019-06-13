var stompClient = null;
var topic = null;
var url = null;


function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    //$("#disconnect").prop("disabled", !connected);
    // if (connected) {
    //     $("#conversation").show();
    // }
    // else {
    //     $("#conversation").hide();
    // }
    $("#greetings").html("");
}


function showGreeting(type, tt, message) {
    $("#greetings").append("<tr><td>" + type + "</td><td>" + tt + "</td><td>" + message + "</td></tr>");
}

// 连接
function connect() {
    url = $("#url").val();
    var socket = new SockJS(url); //'/gs-guide-websocket'
    stompClient = StompJs.Stomp.over(socket);

    topic = $("#topic").val();
    message = $("#message").val();
    var login = "root"
    var passcode = "123456"
    stompClient.connect(login, passcode, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        showGreeting("connect", "", "");
    });
   // $("#connect").setD

}

function sendName() {
    topic = $("#topic").val();
    message = $("#message").val();
    if (stompClient.active) {
        console.log('stompClient.connected...');
        var receiptId = window.uuid + "-receipt";
        stompClient.publish({
            destination: topic, headers: {receipt: receiptId}, body: JSON.stringify({
                "message": message
            })
        });
        showGreeting("send", topic, "");
    } else {
        console.log('fail...');
    }
}


function subscribeTopic() {
    subscribetopic = $("#subscribeTopic").val();
    // 消息订阅
    if (stompClient.active) {
        console.log('stompClient.connected...');

        stompClient.subscribe(subscribetopic, function (message) {
            if (message.body) {
                console.log('stompClient.connected subscribe...');
                console.log("got message with body " + message.body)

                showGreeting("subscribe", subscribetopic, "");
            } else {
                console.log('stompClient.connected subscribe  ot empty message...');
                showGreeting("subscribe", subscribetopic, "");
            }
        }, {id: window.uuid, destination: subscribetopic});
    } else {
        console.log('fail...');
    }
}

// unsubscribeTopic
function unsubscribeTopic() {
    topicValue = $("#subscribeTopic").val();
    if (stompClient.active) {
        stompClient.unsubscribe(topicValue, {
            id: window.uuid,
            destination: topicValue,
            body: "unsubscribeTopic"
        });

    } else {
        console.log('fail...');
    }
}

//disconnectChannel
function disconnectChannel() {
    if (stompClient.active) {
        stompClient.disconnect(function () {
            console.log("disnnect");
        }, {receipt: window.uuid})
    } else {
        console.log("it is already disconnect")
    }
}

function subMultiSubscribeTopic() {
    multiSubscribeTopic = $("#multiSubscribeTopic").val();
    var multiSubscribeTopicArr = multiSubscribeTopic.split(",");
    //var topicOne = multiSubscribeTopicArr.shift()
    // 消息订阅
    if (stompClient.active) {
        console.log('stompClient.connected...');

        stompClient.subscribe(multiSubscribeTopicArr, function (message) {
            if (message.body) {
                console.log('stompClient.connected subscribe...');
                console.log("got message with body " + message.body)

                showGreeting("subscribe", multiSubscribeTopic, "");
            } else {
                console.log('stompClient.connected subscribe  ot empty message...');
                showGreeting("subscribe", multiSubscribeTopic, "");
            }

        }, {
            id: window.uuid,
            startposition: false
        });
    } else {
        console.log('fail...');
    }
}

function get_uuid() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}

$(function () {
    window.uuid = get_uuid();
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    //$( "#disconnect" ).click(function() { disconnect(); });
    $("#send").click(function () {
        sendName();
    });
    $("#subscribe").click(function () {
        subscribeTopic();
    });
    $("#unsubscribe").click(function () {
        unsubscribeTopic()
    })

    $("#disconnect").click(function () {
        disconnectChannel()
    })
    //multiSubscribe
    $("#multiSubscribe").click(function () {
        subMultiSubscribeTopic();
    });
});
// stompClient.connected =true