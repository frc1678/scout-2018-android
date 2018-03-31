package com.example.evan.scout;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

/**
 * Created by sam on 3/27/18.
 */
public class QRScan extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    QRCodeReaderView qrCodeReader;
    public static String qrString = "NULL";
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scan_activity);
        intent = getIntent();
        qrCodeReader = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReader.setOnQRCodeReadListener(this);
        // Use this function to enable/disable decoding
        qrCodeReader.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReader.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReader.setTorchEnabled(true);

        // Use this function to set front camera preview
        qrCodeReader.setFrontCamera();

        // Use this function to set back camera preview
        qrCodeReader.setBackCamera();
    }
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        qrString = text;
        Log.e("QRSTRING", qrString+"");
        MainActivity.spfe.putString("qrString", qrString);
        MainActivity.spfe.commit();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("qrObtained", true);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReader.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReader.stopCamera();
    }

}
