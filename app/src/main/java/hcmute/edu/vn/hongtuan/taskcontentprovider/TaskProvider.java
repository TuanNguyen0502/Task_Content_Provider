package hcmute.edu.vn.hongtuan.taskcontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

public class TaskProvider extends ContentProvider {
    private static final String AUTHORITY = "hcmute.edu.vn.hongtuan.taskcontentprovider";
    private static final String TABLE_NAME = "tasks";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    private static final int TASKS = 1;
    private static final int TASK_ID = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, TABLE_NAME, TASKS);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", TASK_ID);
    }
    private SQLiteDatabase database;
    public TaskProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int count;
        switch (uriMatcher.match(uri)) {
            case TASKS:
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                count = database.delete(TABLE_NAME, "id=?", new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (uriMatcher.match(uri)) {
            case TASKS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".tasks";
            case TASK_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".tasks";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        long rowId = database.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri rowUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(rowId));
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        TaskDatabaseHelper taskDatabaseHelper = new TaskDatabaseHelper(getContext());
        database = taskDatabaseHelper.getWritableDatabase();
        Log.d("TaskProvider", String.valueOf("onCreate: " + database != null));
        return true;
//        return database != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case TASKS:
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TASK_ID:
                String id = uri.getLastPathSegment();
                cursor = database.query(TABLE_NAME, projection, "id = ?", new String[]{id}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int count;
        switch (uriMatcher.match(uri)) {
            case TASKS:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case TASK_ID:
                count = database.update(TABLE_NAME, values, "id = ?", new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class TaskDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "tasks.db";
        private static final int DATABASE_VERSION = 1;
        public TaskDatabaseHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "due_time TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}