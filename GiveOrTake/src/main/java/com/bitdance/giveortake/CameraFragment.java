package com.bitdance.giveortake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by nora on 7/1/13.
 */
public class CameraFragment extends Fragment {
    public static final String TAG = "CameraFragment";

    public static final String EXTRA_IMAGE_FILENAME = "extra_image_filename";
    public static final String EXTRA_THUMBNAIL_FILENAME = "extra_thumbnail_filename";
    private static final int THUMBNAIL_SIZE = 80;
    private static final int IMAGE_SIZE = 1024;
    private static final int CAMERA_ID = 0;

    private Camera camera;
    private SurfaceView surfaceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        surfaceView = (SurfaceView)view.findViewById(R.id.camera_surface);
        SurfaceHolder holder = surfaceView.getHolder();
        // deprecated but needed for pre-3.0 devices
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (camera != null) {
                        camera.setPreviewDisplay(surfaceHolder);
                    }
                } catch (IOException ioException) {
                    Log.e(TAG, "Error setting up the preview display", ioException);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                if (camera == null) return;
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(),
                        width, height);
                int squareSize = Math.min(s.width, s.height);
                squareSize = 480;
                ViewGroup.LayoutParams surfaceViewLayoutParams = surfaceView.getLayoutParams();
                surfaceViewLayoutParams.height = s.height;
                surfaceViewLayoutParams.width = s.width;
                surfaceView.setLayoutParams(surfaceViewLayoutParams);

                surfaceView.getLayoutParams().height = squareSize;
                surfaceView.getLayoutParams().width = squareSize;

                Log.i(TAG, "surface frame:" + surfaceHolder.getSurfaceFrame().toString());

                // will this work even though it's not supported?
                parameters.setPreviewSize(squareSize, squareSize);
                Log.i(TAG, "preview size: " + s.width + " x " + s.height);
                Camera.Size picSize = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                parameters.setPictureSize(picSize.width, picSize.height);

                Log.i(TAG, "pic size: " + picSize.width + " x " + picSize.height);

                Log.i(TAG, "Setting parameters: " + parameters.flatten());
                camera.setParameters(parameters);


                try {
                    camera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    camera.release();
                    camera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (camera != null) {
                    camera.stopPreview();
                }
            }
        });


        OrientationEventListener orientationEventListener =
                new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.i(TAG, "On orientation changed: " + orientation);
                if (orientation == ORIENTATION_UNKNOWN) return;
                android.hardware.Camera.CameraInfo info =
                        new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(CAMERA_ID, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == info.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }
                Log.i(TAG, "Rotation: " + rotation);
                camera.getParameters().setRotation(rotation);

            }
        };
        orientationEventListener.enable();

        Button takePictureButton = (Button)view.findViewById(R.id.camera_take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(shutterCallback, null, jpegCallback);
            }
        });
        return view;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestSmallSide = Math.min(bestSize.width, bestSize.height);
        for (Camera.Size s: sizes) {
            // The best size is the largest square.  Since we can't count on any sizes
            // being square, we'll choose the size with the largest of the smaller of
            // width and height, to make sure we get the max square
            Log.i(TAG, "Supported size: " + s.width + " x " + s.height);
            int smallSide = Math.min(s.width, s.height);
            if (smallSide > largestSmallSide) {
                bestSize = s;
                largestSmallSide = smallSide;
            }
        }
        return bestSize;
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // TODO: show progress?
            Toast.makeText(getActivity(), "Took picture", Toast.LENGTH_SHORT).show();
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            boolean success = false;
            BitmapFactory.Options options = new BitmapFactory.Options();
            Log.i(TAG, "bitmap sample size = " + options.inSampleSize);
            Log.i(TAG, "density = " + options.inDensity);
            Bitmap fullImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap squareImage = createSquareImage(fullImage);
            fullImage.recycle();
            Bitmap image = Bitmap.createScaledBitmap(squareImage, IMAGE_SIZE, IMAGE_SIZE, true);
            Bitmap thumbnail = createRoundedThumbnail(squareImage);
            squareImage.recycle();

            String baseFilename = UUID.randomUUID().toString();
            String filename = baseFilename + ".png";
            String thumbnailFilename = baseFilename + "_thumbnail.png";
            FileOutputStream out = null;
            FileOutputStream thumbOut = null;
            try {
                out = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                thumbOut = getActivity().openFileOutput(thumbnailFilename, Context.MODE_PRIVATE);
                image.compress(Bitmap.CompressFormat.PNG, 100, out);
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbOut);
                success = true;
            } catch (FileNotFoundException fnfe) {
                Log.e(TAG, "Could not open image file: ", fnfe);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "Error while closing image file", ioe);
                    }
                }
                if (thumbOut != null) {
                    try {
                        thumbOut.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "Error while closing thumbnail file", ioe);
                    }
                }
                image.recycle();
                thumbnail.recycle();
            }

            if (success) {
                Intent i = new Intent();
                i.putExtra(EXTRA_IMAGE_FILENAME, filename);
                i.putExtra(EXTRA_THUMBNAIL_FILENAME, thumbnailFilename);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            getActivity().finish();

        }
    };

    private Bitmap createSquareImage(Bitmap fullImage) {
        int size = Math.min(fullImage.getWidth(), fullImage.getHeight());
        Bitmap squareBitmap = Bitmap.createBitmap(fullImage, 0, 0, size, size);
        return squareBitmap;
    }

    private Bitmap createRoundedThumbnail(Bitmap fullImage) {
        // scaled correctly, but square
        Bitmap thumbnail = Bitmap.createScaledBitmap(fullImage, THUMBNAIL_SIZE, THUMBNAIL_SIZE,
                false);

        BitmapShader shader = new BitmapShader(thumbnail, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(0.0f, 0.0f, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        int radius = 5;

        Bitmap result = Bitmap.createBitmap(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawRoundRect(rect, radius, radius, paint);

        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
    }
}
