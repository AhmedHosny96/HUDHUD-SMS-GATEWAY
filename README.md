HUDHUD SMS Gateway
HUDHUD SMS Gateway is a high-performance messaging system designed to handle more than 1 million messages per day. Built using Java Spring Boot and integrated with a Kannel server running on Linux, this project leverages an MSSQL database for robust data management.

Features
High Throughput: Capable of processing over 1 million messages daily.
Java Spring Boot: Utilizes Spring Boot for rapid development and production-ready applications.
MSSQL Database: Ensures reliable data storage and retrieval.
REST API Integration: Seamlessly integrates with Kannel server for efficient message routing.
Technology Stack
Backend: Java Spring Boot
Database: MSSQL
Message Routing: Kannel server (running on Linux)
API: RESTful API
Prerequisites
Java 11 or higher
Spring Boot 2.5.4 or higher
MSSQL Server
Kannel server (configured and running on a Linux machine)
Maven (for build automation)


Installation
Clone the Repository:

bash
Copy code
git clone https://github.com/yourusername/hudhud-sms-gateway.git
cd hudhud-sms-gateway
Configure the Database:

Set up an MSSQL database.
Update the application.properties file with your MSSQL database connection details.
properties
Copy code
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=hudhud_sms
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
Build the Project:

bash
Copy code
mvn clean install
Run the Application:

bash
Copy code
mvn spring-boot:run
Configuration
Ensure the Kannel server is configured correctly and running on your Linux machine. Update the Kannel server details in the application configuration.

API Endpoints
The HUDHUD SMS Gateway provides the following REST API endpoints:

Send SMS: /api/sms/send

Usage
Sending an SMS
To send an SMS, make a POST request to /api/sms/send with the following JSON payload:

json
Copy code
{
  "to": "+1234567890",
  "message": "Your message here"
}

Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.

License
This project is licensed under the MIT License - see the LICENSE file for details.

Acknowledgments
Special thanks to the developers of Spring Boot and Kannel for their incredible tools and documentation.
