package com.example.cbitss.videouploading;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_PICCODE = 989;
    String mCameraFileName;
    VideoView videoView;
    Button mVideoRecording,mUpload;
    Uri uri;
    String path;
    String outPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoRecording     =   (Button)findViewById(R.id.videorecording);
        mUpload             =   (Button)findViewById(R.id.upload);
        videoView           =   (VideoView)findViewById(R.id.videoview);
        mVideoRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent,CAPTURE_PICCODE );*/
                videoIntent();
            }
        });

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploaddata();
            }
        });

    }

    private void videoIntent(){

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("-mm-ss");

        String newPicFile = df.format(date) + ".mp4";
        outPath = Environment.getExternalStorageDirectory()+ "/sdcard/" + newPicFile;
        File outFile = new File(outPath);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, CAPTURE_PICCODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_PICCODE) {


            uri = data.getData();
//                path = data.getData().getPath();
//                path = Uri.parse(String.valueOf(uri));
            path = data.getData().getPath();
            /*byte[] byteBufferString = new byte[20971520];
            uri= Uri.parse((Base64.encodeToString(byteBufferString, Base64.DEFAULT)));


*/

            outPath = getRealPathFromURI(MainActivity.this, uri);

            Log.e("ABC", String.valueOf(uri));
            Log.e("path2",path);
            Log.e("path", outPath);
            MediaController vidControl = new MediaController(MainActivity.this);
            vidControl.setAnchorView(videoView);
            videoView.setMediaController(vidControl);
//                videoView.setVideoPath(path);
            videoView.setVideoPath(outPath);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    videoView.start();
                }
            });
        }
    }


    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private String videoToBase64(File file) {
        String encodedString = path;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        Log.i("Strng", encodedString);

        return encodedString;
    }

    public void uploaddata() {

        final String video = path;

/*
//        final ProgressDialog loading = ProgressDialog.show(this,"Uploading","Please wait...",false,false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://api.hostingfunda.com/video-uploading/video.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        loading.dismiss();
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();


                params.put("video",video);


                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);*/

        try
        {

            OkHttpClient client = new OkHttpClient(); // create your own OkHttp client
            UploadService.HTTP_STACK = new OkHttpStack(client); // make the library use your own OkHttp client



            final ProgressDialog pd = new ProgressDialog(this);
            pd.setCancelable(false);
            pd.show();
            new MultipartUploadRequest(MainActivity.this, "http://api.hostingfunda.com/video-uploading/video.php")
                    .setMethod("POST")
                    .setNotificationConfig(null)
                    .addFileToUpload(outPath, "video")
                    .setMaxRetries(3)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serveresponse, Exception exception) {
//                            Toast.makeText(context, ""+serverResponse.toString(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, ""+exception.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("error", exception.toString());
                            pd.dismiss();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            Toast.makeText(context, "uploaded"+serverResponse.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("server path",serverResponse.toString()+uploadInfo.toString());
                            pd.dismiss();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            pd.dismiss();
                        }
                    })
                    .startUpload();
        }
        catch (Exception e)
        {
            Log.d("Error", e.toString());
        }

    }

}
