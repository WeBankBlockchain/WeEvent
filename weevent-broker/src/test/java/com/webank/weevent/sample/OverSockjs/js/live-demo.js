var stompClient = null;
var stompClientSub = null;
var topic = null;
var url = null;
var login = "";
var passcode = "";
var type =1


function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    //$("#disconnect").prop("disabled", !connected);
    // if (connected) {
    //     $("#conversation").show();
    // }
    // else {
    //     $("#conversation").hide();
    // }
    // $("#greetings").html("");
}

// Publish connect
function connect() {
    url = $("#url").val();
    var socket = new SockJS(url); //'/gs-guide-websocket'
    stompClient = StompJs.Stomp.over(socket);
    stompClient.connect(login, passcode, function (frame) {
        setConnected(true);
        // console.log('Connected: ' + frame);
        if(frame.command === "CONNECTED"){
            connectBtn(true)
        } else {
            connectBtn(false)
        }
    });
    // $("#connect").setD
}

// Publish disconnect
function disconnectChannel() {
    if (stompClient.active) {
        stompClient.disconnect(function (e) {
            connectBtn(false,'断开连接')
        }, {receipt: window.uuid})
    } else {
        console.log('it is already disconnect')
    }
}

// Publish send
function sendName() {
    try{
        if (stompClient.active) {
            // 如果 publish 部分存在未填部分则终止下面提交的流程
            if (!checkPublishInput()) {
                return
            }
            // 点击sand 设置type=1 则获取内容输出在publish的滚动框里 
            type=1
            topic = $("#topic").val();
            message = $("#message").val();
            var groupId = $('#group').val()
            var receiptId = window.uuid + "-receipt";
            var newItem = {
                receipt: receiptId,
                groupId:groupId
            }
            var items = $('#newItem').children()
            if(items.length>0){
                if(!checkKey()){
                    return
                } else {
                    for(var i=0; i<items.length;i++){
                        var key = $(items[i]).children('.cus_key').val()
                        var value = $(items[i]).children('.cus_value').val()
                        newItem[key] = value
                    }
                }
            }
            console.log('stompClient.connected...');
            stompClient.publish({
                destination: topic, headers: newItem, body: message
            });
            pubMember('send')
        } else {
            console.log('fail...');
            pubMember('send fail, client is not connected!', true)
        }
    }catch(e){
        console.log('fail...');
        pubMember('send fail, client is not connected!', true)
    }
}

// Subscribe connetc
function subscribeConnect() {
    var subscribetopic = $("#url").val();
    var socket = new SockJS(subscribetopic); //'/gs-guide-websocket'
    stompClientSub = StompJs.Stomp.over(socket);
    stompClientSub.connect(login, passcode, function (frame) {
        setConnected(true);
        console.log('subscribe.connected: ' + frame);
        if(frame.command === "CONNECTED"){
            subBtn(true)
            subMember('CONNECT', true)
        } else {
            console.log('fail...');
            subBtn(false)
            subMember('CONNECTED FAIL', true)
        }
    });
}

// Subscribe disconnect
function disconnectSubscribe() {
    if (stompClientSub.active) {
        stompClientSub.disconnect(function (e) {
            subBtn(false)
            unsubscribeTopic()
            subMember('DISCONNECTED', true)
        }, {receipt: window.uuid})
    } else {
        console.log('it is already disconnect')
        subBtn(false)
        subMember('it is already disconnected', true)
        unsubscribeTopic()
    }
}

// Subscribe subscribeTopic
function subMultiSubscribeTopic() {   
    // 消息订阅
    try{
        if (stompClientSub.active) {
            if(!checkSubInput()){
                return
            }
            type = 2 
            var multiSubscribeTopicArr = $("#multiSubscribeTopic").val();
            console.log('stompClient.connected...');
            subscribe(true)
            subMember('SUBSCRIBED', true)
            var groupId = $('#sendBox-group').val()
            var eventId = $('#eventId').val()
            var tag = $('#tag').val()
            var data = {
                id: window.uuid,
                startposition: false,
                groupId:groupId,
                eventId:eventId,
            }
            if(tag!=''){
                data.tag=tag
            }
            stompClientSub.subscribe(multiSubscribeTopicArr,function (message) {
                if (message.body) {
                    // console.log('stompClient.connected subscribe...');
                    var con = JSON.parse(message.body)
                    var content = $.base64.decode(con.content)
                    con.content = content
                    subMember(JSON.stringify(con), true)
                }
            },data);
        } else {
            console.log('fail...');
            subMember('client is not connectd!', true)
        }
    }catch(e){
        console.log('fail...');
        subMember('client is not connectd!', true)
    }
}

// Subscribe unsubscribeTopic
function unsubscribeTopic() {
    topicValue = $("#multiSubscribeTopic").val();
    if (stompClientSub.active) {
        subscribe(false,'关闭订阅')
        stompClientSub.unsubscribe(topicValue, {
            id: window.uuid,
            destination: topicValue,
            body: "unsubscribeTopic"
        })
        subMember('UNSUBSCRIBED!', true)
    } else {
        console.log('fail...');
        let item = $('#multiSubscribe').css('display')
        if(item == 'none') {
            subscribe(false,'订阅已取消')
            subMember('client is already closed', true)
        }
    }
}

