package com.bepis.ugah3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.bepis.ugah3.getColors.getBlue;

public class CameraActivity extends AppCompatActivity {
    private final String[] CAMERA_PERMISSIONS = new String[] {
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    // onCreate
    private CameraManager cameraManager;
    private SurfaceView surfaceView;
    private String cameraId;
    private ImageReader imageReader;
    private HandlerThread handlerThread;
    private Handler handler;
    private TextView txt;
    private static int h;

    // onSurfaceCreated
    private SurfaceHolder surfaceHolder;
    private Surface previewSurface;
    private CaptureRequest captureRequest;
    private CameraCaptureSession cameraCaptureSession;
    private CameraDevice cameraDevice;
    private CameraCharacteristics cameraCharacteristics;

    // Nested classes
    private CameraSurfaceWatcher cameraSurfaceWatcher;
    private CameraStateWatcher cameraStateWatcher;
    private CameraCaptureWatcher cameraCaptureWatcher;
    private ImageReaderFrameWatcher imageReaderFrameWatcher;
    private SurfaceWatcher surfaceWatcher;

    void updateText(String text) {
        if (txt != null) {
            txt.setText(text);
        }
    }

    public void onWin() {
        Intent intent = new Intent(this, WinScreen.class);
        intent.putExtra("color", h);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("CameraActivity", "onCreate");

        setContentView(R.layout.activity_camera);
        //getActionBar().setTitle("Hex Hunter");
        this.surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        this.cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        handlerThread = new HandlerThread("MediaThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        this.cameraStateWatcher = new CameraStateWatcher();
        this.cameraSurfaceWatcher = new CameraSurfaceWatcher();
        this.cameraCaptureWatcher = new CameraCaptureWatcher();
        this.surfaceWatcher = new SurfaceWatcher();
        this.imageReaderFrameWatcher = new ImageReaderFrameWatcher();

        this.surfaceView.getHolder().addCallback(this.surfaceWatcher);

        try {
            this.cameraId = this.findFrontFacingCameraId();
        } catch (CameraAccessException e) {
            this.cameraId = null;
            e.printStackTrace();
        }

        if (this.cameraId == null) {
            Toast.makeText(this.getApplicationContext(), "You must have a camera on your device.", Toast.LENGTH_LONG).show();
        }

        //changes color of reticle
        ImageView img = (ImageView) findViewById(R.id.imageView);
        GradientDrawable shp = (GradientDrawable) img.getBackground();
        h = randomHex.getRandomHex();
        shp.setStroke(10, Color.argb(0xff, getColors.getRed(h), getColors.getGreen(h), getBlue(h)));

        TextView txt2 = (TextView) findViewById(R.id.textView2);
        txt2.setTextColor(Color.argb(0xFF, getColors.getRed(h), getColors.getGreen(h), getBlue(h)));
        txt2.setText("#"+Integer.toHexString(h));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("CameraActivity", "onRequestPermissionsResult");

        if (requestCode == 1) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("CameraActivity", "Access denied: " + permissions[i]);
                    Toast.makeText(this.getApplicationContext(), "The camera and audio recording is required.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Log.i("CameraActivity", "Camera and Audio Recording Access Granted!");
            this.openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("CameraActivity", "onPause");

        if (cameraCaptureSession != null) {
            try {
                cameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                Log.e("CameraActivity", String.format("%s: %s", "onPause", e.getMessage()));
            }

            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
        }

        if (this.imageReader != null) {
            this.imageReader.close();
            this.imageReader = null;
        }

        Log.i("CameraActivity", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.handlerThread.quitSafely();
    }

    private void openCamera() {
        Log.i("CameraActivity", "openCamera");

        for (int i = 0; i < CAMERA_PERMISSIONS.length; i++) {
            String perm = CAMERA_PERMISSIONS[i];

            if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                if (this.shouldShowRequestPermissionRationale(perm)) {
                    Log.i("CameraActivity", "Explaining Camera Access");
                    Toast.makeText(this.getApplicationContext(), "The camera is required for streaming.", Toast.LENGTH_LONG).show();
                }
                else {
                    Log.i("CameraActivity", "Requesting Camera Access");
                    this.requestPermissions(CAMERA_PERMISSIONS, 1);
                }
                return;
            }
        }

        Log.i("CameraActivity", "openCamera: Permissions Granted!");

        try {
            this.cameraManager.openCamera(this.cameraId, this.cameraStateWatcher, handler);
        } catch (CameraAccessException e) {
            Toast.makeText(this.getApplicationContext(), "Unable to access Camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @NonNull
    private String determineOutputMovieFile() {
        File moviesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File outputFile = new File(moviesFolder, "Example.mp4");
        moviesFolder.mkdirs();

        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile.getAbsolutePath();
    }

    private String findFrontFacingCameraId() throws CameraAccessException {
        String[] cameraIds = this.cameraManager.getCameraIdList();

        for (String cameraId : cameraIds) {
            cameraCharacteristics = this.cameraManager.getCameraCharacteristics(cameraId);

            if (CameraMetadata.LENS_FACING_BACK == cameraCharacteristics.get(CameraCharacteristics.LENS_FACING).intValue()) {
                Log.i("CameraActivity", String.format("%s: %s", "findFrontFacingCameraId", cameraId));
                return cameraId;
            }
        }

        if (cameraIds.length > 0) {
            return cameraIds[0];
        }

        return null;
    }

    private class CameraStateWatcher extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.i("CameraStateWatcher", "onOpened");
            CameraActivity.this.cameraDevice = cameraDevice;

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(previewSurface);
            surfaces.add(imageReader.getSurface());

            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                for (Surface surface : surfaces) {
                    captureRequestBuilder.addTarget(surface);
                }

                captureRequest = captureRequestBuilder.build();

                for (CaptureRequest.Key<?> key : captureRequest.getKeys()) {
                    Log.i("CameraStateWatcher", "Request Key: " + key.getName());
                }

                cameraDevice.createCaptureSession(surfaces, cameraSurfaceWatcher, handler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.i("CameraStateWatcher", "onDisconnected");
            CameraActivity.this.cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.i("CameraStateWatcher", "onError");
            CameraActivity.this.cameraDevice = null;
        }
    }

    private class CameraSurfaceWatcher extends  CameraCaptureSession.StateCallback {
        @Override
        public void onActive(CameraCaptureSession session) {
            super.onActive(session);
            Log.i("CameraSurfaceWatcher", "onActive");

            cameraCaptureSession = session;
        }

        @Override
        public void onClosed(CameraCaptureSession session) {
            super.onClosed(session);
            Log.i("CameraSurfaceWatcher", "onClosed");
        }

        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraSurfaceWatcher", "onConfigured");

            try {
                cameraCaptureSession.setRepeatingRequest(captureRequest, cameraCaptureWatcher, handler);
            } catch (CameraAccessException e) {
                Log.e("CameraSurfaceWatcher", String.format("%s: %s", "onConfigured", e.getMessage()));
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraSurfaceWatcher", "onConfigureFailed");
        }

        @Override
        public void onReady(CameraCaptureSession session) {
            super.onReady(session);
            Log.i("CameraSurfaceWatcher", "onReady");
        }

        @Override
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            super.onSurfacePrepared(session, surface);
            Log.i("CameraSurfaceWatcher", "onSurfacePrepared");
        }
    }

    private class CameraCaptureWatcher extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            //Log.i("CameraCaptureWatcher", "onCaptureStarted");
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Log.i("CameraCaptureWatcher", "onCaptureCompleted");
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.i("CameraCaptureWatcher", "onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.i("CameraCaptureWatcher", "onCaptureBufferLost");
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.i("CameraCaptureWatcher", "onCaptureFailed");
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.i("CameraCaptureWatcher", "onCaptureSequenceAborted");
        }
    }

    private class ImageReaderFrameWatcher implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                //Image.Plane plane = image.getPlanes()[0];
                int width = image.getWidth();
                int height = image.getHeight();
                /*ByteBuffer buffer = plane.getBuffer();
                //Log.i("ImageReaderFrameWatcher", "Size: " + buffer.limit());
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                int format = image.getFormat();
                /*Mat mat = ImageUtils.imageToMat(image);
                double[] pix = mat.get(mat.height()/2, mat.width()/2);
                double r = pix[2];
                double g = pix[1];
                double b = pix[0];*/
                int[] rgb = getRGBIntFromPlanes(image.getPlanes());
                image.close();
                final int pixel = rgb[width*height/2 + width/2];
                final String hex = "#"+Integer.toHexString(pixel);
                //System.out.println(pixel);
                txt = (TextView) findViewById(R.id.textView);
                final int[] pixels = new int[9];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        pixels[i] = rgb[((width)*(i+(height/2)) + j + width/2)];
                    }
                }
                //final int pixel = getColors.averageHex(pixels);
                //Log.d("Center Color: ", Integer.toHexString(pixel));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui
                        //updateText(Integer.toHexString(pixel));
                        //changes color of text

                        txt.setTextColor(Color.argb(0xFF, getColors.getRed(pixel), getColors.getGreen(pixel), getBlue(pixel)));
                        txt.setText(hex);
                    }
                });

                Button button = (Button) findViewById(R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (int p : pixels) {
                            if (percentMatch.percentMatch(h, p)) {
                                onWin();
                            }
                        }
                    }
                });

                image.close();
            }
        }


    }

    private class SurfaceWatcher implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.i("SurfaceWatcher", "surfaceCreated");
            CameraActivity.this.surfaceHolder = surfaceHolder;
            previewSurface = surfaceHolder.getSurface();

            Log.i("SurfaceWatcher", String.format("%dx%d", surfaceHolder.getSurfaceFrame().width(), surfaceHolder.getSurfaceFrame().height()));

            Rect rect = surfaceHolder.getSurfaceFrame();

            imageReader = ImageReader.newInstance(rect.width(), rect.height(), ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(imageReaderFrameWatcher, handler);

            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.i("SurfaceWatcher", "surfaceChanged");
            previewSurface = surfaceHolder.getSurface();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.i("SurfaceWatcher", "surfaceDestroyed");
            CameraActivity.this.surfaceHolder = null;
            previewSurface = null;
        }
    }

    private int[] getRGBIntFromPlanes(Image.Plane[] planes) {
        ByteBuffer yPlane = planes[0].getBuffer();
        ByteBuffer uPlane = planes[1].getBuffer();
        ByteBuffer vPlane = planes[2].getBuffer();

        int bufferIndex = 0;
        final int total = yPlane.capacity();
        final int uvCapacity = uPlane.capacity();
        final int width = planes[0].getRowStride();
        int mHeight = surfaceView.getHeight();
        int[] mRgbBuffer = new int[surfaceView.getWidth() * surfaceView.getHeight() * 3];
        int yPos = 0;
        for (int i = 0; i < mHeight; i++) {
            int uvPos = (i >> 1) * width;

            for (int j = 0; j < width; j++) {
                if (uvPos >= uvCapacity-1)
                    break;
                if (yPos >= total)
                    break;

                final int y1 = yPlane.get(yPos++) & 0xff;

            /*
              The ordering of the u (Cb) and v (Cr) bytes inside the planes is a
              bit strange. The _first_ byte of the u-plane and the _second_ byte
              of the v-plane build the u/v pair and belong to the first two pixels
              (y-bytes), thus usual YUV 420 behavior. What the Android devs did
              here (IMHO): just copy the interleaved NV21 U/V data to two planes
              but keep the offset of the interleaving.
             */
                final int u = (uPlane.get(uvPos) & 0xff) - 128;
                final int v = (vPlane.get(uvPos) & 0xff) - 128;
                if ((j & 1) == 1) {
                    uvPos += 2;
                }

                // This is the integer variant to convert YCbCr to RGB, NTSC values.
                // formulae found at
                // https://software.intel.com/en-us/android/articles/trusted-tools-in-the-new-android-world-optimization-techniques-from-intel-sse-intrinsics-to
                // and on StackOverflow etc.
                final int y1192 = 1192 * y1;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                r = (r < 0) ? 0 : ((r > 262143) ? 262143 : r);
                g = (g < 0) ? 0 : ((g > 262143) ? 262143 : g);
                b = (b < 0) ? 0 : ((b > 262143) ? 262143 : b);

                mRgbBuffer[bufferIndex++] = ((r << 6) & 0xff0000) |
                        ((g >> 2) & 0xff00) |
                        ((b >> 10) & 0xff);
            }
        }
        return mRgbBuffer;
    }

}