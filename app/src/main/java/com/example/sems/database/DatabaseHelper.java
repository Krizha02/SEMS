package com.example.sems.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.sems.models.User;
import com.example.sems.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParseException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SEMS.db";
    private static final int DATABASE_VERSION = 2;

    // Table name and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone_number";
    private static final String COLUMN_DEPARTMENT = "department";
    private static final String COLUMN_POSITION = "position";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_IS_ACTIVE = "is_active";

    // Add these constants to the existing DatabaseHelper class
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_EVENT_ID = "id";
    private static final String COLUMN_EVENT_TITLE = "title";
    private static final String COLUMN_EVENT_DESCRIPTION = "description";
    private static final String COLUMN_EVENT_START_DATE = "start_date";
    private static final String COLUMN_EVENT_END_DATE = "end_date";
    private static final String COLUMN_EVENT_LOCATION = "location";
    private static final String COLUMN_EVENT_ORGANIZER = "organizer";
    private static final String COLUMN_EVENT_IS_ACTIVE = "is_active";

    // Create table query
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_DEPARTMENT + " TEXT,"
            + COLUMN_POSITION + " TEXT,"
            + COLUMN_ROLE + " TEXT,"
            + COLUMN_IS_ACTIVE + " INTEGER"
            + ")";

    // Add this to the onCreate method
    private static final String CREATE_EVENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "("
            + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EVENT_TITLE + " TEXT NOT NULL,"
            + COLUMN_EVENT_DESCRIPTION + " TEXT NOT NULL,"
            + COLUMN_EVENT_START_DATE + " INTEGER NOT NULL,"
            + COLUMN_EVENT_END_DATE + " INTEGER NOT NULL,"
            + COLUMN_EVENT_LOCATION + " TEXT NOT NULL,"
            + COLUMN_EVENT_ORGANIZER + " TEXT NOT NULL,"
            + COLUMN_EVENT_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1,"
            + "FOREIGN KEY(" + COLUMN_EVENT_ORGANIZER + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ")"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Enable foreign key support
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (!db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error enabling foreign keys: " + e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("DatabaseHelper", "Creating database tables");
            
            // Enable foreign key support
            db.execSQL("PRAGMA foreign_keys=ON;");
            
            // Drop existing tables if they exist
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            // Create tables with error checking
            try {
                db.execSQL(CREATE_USERS_TABLE);
                Log.d("DatabaseHelper", "Users table created successfully");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error creating users table: " + e.getMessage(), e);
                throw e;
            }

            try {
                db.execSQL(CREATE_EVENTS_TABLE);
                Log.d("DatabaseHelper", "Events table created successfully");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error creating events table: " + e.getMessage(), e);
                throw e;
            }

            // Create default admin user
            try {
                createDefaultAdmin(db);
                Log.d("DatabaseHelper", "Default admin user created successfully");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error creating default admin: " + e.getMessage(), e);
                throw e;
            }

            Log.d("DatabaseHelper", "Database initialization completed successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating database: " + e.getMessage(), e);
            throw new RuntimeException("Database creation failed", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
            // Drop existing tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            // Recreate tables
            onCreate(db);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage(), e);
        }
    }

    private void createDefaultAdmin(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL, "admin@sems.com");
            values.put(COLUMN_PASSWORD, "admin123"); // In production, use proper password hashing
            values.put(COLUMN_NAME, "System Admin");
            values.put(COLUMN_ROLE, "admin");
            values.put(COLUMN_IS_ACTIVE, 1);
            
            long result = db.insert(TABLE_USERS, null, values);
            if (result == -1) {
                Log.e("DatabaseHelper", "Failed to create default admin user");
                throw new RuntimeException("Failed to create default admin user");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating default admin: " + e.getMessage(), e);
            throw e;
        }
    }

    // User Management Methods
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_PHONE, user.getPhoneNumber());
        values.put(COLUMN_DEPARTMENT, user.getDepartment());
        values.put(COLUMN_POSITION, user.getPosition());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);

        return db.insert(TABLE_USERS, null, values);
    }

    public User getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_EMAIL + "=?",
                new String[]{email}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                userList.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }

    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_NAME, user.getName());
        values.put(COLUMN_PHONE, user.getPhoneNumber());
        values.put(COLUMN_DEPARTMENT, user.getDepartment());
        values.put(COLUMN_POSITION, user.getPosition());
        values.put(COLUMN_ROLE, user.getRole());
        values.put(COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);

        return db.update(TABLE_USERS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(user.getId())});
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=? AND " + COLUMN_IS_ACTIVE + "=1",
                new String[]{email, password}, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    private User cursorToUser(Cursor cursor) {
        return new User(
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION)),
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1
        );
    }

    // Event Management Methods
    public long addEvent(Event event) {
        SQLiteDatabase db = null;
        long id = -1;
        
        try {
            db = this.getWritableDatabase();
            
            // Enable foreign keys
            db.execSQL("PRAGMA foreign_keys=ON");
            
            Log.d("DatabaseHelper", "Adding event: " + event.toString());

            // Validate event data
            if (!event.isValid()) {
                Log.e("DatabaseHelper", "Event validation failed");
                return -1;
            }

            // Verify organizer exists in users table
            String[] columns = new String[]{COLUMN_EMAIL};
            String selection = COLUMN_EMAIL + "=? AND " + COLUMN_IS_ACTIVE + "=1";
            String[] selectionArgs = new String[]{event.getOrganizer()};
            
            Log.d("DatabaseHelper", "Checking organizer existence: " + event.getOrganizer());
            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e("DatabaseHelper", "Organizer not found or inactive: " + event.getOrganizer());
                if (cursor != null) cursor.close();
                return -1;
            }
            cursor.close();
            Log.d("DatabaseHelper", "Organizer validation passed");

            ContentValues values = new ContentValues();
            values.put(COLUMN_EVENT_TITLE, event.getTitle());
            values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
            values.put(COLUMN_EVENT_START_DATE, event.getStartDate() != null ? event.getStartDate().getTime() : 0);
            values.put(COLUMN_EVENT_END_DATE, event.getEndDate() != null ? event.getEndDate().getTime() : 0);
            values.put(COLUMN_EVENT_LOCATION, event.getLocation());
            values.put(COLUMN_EVENT_ORGANIZER, event.getOrganizer());
            values.put(COLUMN_EVENT_IS_ACTIVE, event.isActive() ? 1 : 0);

            Log.d("DatabaseHelper", "Prepared values for database insert");

            // Begin transaction
            db.beginTransaction();
            try {
                id = db.insertOrThrow(TABLE_EVENTS, null, values);
                if (id != -1) {
                    db.setTransactionSuccessful();
                    Log.d("DatabaseHelper", "Event created successfully with ID: " + id);
                } else {
                    Log.e("DatabaseHelper", "Failed to insert event into database");
                }
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error during database insert: " + e.getMessage() + "\nStack trace: " + android.util.Log.getStackTraceString(e));
                id = -1;
            } finally {
                db.endTransaction();
            }
            
            return id;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding event: " + e.getMessage() + "\nStack trace: " + android.util.Log.getStackTraceString(e));
            return -1;
        }
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_EVENT_START_DATE + " ASC";
            
            cursor = db.rawQuery(selectQuery, null);
            Log.d("DatabaseHelper", "Found " + cursor.getCount() + " events in database");

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Event event = new Event(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)),
                            new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE))),
                            new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE))),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1
                        );
                        eventList.add(event);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error creating event from cursor: " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all events: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        Log.d("DatabaseHelper", "Returning " + eventList.size() + " events");
        return eventList;
    }

    public int updateEvent(Event event) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_EVENT_TITLE, event.getTitle());
            values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
            values.put(COLUMN_EVENT_START_DATE, event.getStartDate() != null ? event.getStartDate().getTime() : 0);
            values.put(COLUMN_EVENT_END_DATE, event.getEndDate() != null ? event.getEndDate().getTime() : 0);
            values.put(COLUMN_EVENT_LOCATION, event.getLocation());
            values.put(COLUMN_EVENT_ORGANIZER, event.getOrganizer());
            values.put(COLUMN_EVENT_IS_ACTIVE, event.isActive() ? 1 : 0);

            return db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + "=?",
                    new String[]{String.valueOf(event.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Add this method to the DatabaseHelper class
    public List<Event> getEventsByDate(Date date) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        String selectQuery = "SELECT * FROM " + TABLE_EVENTS 
                + " WHERE " + COLUMN_EVENT_START_DATE + " >= ? AND " 
                + COLUMN_EVENT_START_DATE + " < ?"
                + " ORDER BY " + COLUMN_EVENT_START_DATE + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, 
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)});

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE))),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE))),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1
                );
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventList;
    }

    public int getTotalEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EVENTS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getUpcomingEventsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long currentTime = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(currentTime)};
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_EVENTS + 
                " WHERE " + COLUMN_EVENT_START_DATE + " >= ?",
                selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Event> getRecentEvents(int limit) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_EVENTS +
                " ORDER BY " + COLUMN_EVENT_START_DATE + " DESC LIMIT " + limit;
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION));
                    Date startDate = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE)));
                    Date endDate = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE)));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION));
                    String organizer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER));
                    boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1;

                    Event event = new Event(id, title, description, startDate, endDate, location, organizer, isActive);
                    events.add(event);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error creating event from cursor: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public List<Event> getUpcomingEvents(Date fromDate) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_EVENTS 
                + " WHERE " + COLUMN_EVENT_START_DATE + " >= ? AND "
                + COLUMN_EVENT_IS_ACTIVE + " = 1"
                + " ORDER BY " + COLUMN_EVENT_START_DATE + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(fromDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE))),
                    new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE))),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1
                );
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventList;
    }

    public int getEventCountForDate(Date date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();

        String selection = COLUMN_EVENT_START_DATE + " >= ? AND " + COLUMN_EVENT_START_DATE + " <= ?";
        String[] selectionArgs = {
            String.valueOf(startOfDay.getTime()),
            String.valueOf(endOfDay.getTime())
        };

        Cursor cursor = db.query(TABLE_EVENTS, new String[]{"COUNT(*)"},
                selection, selectionArgs, null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // Add this method to fetch events by organizer
    public List<Event> getEventsByOrganizer(String organizerEmail) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_EVENTS
                    + " WHERE " + COLUMN_EVENT_ORGANIZER + " = ?"
                    + " ORDER BY " + COLUMN_EVENT_START_DATE + " ASC";

            cursor = db.rawQuery(selectQuery, new String[]{organizerEmail});
            Log.d("DatabaseHelper", "Found " + cursor.getCount() + " events for organizer " + organizerEmail);

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Event event = new Event(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION)),
                                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE))),
                                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE))),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION)),
                                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1
                        );
                        eventList.add(event);
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error creating event from cursor: " + e.getMessage(), e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting events by organizer: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d("DatabaseHelper", "Returning " + eventList.size() + " events for organizer " + organizerEmail);
        return eventList;
    }

    // Add this method to get total events for a specific organizer
    public int getTotalEventsByOrganizer(String organizerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EVENTS + " WHERE " + COLUMN_EVENT_ORGANIZER + " = ?", new String[]{organizerEmail});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Add this method to get upcoming events count for a specific organizer
    public int getUpcomingEventsCountByOrganizer(String organizerEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        long currentTime = System.currentTimeMillis();
        String[] selectionArgs = {organizerEmail, String.valueOf(currentTime)};
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_EVENTS + 
                " WHERE " + COLUMN_EVENT_ORGANIZER + " = ? AND " +
                COLUMN_EVENT_START_DATE + " >= ?",
                selectionArgs);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Add this method to get recent events for a specific organizer
    public List<Event> getRecentEventsByOrganizer(String organizerEmail, int limit) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_EVENTS +
                " WHERE " + COLUMN_EVENT_ORGANIZER + " = ?" +
                " ORDER BY " + COLUMN_EVENT_START_DATE + " DESC LIMIT " + limit;

        Cursor cursor = db.rawQuery(query, new String[]{organizerEmail});

        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DESCRIPTION));
                    Date startDate = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_START_DATE)));
                    Date endDate = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_END_DATE)));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_LOCATION));
                    String organizer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ORGANIZER));
                    boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_IS_ACTIVE)) == 1;

                    Event event = new Event(id, title, description, startDate, endDate, location, organizer, isActive);
                    events.add(event);
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Error creating event from cursor: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }
} 