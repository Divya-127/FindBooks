package com.shahdivya.findbooks.ui.home;

import android.content.Intent;
import android.widget.ArrayAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.shahdivya.findbooks.BooksActivity;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    public ArrayAdapter<String> arrayAdapter;

    public HomeViewModel() {
    }

    public LiveData<String> getText() {
        return mText;
    }

}