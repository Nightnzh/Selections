package com.boardtek.selection;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.db.SelectionDao;
import com.boardtek.selection.db.SelectionRoomDatabase;

public class Repository {

    //Selection Database Access Object interface
    private SelectionDao selectionDao;

    public Repository(Application application){
        SelectionRoomDatabase selectionRoomDatabase = SelectionRoomDatabase.getDatabase(application);
        selectionDao = selectionRoomDatabase.selectionDao();
    }

//    public Selection getSelectionFromId(int id){
//        return selectionDao.getSelection(id);
//    }

    public void getSelectionFromId(int id, GetSelection getSelection){
        SelectionRoomDatabase.databaseWriteExecutor.execute(() -> {
            Selection selection = selectionDao.getSelection(id);
            getSelection.call(selection);
        });
//        return selectionDao.getSelection(id);
    }

    public interface GetSelection {
        public void call(Selection selection);
    }

}