function getTime() {
    var time = new Date
    var Y = time.getFullYear()
    var M = time.getMonth() +1
    var D = time.getDate()
    var hh = time.getHours()
    var mm = time.getMinutes()
    var ss = time.getSeconds()
    ss = ss<10?'0'+ss:ss
    var t = Y+'-'+M+'-'+D+' '+hh+':'+mm+':'+ss
    return t
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

function add_item() {
    var item = "<div class='item'><span class='key'>Key:</span> <input type='text' class='cus_key checked'> <span class='value'>Value:</span> <input type='text' class='cus_value checked'> <i class='delate-one'>-</i><span class='warningbox'>Key 和 Value不能为空</span><span class='error_type'>Key 必须以'weevent-'为前缀</span></div>"
    $('#newItem').append(item)
    $('.delate-one').unbind('click').bind('click', function(){
        delate_one($(this))
    })
}

function delate_one(e) {
     var item = $(e).parent()
     $(item[0]).remove()
}

// Publich click connect-btn / change button style 
function connectBtn(btn, text) {
    $('#connect').hide()
    $('#disconnect').hide()
    // 点击publich connect
    if(btn){
        $('#disconnect').show()
        pubMember("CONNECT", true)
    } else {
        $('#connect').show()
        pubMember("DISCONNECT", true)
    }
}
// Subscribe click connect-btn / change button style 
function subBtn(btn, text) {
    $('#subscribe').hide()
    $('#dissubscribe').hide()
    if(btn){
        $('#dissubscribe').show()
    } else {
        $('#subscribe').show()
    }
}
// Subscribe click subscribe-btn / change button style
function subscribe(btn, text) {
    $('#multiSubscribe').hide()
    $('#unsubscribe').hide()
    if(btn){
        $('#unsubscribe').show()
    }else{
        $('#multiSubscribe').show()
    }
}

function changeEventId(e) {
    var id = $(e).attr('id')
    switch (id) {
        case 'start':
        $('#eventId').val('OFFSET_FIRST');
        $('#eventId').attr('disabled','disabled')
        break;
        case 'end':
        $('#eventId').val('OFFSET_LAST');
        $('#eventId').attr('disabled','disabled')
        break;
        case 'last':
        $('#eventId').val('');
        $('#eventId').removeAttr('disabled')
        break;
    }
}

// Publish click Send-btn 
function pubMember(message, isClickConnect){
    var time = getTime()
    if (isClickConnect) {
        var pubMember = '<p class="infor_list">'+ time +' - <br/>'+ message +'</p>'
        $('#pub-message').append(pubMember)
    } else {
        if (message.indexOf(">>> SEND") > -1 || message.indexOf("<<< ERROR") > -1) {
            var pubMember = '<p class="infor_list">'+ time +' - <br/>'+ message +'</p>'
            $('#pub-message').append(pubMember)
        }
    }
}

function subMember(str, isClickSubscrib) {
    var time = getTime()
    if(isClickSubscrib) {
        var subMember= "<p class='infor_list'>"+time+" -<br/>"+str+"</p>"
        $('#sub-message').append(subMember)
    }else{
        if (str.indexOf(">>> SEND") > -1 || str.indexOf("<<< ERROR") > -1) {
            var subMember= "<p class='infor_list'>"+time+" -<br/>"+str+"</p>"
            $('#sub-message').append(subMember)
        }
    }
}

// check publish-part input is empty
function  checkPublishInput () {
    var index = 0
    $('.error_type').hide()
    $('.publish-part .warningbox').hide()
    $('.publish-part .checked').each(function(){
        if($(this).val() == '') {
            $(this).parent().children('.warningbox').show()
            index++
        }else{
            $(this).parent().children('.warningbox').hide()
        }
    })
    if(index==0){
        return true
    } else {
        return false
    }
}

function checkKey () {
    var index = 0
    $('.publish-part .cus_key').each(function(){
        var val = $(this).val()
        // 如果参数名部位weevent-开头就是错的
        if(val.substring(0,8) =='weevent-'){
            $(this).parent().children('.error_type').hide()
        }else{
            $(this).parent().children('.error_type').show()
            index++
        }
    })
    if(index==0){
        return true
    } else {
        return false
    }
}

function checkSubInput () {
    var index = 0
    $('.subscribe-part .checked').each(function(){
        if($(this).val() == '') {
            var item = $(this).parent().children('.warningbox')
            $(item).show()
            index ++
        } else {
            var item = $(this).parent().children('.warningbox').hide()
        }
    })
    if(index==0){
        return true
    } else {
        return false
    }
}

$(function () {
    window.uuid = get_uuid();
    // console.log = (function (oriLogFunc) {
    //     return function (str) {
    //         oriLogFunc.call(console, "--:" + str);
    //         if(type==1) {
    //             pubMember(str, false)
    //         }
    //     }
    // })(console.log);

    $("#connect").click(function (e) {
        e.stopPropagation();
        e.preventDefault();
        connect();
    });

    $("#disconnect").click(function () {
        disconnectChannel()
    })

    //$( "#disconnect" ).click(function() { disconnect(); });
    $("#send").click(function () {
        sendName();
    });
    $("#subscribe").click(function () {
        subscribeConnect();
    });

    $('#dissubscribe').click(function () {
        disconnectSubscribe()
    })

    //multiSubscribe
    $("#multiSubscribe").click(function () {
        subMultiSubscribeTopic();
    });

    $("#unsubscribe").click(function () {
        unsubscribeTopic()
    })

    $('#add-one').click(function () {
        add_item()
    })

    $('input[type="radio"]').click(function(){
        changeEventId($(this))
    })
    $('.clear').click(function(){
        $(this).prev().empty()
    })
});
// stompClient.connected =true