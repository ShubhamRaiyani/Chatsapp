# 📩 Chatsapp Backend

Chatsapp is a real-time messaging backend designed to make communication **fast, reliable, and secure**. It supports one-to-one and group conversations while keeping track of presence, sessions, and message status. With added features like **chat summaries**, it goes beyond messaging to make conversations more useful.

---
## 🔗 Live Demo
👉 [Try Chatsapp Backend Live](https://chatsapp.shubhamraiyani.com)  

---
## 🌟 Highlights
- ⚡ **Instant messaging** with WebSocket connections  
- 👀 **Presence tracking** using Redis (who’s online, active sessions)  
- 📬 **Message lifecycle** → sent, delivered, seen  
- 🗂️ **Persistent storage** of all chats and history in PostgreSQL  
- 📝 **Chat summaries** to quickly understand long conversations  
- 🔑 **Secure authentication** with JWT  
- 👥 **One-to-one and group chat support**  
- 🌍 **Scalable and cloud-ready** design  

---

## 🔎 Why Chatsapp Stands Out
- **Real-time Presence** → Know when your friends or teammates are online.  
- **Message Status** → Clear updates on sent, delivered, and seen messages.  
- **Summaries** → Long chats condensed into easy-to-read highlights.  
- **Persistence** → Conversations are stored safely for later access.  
- **Security** → Strong, token-based authentication protects user data.  

---

## 📡 How It Works (High Level)
1. Users log in securely with JWT.  
2. A WebSocket connection enables real-time chat.  
3. Redis tracks **presence** and **sessions** for accurate status updates.  
4. PostgreSQL stores messages, chat history, and supports **summaries**.  
5. The backend scales seamlessly to support more users and groups.  

---

## 🌍 Vision
The vision behind Chatsapp is simple: **to learn and explore modern backend technologies by building something practical and challenging.**  

Through this project, I aimed to gain hands-on experience with:  
- **Spring Boot & Java** → Building production-ready APIs  
- **WebSockets** → Real-time, bidirectional communication  
- **Redis** → Presence management and session tracking  
- **PostgreSQL** → Reliable data persistence  
- **JWT** → Secure user authentication  
- **Cloud & Docker** → Scalable deployments  

This project reflects my journey of learning by doing — applying theory into a real-world system.


