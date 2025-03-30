# Six Nations Rugby Blog Project

## Database Setup
Before running the project, ensure you create a database called **`rugbyblog_db`** in MySQL.

## Admin Setup
To assign admin privileges, first **register a user** on the website.  
Then, in MySQL, run the following command:

```sql
USE rugbyblog_db;
UPDATE users
SET role = 'admin'
WHERE username = 'your_registered_username';
hello to this project
