package com.example.adstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentsActivity extends AppCompatActivity {


    private Toolbar commentToolbar;
    private EditText comment_field;
    private ImageView comment_post_btn;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String blog_post_id;
    private String current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
    }
}