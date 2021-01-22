package com.shahdivya.findbooks.addBooks;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shahdivya.findbooks.Constants;
import com.shahdivya.findbooks.R;
import com.shahdivya.findbooks.Upload;

import java.util.Objects;

public class addbooks extends Fragment {

    private AddbooksViewModel mViewModel;
    TextView notification;
    Button select;
    Uri pdfuri;
    ProgressDialog progressDialog;
    FirebaseStorage storage;
    FirebaseDatabase database;
    String downloadUrl;
    FirebaseAuth mAuth;
    String fileName;
    //EditText description;
    //String desp;
    public static addbooks newInstance() {
        return new addbooks();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.addbooks_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AddbooksViewModel.class);
        // TODO: Use the ViewModel
        notification  = requireView().findViewById(R.id.text);
       // description = requireView().findViewById(R.id.description);
        select = requireView().findViewById(R.id.select);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (select.getText().toString().compareTo("SELECT FILE")==0)
                {
                    select.setText("Upload");
                    if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                        selectPdf();
                        AlertDialog.Builder builder  = new AlertDialog.Builder(requireContext());
                        builder.setTitle("Save as...");
                        final EditText namefile = new EditText(requireContext());
                        builder.setView(namefile);
                        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fileName = namefile.getText().toString();
                                notification.setText(fileName);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                pdfuri = null;
                                dialog.cancel();
                                select.setText("SELECT FILE");
                            }
                        });
                        builder.show();
                        //desp = description.getText().toString();
                    }else {
                        ActivityCompat.requestPermissions((Activity) requireContext(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},12);
                    }
                }else if (select.getText().toString().compareTo("Upload")==0){
                    if (pdfuri!=null)
                    {
                        select.setText("SELECT FILE");
                        uploadFile(pdfuri);
                    }
                    else{
                        select.setText("SELECT FILE");
                        notification.setText("No File Selected Currently");
                        Toast.makeText(requireContext(),"Select A File",Toast.LENGTH_SHORT).show();
                    }
                }
                }
        });
    }

    private void uploadFile(Uri pdfuri)
    {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading the file.....");
        progressDialog.setProgress(0);
        progressDialog.show();
        final StorageReference  uploadTask= storage.getReference().child(Constants.STORAGE_PATH_UPLOADS+fileName+".pdf");
        UploadTask uploadTask2 = uploadTask.putFile(pdfuri);
        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUrl = "";
                uploadTask.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri downloaduri = task.getResult();
                        assert downloaduri != null;
                        downloadUrl = downloaduri.toString();
                        DatabaseReference reference = database.getReference(Constants.DATABASE_PATH_UPLOADS);
                        String key =reference.push().getKey();
                        //reference.child("index").setValue(index);
                        Upload upload = new Upload(fileName,downloadUrl,mAuth.getCurrentUser().getEmail().toString(),key);
                        assert key != null;
                        reference.child(key).setValue(upload).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    notification.setText(R.string.no_file_selected_currently);
                                    Toast.makeText(requireContext(),"File Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }else {
                                    Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    Log.i("Divya Error",task.getException().getMessage());
                                    progressDialog.cancel();
                                }
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.i("Divya",pdfuri.toString());
                Toast.makeText(requireContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int)(100 *taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==12 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPdf();
        }else {
            ActivityCompat.requestPermissions((Activity) requireContext(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},12);
        }
    }

    private void selectPdf()
    {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,26);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==26 && resultCode==Activity.RESULT_OK && data!=null){
            pdfuri = data.getData();
            Log.i("Divya",pdfuri.toString());
        }else {
            Toast.makeText(requireContext(),"Select A File",Toast.LENGTH_SHORT).show();
        }
    }

}