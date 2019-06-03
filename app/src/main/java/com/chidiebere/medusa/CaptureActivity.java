package com.chidiebere.medusa;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chidiebere.medusa.constants.Constants;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;


public class CaptureActivity extends AppCompatActivity {

    SurfaceView mCameraView;
    View card;
    CardProcessor cardProcessor;
    Typeface typeface;
    TextView captureCardTextView;
    CameraSource mCameraSource;
    TextView cardNumberTextView;
    TextView cardExpTextView;
    TextView cardNameTextView;
    LinearLayout manualEditButton;

    private static final int requestPermissionID = 101;
    public static final String TAG = "CARD PROCESSOR";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraSource.release();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mCameraView = findViewById(R.id.camera);
        card = findViewById(R.id.card);
        cardNumberTextView = findViewById(R.id.card_num);
        cardExpTextView = findViewById(R.id.card_exp);
        cardNameTextView = findViewById(R.id.card_name);
        manualEditButton = findViewById(R.id.manual_edit_button);
        //card.setDrawingCacheEnabled(true);
        cardProcessor = new CardProcessor(this);

        final String fontPath = "fonts/Roboto-Bold.ttf";
        // Loading Font Face
        typeface = Typeface.createFromAsset(getAssets(), fontPath);
        captureCardTextView = findViewById(R.id.capture_card_view);
        captureCardTextView.setTypeface(typeface);
        startCameraSource();

        manualEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaptureActivity.this, ManualEditActivity.class);
                intent.putExtra(Constants.IS_MANUAL_EDIT, true);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void vibrate(){
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 80, 100, 80, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);
        //mCameraSource.stop();
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(15.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(CaptureActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> cardTextBlock = detections.getDetectedItems();

                    //check if the card is already scanned, if not then scan
                    if (!cardProcessor.isScanned()) {
                        cardProcessor.processCard(cardTextBlock);
                    }

                    cardNumberTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cardProcessor.isScanned()) {
                                cardNumberTextView.setText(cardProcessor.getCardNumber());
                                vibrate();
                            }

                        }
                    });

                    cardExpTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cardProcessor.isScanned()) {
                                cardExpTextView.setText(cardProcessor.getExpDate());
                            }
                        }
                    });

                    cardNameTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cardProcessor.isScanned()) {
                                cardNameTextView.setText(cardProcessor.getCardName());
                            }
                        }
                    });

                    if (cardProcessor.isScanned()){
                        Intent intent = new Intent(CaptureActivity.this, ManualEditActivity.class);
                        intent.putExtra(Constants.CARD_NUMBER, cardProcessor.getCardNumber());
                        intent.putExtra(Constants.CARD_EXP_DATE, cardProcessor.getExpDate());
                        intent.putExtra(Constants.CARD_NAME, cardProcessor.getCardName());
                        intent.putExtra(Constants.IS_MANUAL_EDIT, false);
                        textRecognizer.release();
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }
}
