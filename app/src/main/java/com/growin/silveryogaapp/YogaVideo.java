package com.growin.silveryogaapp;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.growin.silveryogaapp.data.Content;
import com.growin.silveryogaapp.data.Favorit;

public class YogaVideo extends YouTubeBaseActivity {

    YouTubePlayerView playerView;
    Button playBtn;
    Button likeBtn;
    YouTubePlayer.OnInitializedListener listener;
    Content pItem;
    String pMail;

    private final FirebaseDatabase pDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference pDatabaseRef;
    private Query pQuery;

    private String pVideo;
    private String pImg;
    private String pName;
    private int stateFavorit;

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_video);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

        pMail = signInAccount.getEmail();
        pVideo = getIntent().getStringExtra("videoId");
        pImg = getIntent().getStringExtra("imgPath");
        pName = getIntent().getStringExtra("poseName");

        playBtn = findViewById(R.id.youtubeBtn);
        likeBtn = findViewById(R.id.likeBtn);
        playerView = findViewById(R.id.youtubeView);


        //Webview for checking pose accuracy
        mWebView = (WebView) findViewById(R.id.webView);



        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl("https://silversquat.netlify.app/"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작



        CheckFavoritVideo(pMail, pVideo, new IYogaVideo() {
            @Override
            public void onCallBack(Boolean bFavorit) {
                if (bFavorit) {
                    likeBtn.setSelected(true);
                    stateFavorit = 1;
                } else {
                    likeBtn.setSelected(false);
                    stateFavorit = 0;
                }
            }
        });

        listener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(pVideo);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playerView.initialize("아무키", listener);
//            }
//        });

        playerView.initialize("아무키", listener);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (stateFavorit == 0){
                    Favorit pFavor = new Favorit();
                    pFavor.setImg(pImg);
                    pFavor.setMail(pMail);
                    pFavor.setMail_video(pMail+"_"+pVideo);
                    pFavor.setName(pName);
                    pFavor.setVideo(pVideo);
                    pDatabaseRef = pDatabase.getReference("SilverYoga").child("Favorit");
                    pDatabaseRef.push().setValue(pFavor);

                    stateFavorit = 1;
                    likeBtn.setSelected(true);
                } else {
                    Favorit pFavor = new Favorit();
                    pFavor.setImg(pImg);
                    pFavor.setMail(pMail);
                    pFavor.setMail_video(pMail+"_"+pVideo);
                    pFavor.setName(pName);
                    pFavor.setVideo(pVideo);
                    pQuery = pDatabase.getReference("SilverYoga").child("Favorit").orderByChild("mail_video").equalTo(pMail+"_"+pVideo);
                    pQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ss: snapshot.getChildren()) {
                                ss.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    stateFavorit = 0;
                    likeBtn.setSelected(false);
                }
            }
        });
    }

    public void CheckFavoritVideo(String strMail, String strVideo, IYogaVideo iYogaVideo){

        pDatabaseRef = pDatabase.getReference("SilverYoga");
        pQuery = pDatabaseRef.child("Favorit").orderByChild("mail_video").equalTo(strMail+"_"+strVideo);

        pQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getChildrenCount()==1){
                    iYogaVideo.onCallBack(true);
                }
                else{
                    iYogaVideo.onCallBack(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //
            }
        });

    }
}