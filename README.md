# Asynchronous Event-Driven Messaging System

Asynchronous Event-Driven Messaging System is about to allow multiple producers to send messages asynchronously to consumers via topics or queues.

## Los Urubos Team:

- Fabian Romero
- José Luis Terán

Here’s a detailed documentation for the endpoints to include in your README file:



## Endpoints

---

## Message Broker

---

## Base URL

```
http://localhost:<PORT>
```

Replace `<PORT>` with the port number on which the `MessageBroker` server is running.

---

### 1. Create Topic
**Endpoint:**  
`POST /createTopic`

**Description:**  
Creates a new topic in the message broker. Topics are case-insensitive.

**Request Body:**
- Plain text containing the name of the topic.

**Response:**
- **200 OK**: Topic successfully created.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```plaintext
news
```

**Response:**
```plaintext
Topic created successfully
```

---

### 2. Publish Message
**Endpoint:**  
`POST /publish`

**Description:**  
Publishes a message to a specified topic. If no subscribers are present, the message will not be delivered.

**Request Body:**
- A comma-separated string containing:
    - Topic name
    - Message content

**Response:**
- **200 OK**: Message successfully published.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```plaintext
news,Today's top headline is...
```

**Response:**
```plaintext
Message published successfully
```

---

### 3. Subscribe to a Topic
**Endpoint:**  
`POST /subscribe`

**Description:**  
Subscribes a consumer to a specific topic. The consumer URL will receive messages for the subscribed topic.

**Request Body:**
- A comma-separated string containing:
    - Topic name
    - Consumer URL

**Response:**
- **200 OK**: Subscription successfully added.
- **405 Method Not Allowed**: If a method other than `POST` is used.

**Example:**  
**Request:**
```plaintext
news,http://localhost:9000
```

**Response:**
```plaintext
Subscription added successfully
```

---
