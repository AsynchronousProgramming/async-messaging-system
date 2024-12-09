# Asynchronous Event-Driven Messaging System

Asynchronous Event-Driven Messaging System is about to allow multiple producers to send messages asynchronously to consumers via topics or queues.

## Los Urubos Team:

- Fabian Romero
- José Luis Terán

---

## Message Broker Endpoints

---

## Base URL

```
http://localhost:<PORT>
```

Replace `<PORT>` with the port number on which the `MessageBroker` server is running.

### 1. Create Topic
**Endpoint:**  
`POST /createTopic`

**Description:**  
Creates a new topic in the message broker. Topics are case-insensitive.

**Request Body (JSON):**
```json
{
  "topic": "news"
}
```

**Response:**
- **200 OK**: Topic successfully created.
- **400 Bad Request**: If the JSON payload is invalid.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```json
{
  "topic": "news"
}
```

**Response:**
```json
{
  "message": "Topic created successfully"
}
```

---

### 2. Publish Message
**Endpoint:**  
`POST /publish`

**Description:**  
Publishes a message to a specified topic. If no subscribers are present, the message will not be delivered.

**Request Body (JSON):**
```json
{
  "topic": "news",
  "message": "Today's top headline is..."
}
```

**Response:**
- **200 OK**: Message successfully published.
- **400 Bad Request**: If the JSON payload is invalid.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```json
{
  "topic": "news",
  "message": "Today's top headline is..."
}
```

**Response:**
```json
{
  "message": "Message published successfully"
}
```

---

### 3. Subscribe to a Topic
**Endpoint:**  
`POST /subscribe`

**Description:**  
Subscribes a consumer to a specific topic. The consumer URL will receive messages for the subscribed topic.

**Request Body (JSON):**
```json
{
  "topic": "news",
  "consumerUrl": "http://localhost:9000"
}
```

**Response:**
- **200 OK**: Subscription successfully added.
- **400 Bad Request**: If the JSON payload is invalid.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```json
{
  "topic": "news",
  "consumerUrl": "http://localhost:9000"
}
```

**Response:**
```json
{
  "message": "Subscription added successfully"
}
```
