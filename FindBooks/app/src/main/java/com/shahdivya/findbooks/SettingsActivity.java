package com.shahdivya.findbooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //setTitleColor(R.color.colorPrimaryDark);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }
    public void passwdchange(View view){

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your Current Password");
        final EditText currPassword = new EditText(this);
        builder.setView(currPassword);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which)
            {
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().toString(),currPassword.getText().toString());
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            flag = 1;
                            Log.i("FLag",Integer.toString(flag));
                            dialog.cancel();
                            if (flag ==1){
                                builder.setTitle("Enter new Password");
                                final EditText newPassword = new EditText(getApplicationContext());
                                builder.setView(newPassword);
                                builder.setPositiveButton("Confirm??", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        user.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.i("Updated","Yes");
                                                    Toast.makeText(getApplicationContext(),"Your password is updated",Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Log.i("Updated","No");
                                                    Toast.makeText(getApplicationContext(),"Something went wrong....please try again later ",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                                builder.show();
                            }
                        }else {
                            dialog.cancel();
                            Log.i("Successful","No");
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void logout(View view){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                    startActivity(intent);
                    mAuth.signOut();
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingsActivity.this,BooksActivity.class));
        finish();
    }
}