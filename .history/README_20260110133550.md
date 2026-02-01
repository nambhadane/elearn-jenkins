# 📘 E-Learning Hub – Full-Stack Learning Management System (LMS)

A complete digital learning platform providing **live classes, assignments, quizzes, messaging, materials management, attendance tracking**, and more for **Admins, Teachers, and Students**.

---

## 🚀 Project Overview

**E-Learning Hub (ElearnHub)** is a full-stack web-based Learning Management System (LMS) developed to modernize online education.  
The platform offers real-time communication, content delivery, evaluation tools, and role-based dashboards.

### 🎯 Purpose
To create a scalable, secure, and feature-rich LMS for online teaching & learning.

### 💡 Problems Solved
- Physical classroom limitations  
- No centralized learning platform  
- Need for digital education post-COVID  
- Lack of integrated assignments, quizzes, and live classes  

---

# 🧰 Technology Stack

## 🎨 Frontend
- **React.js**
- **TypeScript**
- **Vite**
- **Tailwind CSS**
- **Shadcn/UI**
- **React Router**

## ⚙️ Backend
- **Spring Boot**
- **Spring Security**
- **JWT Authentication**
- **Spring Data JPA**
- **MySQL Database**

## 🔗 Integrations
- **Jitsi Meet API** – Live video classes
- **Real-time notifications**
- **File upload system**

## 🏗 Architecture
- RESTful API  
- MVC + Repository Pattern  
- Microservices-ready modules  
- Layered architecture  

---

# 👥 User Roles & Features

---

## 👨‍💼 ADMIN FEATURES
### 🔹 Dashboard
- Total users, classes, assignments  
- Activity logs  
- System analytics  

### 🔹 User Management
- Add/edit/delete teachers & students  
- Role management  
- View all users  

### 🔹 Monitoring Tools
- View all classes & assignments  
- Track submissions  
- Generate reports  

---

## 👨‍🏫 TEACHER FEATURES
### 🔹 Class Management
- Create/manage classes  
- Add/remove students  
- Upload study materials  

### 🔹 Live Classes
- Schedule live sessions (Jitsi)  
- Moderator privileges  
- Screen share + chat  

### 🔹 Assignments
- Create assignments w/ files  
- View & grade submissions  
- Provide feedback  

### 🔹 Quiz System
- MCQ, True/False, Short Answers  
- Timers & deadlines  
- Auto-grading  
- Quiz analytics  

### 🔹 Attendance
- Create attendance sessions  
- Mark students  
- View reports  

### 🔹 Communication
- Class chat  
- Direct messages  
- Discussion forums  

---

## 👨‍🎓 STUDENT FEATURES
### 🔹 Dashboard
- All enrolled classes  
- Upcoming deadlines  
- Recent grades  

### 🔹 Learning Tools
- Join live classes  
- View/download materials  
- Submit assignments  
- Take quizzes  

### 🔹 Communication
- Group chat  
- Private chat with teacher  
- Discussions  

### 🔹 Attendance
- View records  
- Track percentages  
- History timeline  

### 🔹 Timetable
- Calendar view  
- Class reminders  

---

# 🗄️ Database Schema (Main Tables)

```
users
classes
class_student
assignments
submissions
quizzes
questions
question_options
quiz_attempts
student_answers
live_classes
attendance_sessions
attendance_records
materials
messages
conversations
conversation_participants
discussion_topics
discussion_replies
discussion_likes
notifications
user_settings
```

---

# 🔌 API Endpoints (Overview)

## 🔑 Authentication
```
POST /auth/login
POST /auth/register
POST /auth/logout
```

## 📚 Classes
```
GET /classes/teacher/{id}
GET /classes/{id}/students
POST /classes/{id}/students
DELETE /classes/{id}/students/{studentId}
```

## 📝 Assignments
```
POST /assignments
GET /assignments/class/{classId}
POST /assignments/{id}/submit
GET /assignments/{id}/submissions
PUT /assignments/submissions/{id}/grade
```

## ❓ Quizzes
```
POST /quizzes
GET /quizzes/class/{classId}
POST /quizzes/{id}/attempt
POST /quizzes/attempts/{id}/submit
```

## 🎥 Live Classes
```
POST /live-classes
GET /live-classes/class/{classId}
GET /live-classes/{id}/join
```

## 📅 Attendance
```
POST /attendance/sessions
POST /attendance/sessions/{id}/mark
GET /attendance/class/{classId}
```

## 💬 Messaging
```
GET /messages/conversations
POST /messages/send
POST /messages/conversations/direct
GET /messages/conversations/class/{classId}
```

## 🔔 Notifications
```
GET /notifications
GET /notifications/unread/count
PUT /notifications/{id}/read
```

---

# 🌟 Implementation Highlights

✔ JWT Authentication with custom filters  
✔ Secure role-based access (RBAC)  
✔ Real-time messaging + notifications  
✔ Jitsi live class integration  
✔ File upload/download system  
✔ Optimized database with indexing  
✔ Responsive UI with Tailwind + Shadcn  

---

# 🧩 Challenges & Solutions

| Challenge | Solution |
|----------|----------|
| JWT validation | Custom JwtFilter + token parsing |
| Many-to-many mapping | Proper JPA annotations + join table |
| File upload issues | Spring MultipartFile + secure file storage |
| LazyInitializationException | Correct fetch strategies & transactions |
| CORS errors | Spring Security CORS config |
| Conversation creation errors | Used Teacher object instead of teacherId |

---

# 🧪 Testing

### ✔ Manual Testing  
### ✔ API Testing (Postman)  
### ✔ Integration Testing  
### ✔ Live class test with Jitsi  

---

# 📈 Project Stats

- **15,000+ lines of code**  
- **100+ backend Java files**  
- **50+ React components**  
- **20+ database tables**  
- **80+ API endpoints**  
- **3 user roles**  
- **15+ major features**  

---

# 🚀 Future Enhancements

- Video class recording  
- Advanced analytics dashboards  
- Mobile apps (Android & iOS)  
- Email notifications  
- Calendar sync  
- Plagiarism detection  
- Peer review  
- Gamification  
- AI Chatbot  
- Multi-language support  

---

# ▶️ How to Run the Project

## 🖥 Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Create `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/elearnhub
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
jwt.secret=YOUR_SECRET_KEY
```

---

## 🌐 Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

---

# 🙌 Author

**Namrata Bhadane**  
Full-Stack Developer – React.js | Spring Boot | MySQL  

---

# 🏁 Conclusion

E-Learning Hub is a robust, scalable, and production-ready LMS designed with modern technologies.  
It solves real-world educational challenges and demonstrates strong full-stack development skills across:

- Frontend UI/UX  
- Backend REST APIs  
- Database design  
- Real-time systems  
- Security & authentication  
- Third-party integrations  

---

