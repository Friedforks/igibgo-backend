# Study Hive

This is the backend of Study Hive, written in `Java` and `SpringBoot3`. We aim to create a more **interconnected environment for studying** with the use of technologies.  

The website is at: [Study Hive](http://igibgo.cloud) (requires school email to login)

![image-20241123025510374](https://myoss-1303865496.cos.ap-shanghai.myqcloud.com/private-img/image-20241123025510374.png)

## Sections
Study Hive currently offers 4 main sections:
- User config
- Note
- Video
- Forum

## Database
The database uses `Postgresql` for persistent storage and `Redis` for caching.

## Docker

Every component in the project, including the databases, frontnd and backend is containerized through `docker`. It can be simply deployed with the `docker-compose.yml` files.

## Object Storage
The object storage is provided by [Tencent COS](https://cloud.tencent.com/product/cos).

## Installation
To get started with the backend, follow these steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/Friedforks/igibgo-backend
   cd study-hive-backend
2. Configure your `application-secret.yml` that includes the sensitive information, such as passwords or tokens.  
3. Initialize the database with `db_init.sql`
4. Install the dependencies:
   ```sh
   mvn install
   ```
5. Open in you IDE and run the Springboot project

