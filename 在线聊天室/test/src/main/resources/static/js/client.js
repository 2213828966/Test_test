//////////////////////////////////////////////////
// 这里实现标签页的切换
//////////////////////////////////////////////////

function initSwitchTab() {
    let tabSession = document.querySelector('.tab .tab-session');
    let tabFriend = document.querySelector('.tab .tab-friend');
    // querySelectorAll 可以同时选中多个元素，得到的结果是个数组
    // [0] 是会话列表
    // [1] 是好友列表
    let lists = document.querySelectorAll('.list');
    // 2. 针对标签页按钮, 注册点击事件. 
    //    如果是点击 会话标签按钮, 就把会话标签按钮的背景图片进行设置. 
    //    同时把会话列表显示出来, 把好友列表隐藏
    //    如果是点击 好友标签按钮, 就把好友标签按钮的背景图片进行设置. 
    //    同时把好友列表显示出来, 把会话列表进行隐藏
    tabSession.onclick = function() {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/对话.png)';
        tabFriend.style.backgroundImage = 'url(img/用户2.png)';
        // b) 让会话列表显示出来，让好友列表隐藏
        lists[0].classList = 'list';
        lists[1].classList = 'list hide';
    }

    tabFriend.onclick = function() {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/对话2.png)';
        tabFriend.style.backgroundImage = 'url(img/用户.png)';
        // b) 让好友列表显示，让会话列表隐藏
        lists[0].classList = 'list hide';
        lists[1].classList = 'list';
    }
}

initSwitchTab();

//////////////////////////////////////////////////
// 实现添加好友按钮
//////////////////////////////////////////////////
function addPeople(){
    let text = document.querySelector('.search input');
    let button = document.querySelector('.search button');
    button.onclick = function() {
        if(!text.value){
            return;
        }
        $.ajax({
            type: 'get',
            url: 'searchUser?username=' + text.value, 
            success: function(body) {
                console.log("搜索结果:", body);
                if(!body || !body.exists) {
                    alert("用户不存在");
                    return;
                }
                if(body.isFriend == true) {
                    alert("该用户已经是您的好友");
                    return;
                }
                if(confirm("是否添加 " + body.username + " 为好友？")) {
                    let req = {
                        type: 'friendRequest',
                        toUserId: body.userId,
                        content: '请求添加好友'
                    };
                    websocket.send(JSON.stringify(req));
                    alert("好友请求已发送！");
                }
            },
            error: function(){
                console.log("查询用户失败");
            }
        });
    };
}
addPeople();

//////////////////////////////////////////////
// 操作 websocket
//////////////////////////////////////////////

// 创建 websocket  实例
// 自己写的代码在自己电脑上运行的
//let websocket = new WebSocket("ws://127.0.0.1:8080/WebSocketMessage");

// 部署云服务器
// location.host 获取到当前页面的ip和端口（无论 client.htlm 在那个服务器运行）
let websocket = new WebSocket("ws://"+ location.host+"/WebSocketMessage");

websocket.onopen = function(){
    console.log("websocket 连接成功!");
}

//////////////////////////////////////////////
// 处理 消息 逻辑
//////////////////////////////////////////////
websocket.onmessage = function(e){
    console.log("websocket 收到消息!" + e.data);
    let resp = JSON.parse(e.data);
    if(resp.type == 'message'){
        // 处理好友发来的正常消息
        handleMessage(resp);
    }else if(resp.type == 'friendRequest'){
        // 处理收到的好友请求
        handleFriendRequest(resp);
    }else if(resp.type == 'friendResponse'){
        // 处理好友请求的响应
        handleFriendResponse(resp);
    }
}

// 处理收到的好友请求
function handleFriendRequest(resp){
    if(confirm(resp.fromName + " 请求添加您为好友，是否同意？")){
        // 同意好友请求
        let response = {
            type: 'friendResponse',
            toUserId: resp.fromUserId,
            accepted: true,
            content: '已同意您的好友请求'
        };
        websocket.send(JSON.stringify(response));
        // 刷新好友列表
        getFriendList();
    }else{
        // 拒绝好友请求
        let response = {
            type: 'friendResponse',
            toUserId: resp.fromUserId,
            accepted: false,
            content: '已拒绝您的好友请求'
        };
        websocket.send(JSON.stringify(response));
    }
}

