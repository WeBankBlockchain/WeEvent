var stompClient = null;
var stompClientSub = null;
var topic = null;
var url = null;
var login = "";
var passcode = "";
var type = 1
var subID = ''
var pubCon = '0'
var subCon = '0'
var options = {
    transports: ["websocket", "xhr", "xhr_send", "xhr_streaming", "eventsource", "htmlfile"]
}

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
    var socket = new SockJS(url, "", options); //'/gs-guide-websocket'
    stompClient = StompJs.Stomp.over(socket);
    if(login||passcode){
        stompClient.connect(login, passcode, function (frame) {
            next(frame)
        });
    } else {
        pubCon = '1'
        stompClient.connect('', function (frame) {
            next(frame)
        })
    }
    function next(frame){
        if(frame.command === "CONNECTED"){
            pubConnect = true
            console.log('publish connect'+frame)
            connectBtn(true)
        } else {
            connectBtn(false)
        }
    }
    socket.onclose = function(e) {
        type=1
        disconnectChannel()
    }
}

// Publish disconnect
function disconnectChannel() {
    if (stompClient.active) {
        pubCon = '0'
        stompClient.disconnect(function (e) {
            connectBtn(false)
            // var time = getTime()
            // if ($('#subscribe').css('display') == 'none') {
            //     disconnectSubscribe()
            // }
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
                var pubPart = $('.publish-part .cus_key')
                checkKey(pubPart)
                if(!checkKey(pubPart)){
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
    var socket = new SockJS(subscribetopic, "", options); //'/gs-guide-websocket'

    stompClientSub = StompJs.Stomp.over(socket);
    subCon = '1'
    if (login||passcode){
        stompClientSub.connect(login, passcode, function (e) {
            next(e)
        }),function(err) {
            console.log(err)
        };
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

    socket.onclose = function () {
        disconnectSubscribe()
    }
}

// Subscribe disconnect
function disconnectSubscribe() {
    if (stompClientSub.active) {
        subCon = '0'
        stompClientSub.disconnect(function (e) {
            // var time = getTime()
            // var str = '<p class="infor_list">'+ time +'- <br/> >>> Connection closed </p>'
            // $('#sub-message').prepend(str)
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
            var data = {
                id: window.uuid,
                groupId:groupId,
                eventId:eventId,
            }
            var items = $('#tagList').children()
            if(items.length>0){
                var subPart = $('.subscribe-part .cus_key')
                if(!checkKey(subPart)){
                    return
                } else {
                    for(var i=0; i<items.length;i++){
                        var key = $(items[i]).children('.cus_key').val()
                        var value = $(items[i]).children('.cus_value').val()
                        data[key] = value
                    }
                }
            }
            stompClientSub.subscribe(multiSubscribeTopicArr,function (message) {
                if (message.body) {
                    // console.log('stompClient.connected subscribe...');
                    var con = message.headers
                    let keyStr = ""
                    for (var key in con) {
                        if (key.indexOf('weevent') > -1) {
                            keyStr += key + ':' + con[key] + '<br/>'
                        }
                    }
                    console.log(keyStr)
                    data.body = message.body
                    // var str = '<p class="infor_list">'+ getTime() +' - <br/> <<< MESSAGE <br/>'+ JSON.stringify(data) + '</p>'
                    var str = '<p class="infor_list">'+ getTime() +' - <br/> <<< MESSAGE <br/>destination: '+ con.destination + '<br/>eventId: '+ con.eventId +'<br/>'+ keyStr +'content-length: '+ con['content-length'] + '<br/>'+ message.body +'</p>'
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
            destination: subID,
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

function add_item(e) {
    var item = "<div class='item'><span class='key'>Key:</span> <input type='text' class='cus_key checked'> <span class='value'>Value:</span> <input type='text' class='cus_value checked'> <i class='delate-one'>-</i><span class='warningbox'>Key 和 Value不能为空</span><span class='error_type'>Key 必须以'weevent-'为前缀</span></div>"
    e.append(item)
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
        if (message.indexOf('subscription-id') > -1) {
            var s = message
            s = s.split('RECEIPT')
            var strList = s[1].split(':')
            for (var o = 0; o < strList.length; o++) {
                if (strList[o].indexOf('subscription-id') > -1) {
                    subID = strList[0+1]
                }
            }
        }
        if(type===1) {
            $('#pub-message').prepend(str)
        }else{
            $('#sub-message').prepend(str)
        }
        if (message.indexOf(">>> DISCONNECT") > -1) {
            type = 0
        }
    }
    if (message.indexOf("Connection closed") > -1) {
        if (type != 0) {
            var str = '<p class="infor_list">'+ time +'- <br/> <<< Connection closed </p>'
            if (pubCon === '1') {
                $('#pub-message').prepend(str)
                pubCon = '0'
                connectBtn(false)
            }
            if (subCon === '1') {
                $('#sub-message').prepend(str)
                subCon = '0'
                subBtn(false)
                subscribe(false)
            }
        } else {
            if (subCon != '0' || pubCon!='0') {
                if (subCon != '0'){
                    type = 2
                }
                if (pubCon != '0'){
                    type = 1
                }
            }
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
    $('.error_type').css('opacity', 0)
    $('.publish-part .warningbox').css('opacity', 0)
    $('.publish-part .checked').each(function(){
        if($(this).val() == '') {
            $(this).parent().children('.warningbox').css('opacity', 1)
            index++
        }else{
            $(this).parent().children('.warningbox').css('opacity', 0)
        }
    })
    return index === 0;
}

// 检查 customer_header 的key 是不是以 'weevent-' 开头定义的
function checkKey (e) {
    var index = 0
    $(e).each(function(){
        var val = $(this).val()
        // 如果参数名部位weevent-开头就是错的
        if(val.substring(0,8) =='weevent-'){
            $(this).parent().children('.error_type').css('opacity', 0)
        }else{
            $(this).parent().children('.error_type').css('opacity', 1)  
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
            $(item).css('opacity', 1)
            index ++
        } else {
            var item = $(this).parent().children('.warningbox').css('opacity', 0)
        }
    })
    if(index===0){
        return true
    } else {
        return false
    }
}

function myBrowser () {
    var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    if (userAgent.indexOf("Chrome") > -1){
        return "Chrome"
    } else {
        confirm('为确保程序的正常运行,请切换至Chrome浏览器')
    }
}

$(function () {
    window.uuid = get_uuid();
    myBrowser();
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
        let evn= $('#newItem')
        add_item(evn)
    })

    $('#add-tag').click(function () {
        let evn= $('#tagList')
        add_item(evn)
    })

    $('input[type="radio"]').click(function(){
        changeEventId($(this))
    })
    $('.clear').click(function(){
        $(this).prev().empty()
    })
});
// stompClient.connected =true
