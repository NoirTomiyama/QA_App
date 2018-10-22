package jp.techacademy.tomiyama.ryota.qa_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

    private DatabaseReference mDataBaseReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mQuestionReference;

    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;

    private FavoritesListAdapter mAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser user;



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

        mAdapter.setQuestionArrayList(mQuestionArrayList);

        mListView.setAdapter(mAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

        mAdapter.notifyDataSetChanged();

    }

}