// 处理好友请求的响应
function handleFriendResponse(resp){
    if(resp.accepted){
        alert("对方已同意您的好友请求！");
        // 刷新好友列表
        getFriendList();
    }else{
        alert("对方已拒绝您的好友请求！");
    }
}

websocket.onclose = function(){
    console.log("websocket 连接关系!");
}

websocket.onerror = function(){
    console.log("websocket 连接异常!");
}

//////////////////////////////////////////////
// 把 客户端收到的消息展示出来
//////////////////////////////////////////////
function handleMessage(resp){

    
    // 展示到 对应的会话预览区,以及右侧的消息列表中

    // 1. 根据响应中的 sessionId 获取到当前的 li 标签，如果 li 标签不存在，就创建一个新的
    let curSessionLi = findSessionLi(resp.sessionId);
    if(curSessionLi == null){
        // 需要创建一个 新的li 标签，表示新会话
        curSessionLi = document.createElement('li');
        curSessionLi.setAttribute('message-session-id',resp.sessionId);
        // 此处 p 标签内部应该芳消息的预览内容
        curSessionLi.innerHTML = '<h3>' + resp.fromName + '</h3>'
            + '<p></p>';
        // 给这个 li 标签也加上点击事件的处理
        curSessionLi.onclick() = function() {
            clickSession(curSessionLi);
        }
    }
    // 2. 把新的消息显示到会话预览区（li 标签里的 p 标签）,如果消息太长，需要截断
    let p = curSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if(p.innerHTML.length > 10){
        p.innerHTML = p.innerHTML.substring(0,10) + '...';
    }
    // 3. 把收到消息的会话，放到会话列表最上面
    let sessionListUL = document.querySelector('#session-list');
    sessionListUL.insertBefore(curSessionLi,sessionListUL.children[0]); 
    // 4. 如果当前收到消息的会话处于被选中状态，则把当前的消息给放到右侧消息列表中
    //    新增消息的同时，注意滚动条的位置，保证新消息能被用户看到
    if(curSessionLi.className == 'selected'){
        // 该会话处于被选中状态,则把当前的消息发给右侧消息列表中
        let  messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv,resp);
        scrollBottom(messageShowDiv);
    }
}

//////////////////////////////////////////////
// 获取到所有的会话列表中的 li 标签
//////////////////////////////////////////////
function findSessionLi(targetSessionId){
    let sessionLis = document.querySelectorAll('#session-list li');
    for(let li of sessionLis){
        let sessionId = li.getAttribute('message-session-id');
        if(sessionId == targetSessionId){
            return li;
        }
    }
}


