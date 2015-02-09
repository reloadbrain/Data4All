package io.github.data4all.view;

import io.github.data4all.logger.Log;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class serves as Previewclass for the camera . This class creates the
 * preview with all associated views and handles the control of the camera in
 * cooperation with the CameraActivity.
 * 
 * @author: Andre Koch
 */

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private static final String TAG = CaptureCameraSurfaceView.class
            .getSimpleName();

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Size mPreviewSize;
    private Size mPhotoSize;
    private List<Size> mSupportedPreviewSizes;
    private List<Size> mSupportedPictureSizes;
    private List<String> mSupportedFlashModes;
    private List<String> mAutoFocus;
    public static int containerWidth = 0;
    public static int containerHeight = 0;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);

    }

    public CameraPreview(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = resolveSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (containerWidth < width || containerHeight < height) {
            containerWidth = width;
            containerHeight = height;
        }

        if (mSupportedPreviewSizes != null)
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
                    containerWidth, containerHeight);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height
                        / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width
                        / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width,
                        (height + scaledChildHeight) / 2);
            }

        }
    }

    private void init(Context context) {

        SurfaceView mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            // get a group of supported preview size
            mSupportedPreviewSizes = mCamera.getParameters()
                    .getSupportedPreviewSizes();

            mSupportedPictureSizes = mCamera.getParameters()
                    .getSupportedPictureSizes();

            mSupportedFlashModes = mCamera.getParameters()
                    .getSupportedFlashModes();
            mAutoFocus = mCamera.getParameters().getSupportedFocusModes();

            requestLayout();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);

                // Open Camera Instance and assign to the reference

                // retrieve the parameters of cameras
                Camera.Parameters params = mCamera.getParameters();

                // set the preview size for camera view

                params.getVerticalViewAngle();
                params.getHorizontalViewAngle();

                // set the picture size for taking photo
                Log.d(TAG, "########## calcuated mPreviewSize width="
                        + mPreviewSize.width + ", height="
                        + mPreviewSize.height);

                mPhotoSize = getOptimalPictureSize(mSupportedPictureSizes,
                        (mPreviewSize.width * 1.0)
                                / (mPreviewSize.height * 1.0));

                params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

                // set the picture size for taking photo
                Log.d(TAG, "########## calcuated Photo width="
                        + mPhotoSize.width + ", height=" + mPhotoSize.height);
                params.setPictureSize(mPhotoSize.width, mPhotoSize.height);
                // set the picture type for taking photo
                params.setPictureFormat(ImageFormat.JPEG);

                // if auto focus support, use auto focus
                if (mAutoFocus.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

                // if auto flash support, use flash
                if (mSupportedFlashModes != null
                        && mSupportedFlashModes
                                .contains(Camera.Parameters.FLASH_MODE_AUTO))
                    params.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);

            }
        } catch (IOException ex) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", ex);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

        Log.d(TAG, "surfaceChanged is called");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mCamera != null) {
            mCamera.stopPreview();
        }

    }

    /*
     * @Function: get optimal picture size according to camera view angles
     */
    private Size getOptimalPictureSize(List<Size> sizes, double targetRatio) {
        Log.d(TAG, "###### picture targetRatio=" + targetRatio);
        final double ASPECT_TOLERANCE = 0.1;
        if (sizes == null)
            return null;
        Size optimalSize = null;
        

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            Log.d(TAG, "######## ratio=" + ratio);
            if (size.height * size.width > 1200000)
                continue;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            else {
                optimalSize = size;
                break;
            }

        }

        return optimalSize;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        // calculate the ratio of preview display
        double targetRatio = (double) w / h;
        // if no supported preview sizes, return null
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Set target Height based on the
        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.d(TAG, "optimalSize is null");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}