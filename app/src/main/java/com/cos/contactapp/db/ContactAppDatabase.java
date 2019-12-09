package com.cos.contactapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.cos.contactapp.db.model.Contact;

@Database(entities = {Contact.class}, version = 1)
public abstract class ContactAppDatabase extends RoomDatabase {

    // @Database에서 DAO객체에 접근 (규칙)
    public abstract ContactDAO getContactDAO();

}
