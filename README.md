SecureBank — Banking management desktop app

Structure:
- src/main/java/banking: application Java sources
- sql: database setup script
- lib: external libraries (place JDBC jar here)

Run:
1. Create MySQL database using `sql/database_setup.sql`.
2. Put MySQL connector jar in `lib/` and update DBConnection credentials.
3. Build and run with `javac`/`java` or import into an IDE.
