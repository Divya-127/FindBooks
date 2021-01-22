package com.shahdivya.findbooks.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.shahdivya.findbooks.Constants;
import com.shahdivya.findbooks.R;
import com.shahdivya.findbooks.Upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private HomeViewModel mViewModel;
    ListView files;
    ArrayList<Upload> filenames = new ArrayList<Upload>();
    ArrayList<HashMap<String,String>> uploads = new ArrayList<>();
    DatabaseReference reference;
    SimpleAdapter simpleAdapter;
    SearchView searchView;
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        files =  requireView().findViewById(R.id.listOfFiles);
        searchView =requireView().findViewById(R.id.search);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        simpleAdapter = new SimpleAdapter(requireContext(),uploads,R.layout.row,new String[]{"Name:","Author:"},new int[]{R.id.text1,R.id.text2});
        files.setAdapter(simpleAdapter);
        files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Upload upload = filenames.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(upload.getUrl()));
                startActivity(intent);
            }
        });

        files.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Upload upload = filenames.get(position);
                String email = user.getEmail();
                assert email != null;
                if (email.equals(upload.getEmail())){
                    filenames.remove(position);
                    uploads.remove(position);
                    simpleAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(),upload.getNames(),Toast.LENGTH_LONG).show();
                    FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS).child(upload.getKey()).removeValue();
                    FirebaseStorage.getInstance().getReference(Constants.STORAGE_PATH_UPLOADS).child(upload.getNames()+".pdf").delete();
                }else {
                    Toast.makeText(requireContext(),"Not Allowed to delete",Toast.LENGTH_SHORT).show();
                }
                Log.i("Email",upload.getEmail());
                return true;
            }
        });

        reference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postsnapshot:snapshot.getChildren()){
                    Upload upload = postsnapshot.getValue(Upload.class);
                    filenames.add(upload);
                }
                for (int i=0;i<filenames.size();i++)
                {
                    HashMap<String,String> files = new HashMap<>();
                    files.put("Name:",filenames.get(i).getNames());
                    files.put("Author:","Author: "+filenames.get(i).getEmail());
                    uploads.add(files);
                }
                simpleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
        searchView.setQueryHint("Search Books!!");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                simpleAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
}