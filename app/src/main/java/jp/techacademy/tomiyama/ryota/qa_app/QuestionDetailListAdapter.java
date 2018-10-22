package jp.techacademy.tomiyama.ryota.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailListAdapter extends BaseAdapter {

    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;

    private Boolean mIsLogin; // ログインしているかどうか
    private Boolean mIsClick;// ハートボタンがクリックされたかどうか


    public QuestionDetailListAdapter(Context context, Question question,Boolean isLogin) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;
        mIsLogin = isLogin;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }


            String title = mQustion.getTitle();
            String body = mQustion.getBody();
            String name = mQustion.getName();

            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            titleTextView.setText(title);

            TextView bodyTextView = convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

            final ImageView likeButton = convertView.findViewById(R.id.like_button);

            if (mIsLogin){
                // ハートの表示
                likeButton.setVisibility(View.VISIBLE);
            }else{
                // ハートの非表示
                likeButton.setVisibility(View.INVISIBLE);
            }

            // TODO タイトルとお気に入りの数の取得
            // お気に入り数は一旦保留

            // TODO getViewしてきた際に，いいね済みかどうか判定
            // userのfavoritesのなかに，今のQuestionのidがあるかどうか判断
            // いいね済みならmIsClick変数をtrueにしないといけない

            mIsClick = false;

            // いいね済みかどうか判断
            // 変更した表示名をPreferenceに保存する
//            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(convertView.getContext());
//            mIsClick = sp.getBoolean(mQustion.getUid(), false);

            // firebaseの初期化
            DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            // FirebaseAuthのオブジェクトを取得する
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            // ログインしているユーザーの取得
            FirebaseUser user = mAuth.getCurrentUser();

            DatabaseReference userRef = null;

            if(user != null){
//                userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH).child(mQustion.getQuestionUid());
                userRef = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQustion.getQuestionUid());

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap map = (HashMap) dataSnapshot.getValue();
                        if(map != null) {
                            Log.d("genre",String.valueOf(map.get("genre")));
                            mIsClick = true;
                            likeButton.setImageResource(R.drawable.heart_pink);

                        }else{
                            Log.d("genre","なにもありません");
                            mIsClick = false;
                            likeButton.setImageResource(R.drawable.heart_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

//            // 実験
//            DatabaseReference userRef2 = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);
//
//            userRef2.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    HashMap map = (HashMap) dataSnapshot.getValue();
//
//                    for (Object str : map.keySet()) {
//                        System.out.println(str + ":" + map.get(str));
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });


//            if(mIsClick){
//                likeButton.setImageResource(R.drawable.heart_pink);
//            }else{
//                likeButton.setImageResource(R.drawable.heart_gray);
//            }

            // いいねボタンの処理
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // ログインしている場合しかいいねボタンは出てこない
                    // firebaseの初期化
                    DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();
                    // FirebaseAuthのオブジェクトを取得する
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    // ログインしているユーザーの取得
                    FirebaseUser user = mAuth.getCurrentUser();


                    if(mIsClick){ // クリックされていたら
                        likeButton.setImageResource(R.drawable.heart_gray);
                        mIsClick = !mIsClick;
                        // TODO いいねリストから削除

//                        DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH).child(mQustion.getQuestionUid());
                        DatabaseReference userRef = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQustion.getQuestionUid());

                        userRef.removeValue();

//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putBoolean(mQustion.getUid(),false);
//                        editor.commit();


                    }else{
                        likeButton.setImageResource(R.drawable.heart_pink);
                        mIsClick = !mIsClick;
                        // アニメーション
                        Animation animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.like_touch);
                        likeButton.startAnimation(animation);

                        // TODO いいねリストの登録
//                        DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH).child(mQustion.getQuestionUid());
                        DatabaseReference userRef = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQustion.getQuestionUid());

                        Map<String, Integer> data = new HashMap<String, Integer>();
                        data.put("genre", mQustion.getGenre());

                        userRef.setValue(data);

//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putBoolean(mQustion.getUid(),true);
//                        editor.commit();

                    }
                }
            });


        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}
