package com.boardtek.selection.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.datamodel.SelectionTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Selection.class, SelectionTest.class} , version = 2,exportSchema = false)
public abstract class SelectionRoomDatabase extends RoomDatabase {

    //Dao
    public abstract SelectionDao dbDao();

    //單一實例
    private static volatile SelectionRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d("SelectionRoomDatabase:","OPEN");
        }
    };

    public static SelectionRoomDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (SelectionRoomDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context,SelectionRoomDatabase.class,"SelectionDatabase")
                            .addCallback(sRoomDatabaseCallback)
//                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
