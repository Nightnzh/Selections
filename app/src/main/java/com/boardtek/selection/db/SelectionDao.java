package com.boardtek.selection.db;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.boardtek.selection.datamodel.Selection;
import com.boardtek.selection.datamodel.SelectionTest;

@Dao
public interface SelectionDao {

    @Query("select * from selection where :id = programId limit 1")
    Selection getSelection(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(Selection selection);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTestItem(SelectionTest selectiontest);

    @Query("delete from selection")
    void deleteAll();

    @Query("delete from SelectionTest")
    void deleteTestAll();

    @Query("select * from selectionTest where :id = programId limit 1")
    Selection getSelectionTest(int id);

}
