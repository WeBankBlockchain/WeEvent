var stompClient = null;
var topic = null;
var url = "";


function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#greetings").html("");
}


function showGreeting(type, tt, message) {
    $("#greetings").append("<tr><td>" + type + "</td><td>" + tt + "</td><td>" + message + "</td></tr>");
}

// 连接
function connect() {
    // 后台需要写死一个值
    url = $("#url").val();
    var socket = new SockJS(url); //'/gs-guide-websocket'
    stompClient = StompJs.Stomp.over(socket);

    topic = $("#topic").val();
    message = $("#message").val();
    var login = ""
    var passcode = ""

    // 连接的同时需要发布一个topic
    stompClient.connect(login, passcode, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        showGreeting("connect", "", "");
        // 通过rest 连接open
        var url = $("#urlparam");
        fetch('http://localhost:8080/weevent/rest/open?topic=com.webank.test.matthew', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: '{}',
        }).then(function (response) {
            //change the css
            if (response.ok) {
                $("#sendBox").show();
            }
            else {
                //   it is fail
                $("#connect").after("<p style=" + "color:red" + ">fail</p>");
            }
        })

        // fetch('http://127.0.0.1/weevent/rest/open?topic=com.webank.test.matthew', {
        //     method: 'POST',
        //     headers: {
        //         'Content-Type': 'application/json',
        //     },
        //     body: '{"com.webank.test.matthew"}',
        // }).then(function (response) {
        //     //change the css
        //     if (response.ok) {
        //         $("#sendBox").show();
        //     }
        //     else {
        //         //   it is fail
        //         $("#connect").after("<p style=" + "color:red" + ">fail</p>");
        //     }
        // })

        showGreeting("publish", topic, message);

    });

}

function sendName() {
    topic = $("#topic").val();
    message = $("#message").val();
    if (stompClient.connected) {
        // 发布topic,是否只需要考虑使用publish,通过不同类别
        
        stompClient.publish({
            destination: topic,
            body: message
        });
        
        showGreeting("send", topic, "");
    } else {
        console.log('fail...');
    }
}


function subscribeTopic() {
    subscribetopic = $("#subscribeTopic").val();
    // 消息订阅
    if (stompClient.connected) {
        console.log('stompClient.connected...');
        stompClient.subscribe(subscribetopic, function (greeting) {
            console.log('stompClient.connected subscribe...');
            showGreeting("subscribe", subscribetopic, "");
        }, {});
    } else {
        console.log('fail...');
    }

}


$(function () {
    // if not connected, it would be hide
    $("#sendBox").hide();
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });

    $("#send").click(function () {
        sendName();
    });
    $("#subscribe").click(function () {
        $("#connect").click();
        subscribeTopic();
    });
});
// stompClient.connected =true