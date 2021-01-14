package com.growin.silveryogaapp;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
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

    private WebView myWebView; // 웹뷰 선언
    private WebSettings myWebSettings;

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

        //WEBVIEW 할일 정리
        //웹뷰를 다른 형식으로 만들어서 카메라 사용 가능하게 하기. (WebChromeClient 어떠한가.. stackoverflow 참조)
        //카메라 사용 가능해지면 모바일상 인식 확인하고 속도랑 효율 확인하기
        //효율까지 확인 되면 자세 하나 골라서 teachable machine에 입력하기
        //동작 인식 기능이 모바일에서 완벽하게 구현되면, 동영상이 플레이 되는 시간이나 순서에 맞게 인식하는 방법 찾기.
        //그리고 영상 순서에 맞게 재생하고 인식할 수 있게 된다면, 퍼센트로 나오는 대신 더 재밌는 문구 찾아보기. 그리고 화면 인식보다 모션 인식을 위로 올리기.

        //Webview for checking pose accuracy
        WebView myWebView = (WebView) findViewById(R.id.webView);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        myWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        myWebView.setWebViewClient(new WebViewClient());
        myWebView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                final String[] requestedResources = request.getResources();
                for (String r : requestedResources) {
                    if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                        break;
                    }
                }
            }

        });

        myWebView.loadUrl("https://silversquat.netlify.app/"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작



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