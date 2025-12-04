
# 📌 Real-Time Chat Application

A full-stack real-time chat application supporting one-to-one and group messaging, AI-powered chat summaries, email-based user discovery, OAuth2 login, JWT cookie security, WebSocket/STOMP messaging, and Redis-based online presence tracking.

---
# 🚀 Features

### 🔐 Authentication
- Email + Password Login  
- Google OAuth2 Login  
- GitHub OAuth2 Login  
- Secure HttpOnly JWT Cookies  
- Email Verification Flow  

### 💬 Messaging
- One-to-one chat  
- Group chat with admin roles  
- Real-time messaging (STOMP WebSockets)  
- Delivery + Read receipts  
- Unread message count per chat  
- Pagination for message history  
- Redis-based online/offline tracking  
- Accurate SENT/DELIVERED/READ state per user  

### 🤖 AI Features
- Chat summary for last 48 hours (Cohere API)  
- Summaries stored in ChatSummaries table  

### 🧠 Advanced Chat Logic
- Prevent duplicate personal chats  
- Group creation with role-based membership  
- Leave group with system messages  
- Normalized DB schema  
- Composite MessageStatus entity per user  

### 🧩 Frontend Features
- React + Tailwind UI  
- Context API global state  
- Real-time WebSocket updates  
- Infinite scroll message history  
- Typing indicators (extendable)  
- Mobile responsive layout  

---

# 🏗️ Tech Stack

### Frontend
- React  
- Tailwind CSS  
- Vite  
- Context API  
- STOMP + SockJS  

### Backend
- Java 17  
- Spring Boot  
- Spring Security  
- OAuth2 Login  
- JWT Authentication  
- STOMP WebSocket  
- Redis (presence + subscription tracking)  
- Cohere Java SDK  

### Infrastructure
- PostgreSQL (Aiven)  
- Redis Cloud  
- Backend → Render  
- Frontend → Vercel  

---
