var stompClient = null;
var stompClientSub = null;
var topic = null;
var url = null;
var login = "";
var passcode = "";
var type =1


function setConnected(connected) {
    // $("#connect").prop("disabled", connected);
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
    if(login||passcode){
        stompClient.connect(login, passcode, function (frame) {
            next(frame)
        });
    } else {
        stompClient.connect('', function (frame) {
            next(frame)
        });
    }
    function next(frame){
        if(frame.command === "CONNECTED"){
            console.log('publish connect'+frame)
            connectBtn(true)
        } else {
            connectBtn(false)
        }
    }
}

// Publish disconnect
function disconnectChannel() {
    if (stompClient.active) {
        stompClient.disconnect(function (e) {
            connectBtn(false)
        }, {receipt: window.uuid})
    } else {
        console.log('it is already disconnect')
        connectBtn(false)
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
        } else {
            console.log('send fail, client is not connected!');
        }
    }catch(e){
        console.log('send fail, client is not connected!');
    }
}

// Subscribe connetc
function subscribeConnect() {
    var subscribetopic = $("#url").val();
    var socket = new SockJS(subscribetopic); //'/gs-guide-websocket'
    stompClientSub = StompJs.Stomp.over(socket);
    if (login||passcode){
        stompClientSub.connect(login, passcode, function (e) {
            next(e)
        });
    } else{
        stompClientSub.connect('', function (e) {
            next(e)
        });
    }
    
    function next(frame) {
        try{
            console.log('subscribe.connected: ' + frame);
            if(frame.command === "CONNECTED"){
                subBtn(true)
            } else {
                console.log('DISCONNECTED');
                subBtn(false)
            }
        }catch(e){

        }
    }   
}

// Subscribe disconnect
function disconnectSubscribe() {
    if (stompClientSub.active) {
        stompClientSub.disconnect(function (e) {
            subBtn(false)
            unsubscribeTopic()
        }, {receipt: window.uuid})
    } else {
        console.log('it is already disconnect')
        subBtn(false)
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
                data = {
                    id: window.uuid,
                    startposition: false,
                    groupId:groupId,
                    eventId:eventId,
                    'weevent-tag':tag
                }
            }
            stompClientSub.subscribe(multiSubscribeTopicArr,function (message) {
                if (message.body) {
                    // console.log('stompClient.connected subscribe...');
                    var con = JSON.parse(message.body)
                    var content = $.base64.decode(con.content)
                    con.content = content
                    var time = getTime()
                    var str = '<p class="infor_list">'+ time +' - <br/>'+ JSON.stringify(con) +'</p>'
                    $('#sub-message').prepend(str)
                }
            },data);
        } else {
            console.log('send fail...');
        }
    }catch(e){
        console.log('send fail...');
    }
}

// Subscribe unsubscribeTopic
function unsubscribeTopic() {
    topicValue = $("#multiSubscribeTopic").val();
    if (stompClientSub.active) {
        subscribe(false)
        stompClientSub.unsubscribe(topicValue, {
            id: window.uuid,
            destination: topicValue,
            body: "unsubscribeTopic"
        })
    } else {
        console.log('fail...');
        let item = $('#multiSubscribe').css('display')
        if(item == 'none') {
            subscribe(false)
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
function connectBtn(btn) {
    type = 1
    $('#connect').hide()
    $('#disconnect').hide()
    // 点击publich connect
    if(btn){
        $('#disconnect').show()
    } else {
        $('#connect').show()
    }
}
// Subscribe click connect-btn / change button style 
function subBtn(btn) {
    $('#subscribe').hide()
    $('#dissubscribe').hide()
    if(btn){
        $('#dissubscribe').show()
    } else {
        $('#subscribe').show()
    }
}
// Subscribe click subscribe-btn / change button style
function subscribe(btn) {
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
function getMember(message){
    var time = getTime()
    if (message.indexOf(">>> SEND") > -1 || message.indexOf("<<< ERROR") > -1 || message.indexOf("<<< CONNECTED")>-1||message.indexOf(">>> DISCONNECT")>-1||message.indexOf("send fail")>-1||message.indexOf(">>> SUBSCRIBE")>-1||message.indexOf(">>> UNSUBSCRIBE")>-1) {
        var str = '<p class="infor_list">'+ time +'-'+type+' - <br/>'+ message +'</p>'
        if(type==1) {
            $('#pub-message').prepend(str)
        }else{
            $('#sub-message').prepend(str)
        }  
    }
}

function subMember(str) {
    var time = getTime()
    if (message.indexOf(">>> SEND") > -1 || message.indexOf("<<< ERROR") > -1 || message.indexOf("<<< CONNECTED")>-1||message.indexOf(">>> DISCONNECT")>-1||message.indexOf("send fail")>-1) {
        var subMember= "<p class='infor_list'>"+time+" -<br/>"+message+"</p>"
       
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

// 检查 customer_header 的key 是不是以 'weevent-' 开头定义的
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

// check subscribe-part input is empty
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
    console.log = (function (oriLogFunc) {
        return function (str) {
            oriLogFunc.call(console, "--:" + str);
            getMember(str)
        }
    })(console.log);

    $("#connect").click(function (e) {
        type=1
        e.stopPropagation();
        e.preventDefault();
        connect();
    });

    $("#disconnect").click(function () {
        type=1
        disconnectChannel()
    })

    //$( "#disconnect" ).click(function() { disconnect(); });
    $("#send").click(function () {
        type=1
        sendName();
    });
    $("#subscribe").click(function () {
        type=2
        subscribeConnect();
    });

    $('#dissubscribe').click(function () {
        type=2
        disconnectSubscribe()
    })

    //multiSubscribe
    $("#multiSubscribe").click(function () {
        type=2
        subMultiSubscribeTopic();
    });

    $("#unsubscribe").click(function () {
        type=2
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