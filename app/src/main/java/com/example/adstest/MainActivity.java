package com.example.adstest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainbottomNav;
    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseAuth firebaseAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    MobileAds mobileAds;
    public final static int spaceBetweenAds = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addPostBtn = findViewById(R.id.add_post_btn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });


        //this is where i have confusion to attacth the add nob adpater
        // can you please help me i can send you cleaner code without your method so you can put ads in the recycler sir
        //love your work
        //thanks in advance
        blog_list = new ArrayList<>();
        blog_list_view = findViewById(R.id.main_container);
        firebaseAuth = FirebaseAuth.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        loadMorePost();
                    }

                }
            });

            // i am doing pagination here to load data in batches firestore
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blog_list.clear();
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                if (isFirstPageFirstLoad) {
                                    blog_list.add(blogPost);
                                } else {
                                    blog_list.add(0, blogPost);
                                }

                                blogRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            });
        }
        // Then we will call method to add Native Express Ads to mDataset
        addNativeExpressAds();

    }


    private void addNativeExpressAds() {

        for (int i = spaceBetweenAds; i <= blog_list.size(); i += (spaceBetweenAds + 1)) {
            NativeExpressAdView adView = new NativeExpressAdView(this);
            // I have used a Test ID provided by Admob below
            // you should replace it with yours
            // And if wou are just experimenting, then just copy the code
            adView.setAdUnitId("ca-app-pub-3940256099942544/2793859312");
            blog_list.add(i, adView);
            //have a error here too
        }
    }





   blog_list_view.post(new Runnable(){
        @Override
        public void run () {
            float scale = MainActivity.this.getResources().getDisplayMetrics().density;
            int adWidth = (int) (blog_list_view.getWidth() - (2 * MainActivity.this.getResources().getDimension(R.dimen.activity_horizontal_margin)));

            // we are setting size of adView
            // you should check admob's site for possible ads size
            AdSize adSize = new AdSize((int) (adWidth / scale), 150);
            // looping over mDataset to sesize every Native Express Ad to ew adSize
            for (int i = spaceBetweenAds; i <= blog_list.size(); i += (spaceBetweenAds + 1)) {
                NativeExpressAdView adViewToSize = (NativeExpressAdView) blog_list.get(i);
                adViewToSize.setAdSize(adSize);
            }
            // calling method to load native ads in their views one by one
            loadNativeExpressAd(spaceBetweenAds);
        }
    });






    private void loadNativeExpressAd(final int index) {

        if (index >= blog_list.size()) {
            return;
        }

        Object item = blog_list.get(index);
        if (!(item instanceof NativeExpressAdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a Native"
                    + " Express ad.");
        }

        final NativeExpressAdView adView = (NativeExpressAdView) item;

        // Set an AdListener on the NativeExpressAdView to wait for the previous Native Express ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous Native Express ad loaded successfully, call this method again to
                // load the next ad in the items list.
                loadNativeExpressAd(index + spaceBetweenAds + 1);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("AdmobMainActivity", "The previous Native Express ad failed to load. Attempting to"
                        + " load the next Native Express ad in the items list.");
                loadNativeExpressAd(index + spaceBetweenAds + 1);
            }
        });

        // Load the Native Express ad.
        //We also registering our device as Test Device with addTestDevic("ID") method
        adView.loadAd(new AdRequest.Builder().addTestDevice("YOUR_TEST_DEVICE_ID").build());
    }





    public void loadMorePost(){
        if(firebaseAuth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);
            nextQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                blog_list.add(blogPost);

                                blogRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                }
            });

        }

    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();
        } else {
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_settings_btn:
                Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return false;


        }

    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }


}






