package com.cos.contactapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cos.contactapp.adapter.ContactsAdapter;
import com.cos.contactapp.db.ContactAppDatabase;
import com.cos.contactapp.db.ContactDAO;
import com.cos.contactapp.db.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainLog";
    private ContactAppDatabase contactAppDatabase;
    private ContactDAO contactDAO;
    private List<Contact> contacts = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

    private Toolbar toolbar;
    private FloatingActionButton fab;

    // 사진 업로드
    private CircleImageView ivProfile;
    private File tempFile;
    private String imageRealPath;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        tedPermission();
    }

    private void init() {
        // 메인쓰레드에서 쿼리 하는 것을 허용(추천하지 않음.기본 예제여서 이렇게 함)
        contactAppDatabase = Room.databaseBuilder(getApplicationContext(), ContactAppDatabase.class, "ContactDB").allowMainThreadQueries().build();
        contactDAO = contactAppDatabase.getContactDAO();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Contact App");

        adapter = new ContactsAdapter(this, contacts);

        recyclerView = findViewById(R.id.recycler_view_contacts);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        fab = findViewById(R.id.fab);
        findAll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        long id = item.getItemId();
        if (id == R.id.action_deletes) {
            // 전체삭제
            deleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createContact(String name, String email) {
        long id = contactDAO.addContact(new Contact(name, email));
        Log.d(TAG, "createContact: id : " + id);

        Contact contact = contactDAO.getContact(id);
        if (contact != null) {
            // List에 추가
            contacts.add(contact);
            Log.d(TAG, "createContact: 컬렉션에 추가되면 리사이클러뷰 반응함?");
            // 어댑터에 알려주기 notify
            adapter.notifyDataSetChanged();
        }
    }

    private void updateContact(String name, String email, int position) {
        Contact contact = contacts.get(position);
        contact.setName(name);
        contact.setEmail(email);

        contactDAO.updateContact(contact);
        adapter.notifyDataSetChanged();
    }

    private void deleteContact(Contact contact, int position) {
        contacts.remove(position);
        contactDAO.deleteContact(contact);
        adapter.notifyDataSetChanged();
    }

    private void deleteAll() {
        contacts.removeAll(contacts);
        contactDAO.deleteAll();
        adapter.notifyDataSetChanged();
    }

    private void findAll() {
        contacts.addAll(contactDAO.getContacts());
        adapter.notifyDataSetChanged();
    }

    public void editContact(final Contact contact, final int position) {
        View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_add_contact, null);

        final EditText etName = dialogView.findViewById(R.id.name);
        final EditText etEmail = dialogView.findViewById(R.id.email);

        if (contact != null) {
            etName.setText(contact.getName());
            etEmail.setText(contact.getEmail());
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        //dlg.setCancelable(false);
        dlg.setTitle("연락처 수정");
        dlg.setView(dialogView);
        dlg.setPositiveButton("수정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateContact(etName.getText().toString(), etEmail.getText().toString(), position);
            }
        });
        dlg.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact(contact, position);
            }
        });
        dlg.show();

    }

    public void addContact() {
        View dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_add_contact, null);

        final EditText etName = dialogView.findViewById(R.id.name);
        final EditText etEmail = dialogView.findViewById(R.id.email);

        // 갤러리 사진 가져오기
        ivProfile = dialogView.findViewById(R.id.iv_profile);
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("연락처 등록");
        dlg.setView(dialogView);
        dlg.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createContact(etName.getText().toString(), etEmail.getText().toString());
            }
        });
        dlg.setNegativeButton("닫기", null);
        dlg.show();

    }

    // ------------- 아래는 사진 업로드를 위한 추가 코드 ---------------------
    // https://dd00oo.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EA%B0%A4%EB%9F%AC%EB%A6%AC%EC%9D%98-%EC%8B%A4%EC%A0%9C%EA%B2%BD%EB%A1%9C-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0

    // 권한 관련
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    // 앨범으로 이동
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // 이미지 채우기
    private void setImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        ivProfile.setImageBitmap(originalBm);
    }

    // URI로 이미지 실제 경로 가져오기
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // 이미지 선택 후 이미지 채우기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM) {
            Uri photoUri = data.getData();
            imageRealPath = getRealPathFromURI(photoUri);
            tempFile = new File(imageRealPath);
            setImage();
        }
    }
}

/**
 * notifyDataSetChanged : 데이터가 전체 바뀌었을 때 호출. 즉, 처음 부터 끝까지 전부 바뀌었을 경우. (전체삭제)
 * notifyItemChanged : 특정 Position의 위치만 바뀌었을 경우. position 4 번 위치만 데이터가 바뀌었을 경우 사용 하면 된다. (수정)
 * notifyItemRangeChanged : 특정 영역을 데이터가 바뀌었을 경우. position 3~10번까지의 데이터만 바뀌었을 경우 사용 하면 된다.
 * notifyItemInserted : 특정 Position에 데이터 하나를 추가 하였을 경우. position 3번과 4번 사이에 넣고자 할경우 4를 넣으면 되겠죠.
 * notifyItemRangeInserted : 특정 영역에 데이터를 추가할 경우. position 3~10번 자리에 7개의 새로운 데이터를 넣을 경우
 * notifyItemRemoved : 특정 Position에 데이터를 하나 제거할 경우. (삭제)
 * notifyItemRangeRemoved : 특정 영역의 데이터를 제거할 경우
 * notifyItemMoved : 특정 위치를 교환할 경우 (Drag and drop에 쓰이겠네요^^)
 */