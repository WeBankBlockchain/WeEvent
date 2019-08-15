var stompClient = null;
var topic = null;
//var url = window.location.origin+"/weevent/sockjs";

function setConnected(connected) {
    //$("#connect").prop("disabled", connected);
    $("#greetings").html("");
}

// 展示面板
function showGreeting(type, tt, message) {
    $("#greetings").append("<tr><td>" + type + "</td><td>" + tt + "</td><td>" + message + "</td></tr>");
}


function showGreeting(message) {
    if ((message != "Received data") && message.indexOf("SUBSCRIBE") > -1) {
        if (message.indexOf(">>>") > -1) {

            var text = "<tr style='background-color:#666;color: #fff;'><td>" + message + "</td></tr>"
            $("#greetings").prepend(text);
        }
        else {
            if (message.indexOf("<<<") > -1) {
                var text = "<tr><td>" + message + "</td></tr>"
                $("#greetings").prepend(text);
            } else {
                // message
                //if(message.replace(/[\r\n]/g,"").split("SUBSCRIBEversion:1.1")[1]!="message "){
                if (message.replace(/[\r\n]/g, "").indexOf("SUBSCRIBEversion:1.1") != -1) {
                    var text = "<tr style='background-color: #9b7c5e;color: #fff;'><td>" + message.replace(/[\r\n]/g, "").split("SUBSCRIBEversion:1.1")[1] + "</td></tr>"
                    $("#greetings").prepend(text);
                }
                //}
            }
        }
        //   add();
    } else {
        if ((message != "Received data") && message.indexOf(">>>") != -1) {
            $("#CreateTopicPannel").prepend("<tr><td>--------------------------</td></tr>");
            $("#CreateTopicPannel").prepend("<tr><td>" + message + "</td></tr>");
        }
        else {
            if (message != "Received data") {
                $("#CreateTopicPannel").prepend("<tr><td>" + message + "</td></tr>");
            }
        }
    }
}

// 连接
function connect() {
    var socket = new SockJS(url); //'/gs-guide-websocket'
    topic = $("#topic").val();
    stompClient = StompJs.Stomp.over(socket);
    topic = $("#topic").val();
    message = $("#message").val();
    var login = ""
    var passcode = ""
    stompClient.connect(login, passcode, function (frame) {
        setConnected(true);
        // console.log('Connected: ' + frame);

    });

}

function sendName() {
    topic = $("#topic").val();
    message = $("#message").val();
    if (stompClient.active) {
        // 发布topic,是否只需要考虑使用publish,通过不同类别
        stompClient.publish({
            destination: topic,
            body: message
        });
    } else {
        console.log('fail...');
    }
}


function subscribeTopic() {
    subscribeTopicContent = $("#subscribeTopic").val();
    // 消息订阅
    if (stompClient.active) {

        //console.log('stompClient.connected...');
        stompClient.subscribe(subscribeTopicContent, function (message) {
            if (message.body) {
                //      console.log('stompClient.connected subscribe...');
                console.log("got message with body " + message.body)

            } else {
                // console.log('stompClient.connected subscribe  ot empty message...');
                // showGreeting("[subscribe]", subscribeTopicContent, "");
            }
        }, {id: window.uuid, destination: subscribeTopicContent});
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

function createTopic() {
    topic = $("#topic").val();
    message = $("#message").val();
    console.log('open....');
    var baseUrl = 'http://localhost:8080/weevent/rest/open?topic=' + topic;
    fetch(baseUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: '{}',
    }).then(function (response) {

        //change the css
        if (response.ok) {
            $("#sendBox").show();
            // console.log(response);
        }
        else {
            //   it is fail
            $("#connect").after("<p style=" + "color:red" + ">fail</p>");
        }
    })
}

// function add()
// {
//     var now = new Date();
//     var div = document.getElementById('conversation');
//     div.innerHTML = div.innerHTML + 'time_' + now.getTime() + '<br />';
//     div.scrollTop = div.scrollHeight;
// }


function playVoice() {

}


// main
$(function () {
    // if not connected, it would be hide
    console.log = (function (oriLogFunc) {
        return function (str) {
            oriLogFunc.call(console, "----:" + str);
            showGreeting(str);
        }
    })(console.log);
    //console.log("userName");

    window.uuid = get_uuid();

    $("#sendBox").hide();
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        createTopic();
    });

    $("#send").click(function () {
        sendName();
    });
    $("#subscribe").click(function () {
        subscribeTopic();
    });

    connect();
});
// stompClient.connected =true