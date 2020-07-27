package com.example.adstest;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {


    //this is my adapter class where admob has to addded sir
    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private int spaceBetweenAds;
    private static final int DATA_VIEW_TYPE = 0;
    private static final int NATIVE_EXPRESS_AD_VIEW_TYPE = 1;



    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DATA_VIEW_TYPE:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_data_item_container, parent, false);
                context = parent.getContext();
                firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseAuth = FirebaseAuth.getInstance();
                return new ViewHolder(view);
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
                // fall through
            default:
                View nativeExpressLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_native_express_ad_container, viewGroup, false);
                return new NativeExpressAdViewHolder(nativeExpressLayoutView);
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case DATA_VIEW_TYPE:
                holder.setIsRecyclable(false);
                final String blogPostId = blog_list.get(position).BlogPostId;
                final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                String desc_data = blog_list.get(position).getDesc();
                holder.setDescText(desc_data);
                String image_url = blog_list.get(position).getImage_url();
                String thumbUri = blog_list.get(position).getImage_thumb();
                holder.setBlogImage(image_url, thumbUri);
                String user_id = blog_list.get(position).getUser_id();
                //User Data will be retrieved here...
                firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            String userName = task.getResult().getString("name");
                            String userImage = task.getResult().getString("image");
                            holder.setUserData(userName, userImage);
                        } else {
                            //Firebase Exception
                        }
                    }
                });
                try {
                    long millisecond = blog_list.get(position).getTimestamp().getTime();
                    String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
                    holder.setTime(dateString);
                } catch (Exception e) {

                    Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //Get Likes Count
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener( new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if(!documentSnapshots.isEmpty()){
                            int count = documentSnapshots.size();
                            holder.updateLikesCount(count);
                        } else {
                            holder.updateLikesCount(0);
                        }
                    }
                });

                //Get Likes
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_gray));
                        } else {
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_accent));
                        }
                    }
                });

                //Likes Feature
                holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                                } else {
                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                                }
                            }
                        });
                    }
                });

                holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(context, CommentsActivity.class);
                        commentIntent.putExtra("blog_post_id", blogPostId);
                        context.startActivity(commentIntent);
                    }
                });

                break;
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
                // fall through
            default:
                //i have few errors here
                NativeExpressAdViewHolder nativeExpressHolder = (NativeExpressAdViewHolder) holder;
                NativeExpressAdView adView = (NativeExpressAdView) blog_list.get(position);
                ViewGroup adCardView = (ViewGroup) nativeExpressHolder.itemView;
                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                adCardView.addView(adView);
        }

    }




    public class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {
        NativeExpressAdViewHolder(View view) {
            super(view);
        }
    }






    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
        }

        public void setDescText(String descText){
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUri){
            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.photo);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);

        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image){
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.photo);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
        }

        public void updateLikesCount(int count){
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");

        }

    }




}
