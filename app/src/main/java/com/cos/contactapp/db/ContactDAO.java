package com.cos.contactapp.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.cos.contactapp.db.model.Contact;

import java.util.List;

@Dao
public interface ContactDAO {

    // 삽입된 행의 Primary Key값을 long 형으로 return 해준다.
    // 삽입된 행이 여러건일 경우 List<Long> 형으로 return 해준다.
    @Insert
    long addContact(Contact contact);

    // 수정된 행의 개수를 int 형으로 return 해준다.
    @Update
    int updateContact(Contact contact);

    // 삭제된 행의 개수를 int 형으로 return 해준다.
    @Delete
    int deleteContact(Contact contact);

    @Query("DELETE FROM contacts")
    int deleteAll();

    @Query("SELECT * FROM contacts")
    List<Contact> getContacts();

    @Query("SELECT * FROM contacts WHERE contact_id = :contactId ")
    Contact getContact(long contactId);
}