//////////////////////////////////////////////
// 实现消息 发送/接收 逻辑
//////////////////////////////////////////////
function initSendButton() {
    // 1. 获取到发送按钮 和  消息输入框
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');

    // 2. 给发送按钮注册一个点击事件
    sendButton.onclick = function() {
        // a) 先针对输入框的内容做个简单判定，比如输入框内容为空，则啥都不干
        if(!messageInput.value) {
            // value 的值是 null 或者 ‘’ 都会触发这个条件
            return;
        }
        // b) 获取当前选中的 li 标签 sessionId
        let selectedLi = document.querySelector('#session-list .selected');
        if(selectedLi == null){
            // 当前 li 标签未被选中
            return;
        }
        let sessionId = selectedLi.getAttribute('message-session-id');

        // c) 构造 json 数据
        let req = {
            type: 'message',
            sessionId: sessionId,
            content: messageInput.value
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: "+ req);
        // d) 通过 websocket 发送消息
        websocket.send(req);
        // e) 发送完成之后，清空之前的输入框
        messageInput.value = '';
    }
}
initSendButton();


//////////////////////////////////////////////
// 从服务器获取用户登录的信息
//////////////////////////////////////////////
function getUserInfo(){
    $.ajax({
        type: 'get',
        url: '/userInfo',
        success: function(body){
            // 从服务器获取到数据 body
            // 校验结果是否有效
            if(body.userId && body.userId > 0){
                // 如果有效，则把用户名显示到界面上
            // 同时也可以记录 userId 记录到 html 属性中
                let userDiv = document.querySelector('.main .left .user');
                userDiv.innerHTML = body.username;
                userDiv.setAttribute("user-id",body.userId);
            }else{
                 // 如果无效跳转到登录界面
                 alert("当前用户未登录");
                 location.assign('login.html');
            }
           
            
        }
    });
}
getUserInfo();


/////////////////////////////////////////////////
// 获取登录用户的好友列表
/////////////////////////////////////////////////
function getFriendList(){
    $.ajax({
        type:'get',
        url:'friendList',
        success:function(body){
            // 1.先把之前的好友列表内容清空
            let friendListUL = document.querySelector('#friend-list');
            friendListUL.innerHTML = '';
            // 2.遍历body， 把服务器相应的结果，加回到当前的 friend-list 中
            for(let friend of body){
                let li = document.createElement('li');
                li.innerHTML = '<h4>' + friend.friendName + '</h4>';
                // 此处把 friendId 页记录以备后用
                // 此处把 friendId 作为 html 的自定义属性，加到 li 标签上就行了
                li.setAttribute('friend-id', friend.friendId);
                friendListUL.appendChild(li);

                // 每个 li 标签，对应界面上的一个好友的选项，给 li 加上点击事件 的处理
                li.onclick = function() {
                    // 参数表示区分了当前用户点击的是哪个好友
                    clickFriend(friend);
                }

                // 添加右键菜单事件
                li.oncontextmenu = function(e) {
                    e.preventDefault();  // 阻止默认右键菜单
                    if(confirm("是否删除好友 " + friend.friendName + "？")) {
                        // 发送删除好友请求
                        $.ajax({
                            type: 'post',
                            url: 'deleteFriend?friendId=' + friend.friendId,
                            success: function(body) {
                                alert("删除好友成功！");
                                // 刷新好友列表
                                getFriendList();
                                // 刷新会话列表
                                getSessionList();
                            },
                            error: function() {
                                alert("删除好友失败！");
                            }
                        });
                    }
                };
            }
        },
        error: function(){
            console.log('获取好友列表失败');
        }
    });
}
getFriendList();

/////////////////////////////////////////////////
// 获取登录用户的 会话列表
/////////////////////////////////////////////////
function getSessionList() {
    $.ajax({
        type: 'get',
        url: 'sessionList',
        success: function(body){
            // 1. 先清空 之前的会话列表
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML = '';
            // 2. 遍历相应的数量，针对结果构造页面
            for(let session of body){
                // 针对 lastMessage 长度进行截断处理
                if(session.lastMessage.length > 10){
                    session.lastMessage = session.lastMessage.substring(0,10)+'...';
                }
                let li = document.createElement('li');
                // 把会话id 保存到 li 标签的自定义属性中
                li.setAttribute('message-session-id',session.sessionId);
                li.innerHTML = '<h3>'+ session.friends[0].friendName + '</h3>' 
                                     + '<p>' + session.lastMessage + '</p>';
                sessionListUL.appendChild(li);

                // 给 li 标签新增点击事件
                li.onclick = function(){
                    // 此处对应的 clickSession 函数的参数可以保证是点击的 li 标签
                    clickSession(li);
                }

                // 添加右键菜单事件
                li.oncontextmenu = function(e) {
                    e.preventDefault();
                    if(confirm("是否删除该会话？")) {
                        $.ajax({
                            type: 'post',
                            url: 'deleteSession?sessionId=' + session.sessionId,
                            success: function(body) {
                                alert("删除会话成功！");
                                getSessionList();
                            },
                            error: function() {
                                alert("删除会话失败！");
                            }
                        });
                    }
                };
            }
        }
    });
}
getSessionList();


/////////////////////////////////////////////////
// 点击 会话
/////////////////////////////////////////////////
function clickSession(currentLi){
    // 1. 设置 高亮
    let allLis = document.querySelectorAll('#session-list>li');
    activeSession(allLis,currentLi);
    // 2. 获取指定会话的历史消息 TODO
    let sessionId = currentLi.getAttribute("message-session-id");
    getHistoryMessage(sessionId);
}

function activeSession(allLis,currentLi){
    // 这里的 for 循环更主要的目的是取消未被选中的 li 的className
    for(let li of allLis){
        if(li == currentLi){
            li.className = 'selected';
        }else{
            li.className = '';
        }
    }
}


// 获取指定会话的历史消息
function getHistoryMessage(sessionId){
    console.log("获取历史消息 sessionId="+sessionId);
    //1. 先清空右侧消息列表已有内容
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';

    //2. 重新设置会话标题，就是点击会话时的那个标题，先找到当前选中的会话是谁
    // 之前设置了那个会话备选中，就会加上一个类名 selected 
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if(selectedH3){
        // selecteH3 可能不存在，比如页面加载 阶段，可能没有那个会话被选中
        // 也就没有 会话带有 selected 标签，此时就无法查询出这个 selecteH3
        titleDiv.innerHTML = selectedH3.innerHTML;
    }

    // 发送一个 ajax 请求，获取该会话的历史消息
    $.ajax({
        type: 'get',
        url: 'message?sessionId='+ sessionId,
        success: function(body){
            // 此处返回的 body 是个 js 对象数组，里面的每个元素都是一条消息
            // 直接遍历
            for(let message of body){
                addMessage(messageShowDiv,message);
            }
            // 滚动条自动到最下方
            scrollBottom(messageShowDiv);
        }
    });
}

function addMessage(messageShowDiv,message){
    let messageDiv = document.createElement('div');
    // 此处需要针对当前消息是不是用户自己发的，决定靠左靠右
    let selfUsername = document.querySelector('.left .user').innerHTML;
    if(selfUsername == message.fromName){
        // 消息是自己的 ，靠右
        messageDiv.className = 'message message-right';
    }else{
        // 消息是别人的，靠左
        messageDiv.className = 'message message-left';
    }
    messageDiv.innerHTML = '<div class="box">'
    + '<h4>' + message.fromName + '</h4>'
    + '<p>' + message.content + '</p>'
    + '</div>';
    messageShowDiv.appendChild(messageDiv);

    // 打印生成的HTML结构，检查是否正确
    //console.log(messageDiv.outerHTML);
}

/////////////////////////////////////////////////
// 把messageShowDiv 的内容滚动到底部
/////////////////////////////////////////////////
function scrollBottom(elem){
    // 1.获取到可视区域的高度
    let clientHeight = elem.offsetHeight;
    // 2.获取到内容的总高度
    let scrollHeight = elem.scrollHeight;
    // 3.进行滚动操作，第一个参数是水平方向滚动的尺寸，第二个是垂直方向
    elem.scrollTo(0,scrollHeight - clientHeight);
}


/////////////////////////////////////////////////
// 点击好友列表项触发的函数
/////////////////////////////////////////////////
function clickFriend(friend){
    // 1.先判定一下当前这个好友是否有对应的会话
    // 使用 用户的名字 friendName 来查找会话列表里是否有与该用户的会话，有就返回会话列表里的 li 没有就返回 null
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUL = document.querySelector('#session-list');
    if(sessionLi){
        // 2.如果存在匹配的结果，就把这个会话设置成选中状态，并且置顶
        // 使用 insertBefore 把这个找到的 li 标签放到 最前面
        sessionListUL.insertBefore(sessionLi,sessionListUL.children[0]);
        // 设置选中的 会话 高亮
        clickSession(sessionLi);
        // 因为该会话已经存在于 会话列表中，所有 onclick() 也已经设置过了，可以模拟点击
        //sessionLi.click();
    }else{
        // 3.如果不存在匹配的结果，就创建一个新会话(创建 li 标签 + 通知服务器)
        sessionLi = document.createElement('li');
        // 新会话 没有 lastMessage ，<p> 标签为空
        sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>'+'<p></p>';
        // 把 标签置顶
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.onclick = function(){
            clickSession(sessionLi);
        }
        sessionLi.click();
        // 发送消息给服务器，告诉服务器当前新建的会话是啥样的
        createSession(friend.friendId,sessionLi);
    }
    // 4.还需要把标签页给切换到 会话列表
    // 实现方式，只要找到会话列表标签页按钮，模拟一个点击操作
    let tabSession = document.querySelector('.tab .tab-session');
    tabSession.click();
}

function findSessionByName(username){
    // 先获取到会话列表中所有的 li 标签
    // 然后 依次遍历，看看这些 li 标签 谁的名字和 要查找的名字一致 
    let sessionLis = document.querySelectorAll('#session-list>li');
    for(let sessionLi of sessionLis){
        // 获取到该标签里的 h3 标签，进一步得到名字
        let h3 = sessionLi.querySelector('h3');
        if(h3.innerHTML == username){
            return sessionLi;
        }
    }
    return null;
}

// friendId 是构造 HTTP 请求时必备的信息
function createSession(friendId,sessionLi){
    $.ajax({
        type: 'post',
        url: 'session?toUserId='+ friendId,
        success: function(body){
            console.log("会话创建成功 sessionId = "+body.sessionId);
            sessionLi.setAttribute('message-session-id',body.sessionId);
        },
        error: function(){
            console.log("会话创建失败！");
        }
    });
}
