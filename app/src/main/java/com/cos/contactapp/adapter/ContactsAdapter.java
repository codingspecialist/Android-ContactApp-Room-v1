package com.cos.contactapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cos.contactapp.MainActivity;
import com.cos.contactapp.R;
import com.cos.contactapp.db.model.Contact;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private static final String TAG = "ContactsAdapter";
    private MainActivity mainActivity;
    private List<Contact> contacts;

    public ContactsAdapter(MainActivity mainActivity, List<Contact> contacts) {
        this.mainActivity = mainActivity;
        this.contacts = contacts;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvName;
        TextView tvEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: ");
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
        }

        void setItem(String name, String email){
            Log.d(TAG, "setItem: ");
            tvName.setText(name);
            tvEmail.setText(email);

        }
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, final int position) {
        // 컬렉션 증가 변화에만 반응함.
        final Contact contact = contacts.get(position);
        holder.setItem(contact.getName(), contact.getEmail());

        // 데이터 바인딩할 때 이벤트 달기
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.editContact(contact, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
