package jp.techacademy.tomiyama.ryota.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QuestionDetailListAdapter extends BaseAdapter {

    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;

    private Boolean mIsLogin; // ログインしているかどうか
    private Boolean mIsClick = false; // ハートボタンがクリックされたかどうか


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



            // いいねボタンの処理
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mIsClick){ // クリックされていたら
                        likeButton.setImageResource(R.drawable.heart_gray);
                        mIsClick = !mIsClick;
                        // いいねリストから削除
                    }else{
                        likeButton.setImageResource(R.drawable.heart_pink);
                        mIsClick = !mIsClick;
                        // アニメーション
                        Animation animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.like_touch);
                        likeButton.startAnimation(animation);
                        // いいねリストの保持
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
