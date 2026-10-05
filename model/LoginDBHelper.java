package com.example.cs360_projectthree.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

//importing PasswordUtils.java file for hashing passwords;
import com.example.cs360_projectthree.util.PasswordUtils;

public class LoginDBHelper extends SQLiteOpenHelper {

    // Naming the SQLite .db file
    public static final String DBNAME = "Login.db";
    // Setting the database version
    private static final int DATABASE_VERSION = 2;

    // initializing the database with the DBNAME and version #
    public LoginDBHelper(@Nullable Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
    }

    // This function is called when the database is first created
    // Creates the "users" table with username as primary key, password hash, and salt
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT, salt TEXT)");
    }

    // Called when the database version changes
    // Drops the existing table and recreates it
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // Inserts a new user into the database with PBKDF2-HMAC-SHA512 password hashing
    // Returns true if insertion is successful, false if insertion fails
    public boolean insertData(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Generate a random salt for the user
        byte[] salt = PasswordUtils.generateSalt();
        // Hash the password using PBKDF2-HMAC-SHA512 with the salt
        String hashedPasswordHex = PasswordUtils.hashPasswordHex(password, salt);
        String saltHex = PasswordUtils.bytesToHex(salt);

        // Store username, hashed password, and salt into the db as key-value pairs
        values.put("username", username);
        values.put("password", hashedPasswordHex);
        values.put("salt", saltHex);

        // Insert data into the "users" table
        long result = db.insert("users", null, values);

        // If result is -1, insertion failed
        return result != -1;
    }

    // Checks if a username already exists in the database
    // Returns true if the username is found. Otherwise, false
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Querying the database for matching username
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username = ?",
                new String[]{username}
        );

        // If cursor count > 0, the username exists
        boolean exists = cursor.getCount() > 0;

        // Closing the cursor
        cursor.close();

        return exists;
    }

    // -----LOGIN AUTHENTICATION-----
    // Verifies credentials using PBKDF2-HMAC-SHA512 hash, then compares it to the stored password hash and salt
    // Returns true if credentials are valid, otherwise false
    public boolean checkUsernamePassword(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Query database for stored hash and salt for the given username
        Cursor cursor = db.rawQuery(
                "SELECT password, salt FROM users WHERE username = ?",
                new String[]{username}
        );

        boolean isValid = false;

        if (cursor.moveToFirst()) {
            int passwordIndex = cursor.getColumnIndex("password");
            int saltIndex = cursor.getColumnIndex("salt");

            if (passwordIndex != -1 && saltIndex != -1) {
                String storedHashHex = cursor.getString(passwordIndex);
                String storedSaltHex = cursor.getString(saltIndex);

                // Verify the password using PBKDF2-HMAC-SHA512
                isValid = PasswordUtils.verifyPassword(password, storedHashHex, storedSaltHex);
            }
        }

        // Closing the cursor
        cursor.close();

        return isValid;
    }
}
