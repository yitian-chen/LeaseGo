# 聊天室 WebSocket 接口文档

## 连接信息

| 项目 | 说明                                    |
|------|---------------------------------------|
| WebSocket URL | `ws://{host}:{port}/app/chat`         |
| 认证方式 | 通过 URL 参数传递 userId 和 username         |
| 示例连接 | `ws://localhost:8081/app/chat?token=` |

---

## 消息格式

### 1. 发送私信（前端 → 后端）

**请求示例：**
```json
{
  "toId": 9,
  "message": "你好"
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| toId | Long | 接收者的用户 ID |
| message | String | 消息内容 |

---

### 2. 接收消息（后端 → 前端）

#### 普通消息响应

**接收示例：**
```json
{
  "isSystem": false,
  "fromId": 8,
  "fromName": "chen",
  "data": "你好"
}
```

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| isSystem | Boolean | 是否为系统消息 (false = 普通消息) |
| fromId | Long | 发送者 ID |
| fromName | String | 发送者用户名 |
| data | String/Object | 消息内容（私聊时为字符串，群聊可为其他类型） |

#### 系统消息（在线用户列表更新）

**接收示例：**
```json
{
  "isSystem": true,
  "fromId": null,
  "fromName": "系统",
  "data": [
    {"userId": 1, "nickname": "chen"},
    {"userId": 9, "nickname": "alice"}
  ]
}
```

**触发场景：** 用户上线/下线时自动广播

**字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| isSystem | Boolean | 始终为 true |
| data | Array | 当前所有在线用户列表，每个元素包含 userId 和 nickname |

---

## 使用示例（JavaScript）

```javascript
// 1. 建立连接
const userId = 1;
const username = 'chen';
const ws = new WebSocket(`ws://localhost:8080/app/chat?userId=${userId}&username=${username}`);

// 2. 监听连接打开
ws.onopen = () => {
  console.log('WebSocket 连接成功');
};

// 3. 监听消息
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  if (data.isSystem) {
    // 系统消息：更新在线用户列表
    console.log('在线用户:', data.data);
  } else {
    // 普通消息：显示聊天内容
    console.log(`[${data.fromName}] ${data.data}`);
  }
};

// 4. 发送消息
function sendMessage(toId, message) {
  const msg = { toId, message };
  ws.send(JSON.stringify(msg));
}

// 5. 监听连接关闭
ws.onclose = () => {
  console.log('WebSocket 连接已关闭');
};

// 6. 监听错误
ws.onerror = (error) => {
  console.error('WebSocket 错误:', error);
};
```

---

## 注意事项

1. **连接认证**：必须在 WebSocket 连接 URL 中携带 `userId` 和 `username` 查询参数
2. **消息回显**：发送消息后，服务端会自动返回一条自己的消息用于前端回显
3. **在线状态**：服务端会定期广播当前所有在线用户列表，前端可根据 `isSystem` 判断并更新 UI
