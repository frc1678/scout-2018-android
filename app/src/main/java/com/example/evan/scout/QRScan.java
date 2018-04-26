package com.example.evan.scout;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.net.ParseException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

/**
 * Created by sam on 3/27/18.
 */
public class QRScan extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    QRCodeReaderView qrCodeReader;
    public static String qrString = "";
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

        if(MainActivity.scoutNumber <= 6){
            qrCodeReader.setFrontCamera();
        }else if(MainActivity.scoutNumber >= 7){
            qrCodeReader.setFrontCamera();
            qrCodeReader.setBackCamera();
        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        boolean dontlogsuccess = false;
        Intent intent = new Intent(this, MainActivity.class);
        qrString = text;
        String prevQrString = MainActivity.sharedPreferences.getString("qrString", "");
        try{
            if(Integer.parseInt(prevQrString.substring(0, prevQrString.indexOf("|"))) > Integer.parseInt(qrString.substring(0, qrString.indexOf("|")))){
                Utils.makeToast(this, "WRONG CYCLE NUMBER!");
                qrString = prevQrString;
                dontlogsuccess = true;
            }else if(!qrString.contains("|")){
                Log.e("HAS NO PIPE", "NO PIPE");
                Utils.makeToast(this, "NO PIPE!");
                intent.putExtra("qrObtained", false);
                startActivity(intent);
            }else if(Integer.parseInt(prevQrString.substring(0, prevQrString.indexOf("|"))) <= 0){
                Log.e("INVALID CYCLE NUM!", prevQrString.substring(0, prevQrString.indexOf("\\|")));
                Utils.makeToast(this, "INVALID CYCLE NUM!");
                intent.putExtra("qrObtained", false);
                startActivity(intent);
            }
            Log.e("CYCLENUMBER!!!", Integer.parseInt(qrString.substring(0, qrString.indexOf("|"))) + "");
        }catch (ParseException e){
            e.printStackTrace();
        }catch(IndexOutOfBoundsException e){
            dontlogsuccess = true;
            e.printStackTrace();
        }catch (NullPointerException ne){
            Utils.makeToast(this, "FAIL SCAN!");
        }
        Log.e("QRSTRING", qrString+"");
        MainActivity.spfe.putString("qrString", qrString);
        MainActivity.spfe.commit();
        intent.putExtra("qrObtained", true);
        if(!dontlogsuccess){
            bgLoopThread.cycleNumber = Integer.parseInt(qrString.substring(0, qrString.indexOf("|")));
            MainActivity.spfe.putInt("cycle", bgLoopThread.cycleNumber);
            MainActivity.spfe.commit();
            Utils.makeToast(this, "SUCCESS SCAN");
        }
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
