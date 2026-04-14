# 💳 PayPal Clone – Production-Grade Full Stack System

> A **production-ready PayPal-inspired payment system** built with **React, Spring Boot Microservices, Kafka, and API Gateway**. Designed to demonstrate real-world architecture, scalability, and clean engineering practices.

---

## 🚀 Why This Project Stands Out

This is not just another CRUD project.

✔ Microservices Architecture (Industry Standard)
✔ Event-Driven Design using Kafka
✔ Secure Authentication (JWT + RBAC)
✔ Real-world Wallet & Transaction Logic
✔ Idempotency & Failure Handling
✔ API Gateway with Rate Limiting
✔ Clean UI inspired by modern fintech apps

---

## 🧠 System Architecture

* **Frontend:** React (Modern UI, Dashboard, Auth Flow)
* **Backend:** Spring Boot Microservices
* **Messaging:** Apache Kafka (Event-driven communication)
* **Security:** JWT Authentication + Role-Based Access Control
* **Gateway:** API Gateway with Rate Limiting
* **Database:** (H2 Database)

---

## 🧩 Microservices Breakdown

### 🔐 User Service

* User registration & login
* JWT-based authentication
* Role-based access control

### 💰 Wallet Service

* Add money / withdraw
* Balance management
* Idempotency handling (prevents duplicate transactions)

### 💸 Transaction Service

* Handles money transfers
* Publishes events to Kafka

### 🔔 Notification Service

* Kafka consumers
* Sends transaction updates

### 🎁 Reward System

* Real-time rewards based on transactions

### 🌐 API Gateway

* Central entry point
* Rate limiting
* Routing to microservices

---

## ✨ Features

* 🔐 Secure Login & Signup
* 📊 Interactive Dashboard
* 💳 Add Money to Wallet
* 🔁 Real-time Transactions
* ⚡ Kafka-based Event Processing
* 🛡 Fault Tolerance & Retry Logic
* 📉 Rate Limiting for APIs

---

## ⚙️ Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/sahilsanap101/Payflow-Payment-Wallet-System.git
cd paypal-clone
```

### 2️⃣ Run Backend Services

```bash
# Start each microservice
mvn spring-boot:run
```

### 3️⃣ Start Kafka

```bash
# Start Zookeeper & Kafka
```

### 4️⃣ Run Frontend

```bash
cd frontend
npm install
npm start
```

---

## 🔑 Key Concepts Demonstrated

* Microservices Communication
* Event-Driven Architecture
* Distributed System Design
* Idempotency in Payments
* API Gateway Patterns
* Rate Limiting
* Secure Authentication

---

## 📚 Learning Highlights

This project covers:

* How real fintech apps work
* Backend system design for interviews
* Production-level engineering practices

---

## 🧑‍💻 Author

**@sahilsanap101**

* Passionate about Backend & System Design
* Building real-world scalable systems


---

🔥 *This is not just a project. This is a SYSTEM.*
