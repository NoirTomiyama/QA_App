package jp.techacademy.tomiyama.ryota.qa_app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesActivity extends AppCompatActivity {

    // userのfavoritesリストからquestionIdを取得
    // questionIdからcontentsに入り，照らし合わせる
    // questionListを取得できたら、adapterに渡してしまえば終了

    private DatabaseReference mDataBaseReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mQuestionReference;

    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
//    private ArrayList<String> mQuestionIdList;
    private FavoritesListAdapter mAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // ChildEventListener
    // NOTE：使わなくなった
    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            Log.d("ChildEventListener","ChildEventListener");

//            String question_id = (String) dataSnapshot.getKey();

            Log.d("dataSnapshot.getValue()",dataSnapshot.getKey());

            HashMap map = (HashMap) dataSnapshot.getValue();
            if(map==null) Log.d("null","null");

            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 1, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();

        }


        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

//            HashMap map = (HashMap) dataSnapshot.getValue();
//
//            // 変更があったQuestionを探す
//            for (Question question: mQuestionArrayList) {
//                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
//                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
//                    question.getAnswers().clear();
//                    HashMap answerMap = (HashMap) map.get("answers");
//                    if (answerMap != null) {
//                        for (Object key : answerMap.keySet()) {
//                            HashMap temp = (HashMap) answerMap.get((String) key);
//                            String answerBody = (String) temp.get("body");
//                            String answerName = (String) temp.get("name");
//                            String answerUid = (String) temp.get("uid");
//                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
//                            question.getAnswers().add(answer);
//                        }
//                    }
//
//                    mAdapter.notifyDataSetChanged();
//                }
//            }

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // ListViewの準備
        mListView = findViewById(R.id.listView);
        mAdapter = new FavoritesListAdapter(this);

        mQuestionArrayList = new ArrayList<>();
//        mQuestionIdList = new ArrayList<>();

        mAdapter.notifyDataSetChanged();

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        // ログイン時にしかここにこないが
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        setTitle("お気に入り");

//        mUserReference = mDataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);
        mUserReference = mDataBaseReference.child(Const.FavoritesPATH).child(user.getUid());

        // mUserReferenceがなければエラー？

        // リストにお気に入り質問リストを追加する
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap map = (HashMap) dataSnapshot.getValue();

                String user_id = (String) dataSnapshot.getKey();
                Log.d("user_id",user_id);
                Log.d("addValueEventListener1","addValueEventListener1");

                if(map!=null){
                    for(Object questionId : map.keySet()){
                        Log.d("questionId", (String) questionId);
                        HashMap map2 = (HashMap) dataSnapshot.child((String)questionId).getValue();
                        if(map2!=null){
                            Log.d("addValueEventListener2","addValueEventListener2");
                            Log.d("genre", String.valueOf(map2.get("genre")));

                            mQuestionReference = mDataBaseReference.child(Const.ContentsPATH).child((String.valueOf(map2.get("genre")))).child((String) questionId);

                            mQuestionReference.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Log.d("addValueEventListener3","addValueEventListener3");

//                                  String question_id = (String) dataSnapshot.getKey();

                                    Log.d("dataSnapshot.getKey()",dataSnapshot.getKey());

                                    HashMap map = (HashMap) dataSnapshot.getValue();
                                    if(map==null) Log.d("null","null");

                                    String title = (String) map.get("title");
                                    String body = (String) map.get("body");
                                    String name = (String) map.get("name");
                                    String uid = (String) map.get("uid");
                                    String imageString = (String) map.get("image");

                                    byte[] bytes;
                                    if (imageString != null) {
                                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                                    } else {
                                        bytes = new byte[0];
                                    }

                                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                                    HashMap answerMap = (HashMap) map.get("answers");
                                    if (answerMap != null) {
                                        for (Object key : answerMap.keySet()) {
                                            HashMap temp = (HashMap) answerMap.get((String) key);
                                            String answerBody = (String) temp.get("body");
                                            String answerName = (String) temp.get("name");
                                            String answerUid = (String) temp.get("uid");
                                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                            answerArrayList.add(answer);
                                        }
                                    }

                                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), 1, bytes, answerArrayList);
                                    mQuestionArrayList.add(question);
                                    mAdapter.notifyDataSetChanged();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        mListView.setAdapter(mAdapter);
        mAdapter.setQuestionArrayList(mQuestionArrayList);

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

}
