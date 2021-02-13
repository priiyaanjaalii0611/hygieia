package com.example.android.hack1;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class CameraFragment  extends Fragment implements SurfaceHolder.Callback {


    Camera camera;
    Camera.PictureCallback jpegCallBack;

    SurfaceView mSufaceView;

    SurfaceHolder mSufaceHolder;
    final int CAMERA_REQUEST_CODE=1;
    public static CameraFragment newInstance(){
        CameraFragment fragment= new CameraFragment();
        return fragment;
    }

    // CHECK @NONNULL before layoutinflator inflater
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_camera ,container,false);
        mSufaceView= view.findViewById(R.id.surfaceView);
        mSufaceHolder = mSufaceView.getHolder();


        if(ActivityCompat.checkSelfPermission(getContext(),android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        else
        {mSufaceHolder.addCallback(this);
        mSufaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }


        Button mLogout =view.findViewById(R.id.logout);
        Button mCapture=view.findViewById(R.id.capture);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        jpegCallBack= new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                Bitmap decodeBitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);

                Bitmap rotateBitmap = rotate(decodeBitmap);
                String fileLocation=SaveImageToStorage(rotateBitmap);
                if(fileLocation!=null)
                {Intent intent= new Intent(getActivity(), ShowCaptureActivity.class);
                startActivity(intent);
                return;}

            }
        };



        return view;
    }
public  String SaveImageToStorage(Bitmap bitmap){
        String filename="imageToSend";
        try{
            ByteArrayOutputStream bytes= new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
            FileOutputStream fo= getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();

        }
    catch (Exception e) {
        e.printStackTrace();
        filename=null;}
        return filename;
}
    private Bitmap rotate(Bitmap decodeBitmap) {
        int w =decodeBitmap.getWidth();
        int h= decodeBitmap.getHeight();

        Matrix matrix=new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodeBitmap,0,0,w,h,matrix, true);

    }

    private void captureImage() {
        camera.takePicture(null,null,jpegCallBack);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera= Camera.open();

        Camera.Parameters parameters;
        parameters =camera.getParameters();

        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        Camera.Size bestSize= null;
        List<Camera.Size> sizeList=camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);


        for(int i=1;i<sizeList.size();i++){
            if((sizeList.get(i).width * sizeList.get(i).height)>(bestSize.width*bestSize.height)){
                bestSize=sizeList.get(i);
            }
        }
        parameters.setPreviewSize(bestSize.width,bestSize.height);

        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }


        camera.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case  CAMERA_REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    {mSufaceHolder.addCallback(this);
                        mSufaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    }
                }else{
                    Toast.makeText(getContext(),"please provide permissions",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }
    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent= new Intent(getContext(),SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return;
    }
}
