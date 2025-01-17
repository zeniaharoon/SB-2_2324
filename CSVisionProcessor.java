//This is broken pls fixx <3
package org.firstinspires.ftc.teamcode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class CSVisionProcessor extends BlocksOpModeCompanion implements VisionProcessor {
    StartingPosition selection = StartingPosition.NONE;

    Mat b = new Mat();
    Mat g = new Mat();
    Mat r = new Mat();
    Mat hsvMat = new Mat();

    final int leftTh = 207;
    final int rightTh = 445;
    final int upperTh = 100;
    final int lowerTh = 360;

    final int width = 640;
    final int height = 480;

//    private static CSVisionProcessor _csVision;

//    @ExportToBlocks(
//            comment = "Custom CenterStage Vision Processor",
//            tooltip = "Auto OpMode Vision",
//            parameterLabels = {}
//            //parameterLabels = {"Width", "Left X", "Left Y", "Middle X", "Middle Y", "Right X", "Right Y"}
//    )
//    public static VisionProcessor getCSVision(
//
//    ) {
//        _csVision = new CSVisionProcessor();
//        return _csVision;
//    }

    public StartingPosition getPosition() {
        return selection;
    }

    public int getIntPosition() {
        StartingPosition pos = selection;

        if (pos == StartingPosition.LEFT) {
            return 1;
        } else if (pos == StartingPosition.CENTER) {
            return 2;
        } else if (pos == StartingPosition.RIGHT) {
            return 3;
        }

        return 0;
    }

    public CSVisionProcessor() {

    }


    @Override
    public void init(int width, int height, CameraCalibration calibration) {
    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        List<Mat> mats = new ArrayList<Mat>();
        Core.split(frame, mats);
        //telemetry.addData("mat size cols", mats.get(0).cols());
        //telemetry.addData("mat size rows", mats.get(0).rows());
        //telemetry.update();
        b = mats.get(0);
        g = mats.get(1);
        r = mats.get(2);
        //telemetry.addData("Size", b.size());
        //telemetry.update();
        Imgproc.cvtColor(frame, hsvMat, Imgproc.COLOR_RGB2HSV);
//
//        double satRectLeft = getAvgSaturation(hsvMat, rectLeft);
//        double satRectMiddle = getAvgSaturation(hsvMat, rectMiddle);
//        double satRectRight = getAvgSaturation(hsvMat, rectRight);
        double ilowH = 106;
        double ilowS = 124;
        double ilowV = 0;

        double ihighH = 134;
        double ihighS = 255;
        double ihighV = 191;
        Mat threshMat = new Mat();
        Scalar lower_hsv = new Scalar(ilowH, ilowS, ilowV);
        Scalar higher_hsv = new Scalar(ihighH, ihighS, ihighV);
        Core.inRange(hsvMat, lower_hsv, higher_hsv, threshMat);

        //apply morphology
        Size kernel1 = new Size(9, 9);
        Size kernel2 = new Size(15, 15);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernel1);
        Mat cleanMat = new Mat();
        Imgproc.morphologyEx(threshMat, cleanMat, Imgproc.MORPH_OPEN, kernel);
        kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernel2);
        Imgproc.morphologyEx(cleanMat, cleanMat, Imgproc.MORPH_CLOSE, kernel);

        // get external contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cleanMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int max_index = -1;
        double max_area = -1;
        for (int i = 0; i < contours.size(); i++) {
            double Area = Imgproc.contourArea(contours.get(i));

            if (Area > max_area) {
                max_area = Area;
                max_index = i;
            }
        }

        int leftCounter = 0;
        int rightCounter = 0;
        int centerCounter = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i > 0 && i < leftTh && j > upperTh && j < lowerTh && b.get(i,j) != null) {
                    leftCounter += b.get(i, j)[0];
                }
                else if (i > leftTh && i < rightTh && j > upperTh && j < lowerTh && b.get(i,j) != null) {
                    centerCounter += b.get(i, j)[0];
                }
                else if (j > upperTh && j < lowerTh && b.get(i,j) != null) {
                    rightCounter += b.get(i, j)[0];
                }
            }
        }

        if (leftCounter > centerCounter && leftCounter > rightCounter) {
            selection = StartingPosition.LEFT;
            return selection;
        } else if (rightCounter > centerCounter && rightCounter > leftCounter) {
            selection = StartingPosition.RIGHT;
            return selection;
        } else if (centerCounter > leftCounter && centerCounter > rightCounter) {
            selection = StartingPosition.CENTER;
            return selection;
        } else {
            selection = StartingPosition.NONE;
            return selection;
        }
//        if (max_index > 0) {
//            Rect box = Imgproc.boundingRect(contours.get(max_index));
//
//
//            if ((box.width + box.x) < leftTh) {
//                leftCounter += 1;
//
//            } else if (box.x > rightTh) {
//                rightCounter += 1;
//            }
//
//            if (leftCounter == 1) {
//                selection = StartingPosition.LEFT;
//
//            } else if (leftCounter == 0 && rightCounter == 0) {
//                selection = StartingPosition.CENTER;
//
//            } else if (rightCounter == 1) {
//                selection = StartingPosition.RIGHT;
//            } else {
//
//                selection = StartingPosition.NONE;
//            }
//
//            return selection;
//        }

    }
//    protected double getAvgSaturation(Mat input, Rect rect) {
//        submat = input.submat(rect);
//        Scalar color = Core.mean(submat);
//        return color.val[1];
//    }

//    private android.graphics.Rect makeGraphicsRect(Rect rect, float scaleBmpPxToCanvasPx) {
//        int left = Math.round(rect.x * scaleBmpPxToCanvasPx);
//
//        int top = Math.round(rect.y * scaleBmpPxToCanvasPx);
//        int right = left + Math.round(rect.width * scaleBmpPxToCanvasPx);
//
//        int bottom = top + Math.round(rect.height * scaleBmpPxToCanvasPx);
//
//        return new android.graphics.Rect(left, top, right, bottom);
//    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight,
                            float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        Paint selectedPaint = new Paint();
        selectedPaint.setColor(Color.GREEN);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(scaleCanvasDensity * 4);

        Paint nonSelected = new Paint();
        nonSelected.setStrokeWidth(scaleCanvasDensity * 4);
        nonSelected.setStyle(Paint.Style.STROKE);
        nonSelected.setColor(Color.RED);

//        Rect cl = new Rect(0, upperTh, leftTh, upperTh - lowerTh);
//        Rect cc = new Rect(leftTh, upperTh, rightTh - leftTh, upperTh - lowerTh);
//        Rect cr = new Rect(rightTh, upperTh, width - rightTh, upperTh - lowerTh);

//        android.graphics.Rect drawRectangleLeft = makeGraphicsRect(cl, scaleBmpPxToCanvasPx);
//        android.graphics.Rect drawRectangleMiddle = makeGraphicsRect(cc, scaleBmpPxToCanvasPx);
//        android.graphics.Rect drawRectangleRight = makeGraphicsRect(cr, scaleBmpPxToCanvasPx);

        android.graphics.Rect drawRectangleLeft = new android.graphics.Rect(0, upperTh, leftTh, lowerTh);
        android.graphics.Rect drawRectangleMiddle = new android.graphics.Rect(leftTh, upperTh, rightTh, lowerTh);
        android.graphics.Rect drawRectangleRight = new android.graphics.Rect(rightTh, upperTh, width, lowerTh);

        selection = (StartingPosition) userContext;

        switch (selection) {
            case LEFT:
                canvas.drawRect(drawRectangleLeft, selectedPaint);
                canvas.drawRect(drawRectangleMiddle, nonSelected);
                canvas.drawRect(drawRectangleRight, nonSelected);
                break;

            case RIGHT:
                canvas.drawRect(drawRectangleLeft, nonSelected);
                canvas.drawRect(drawRectangleMiddle, nonSelected);
                canvas.drawRect(drawRectangleRight, selectedPaint);
                break;
            case CENTER:
                canvas.drawRect(drawRectangleLeft, nonSelected);
                canvas.drawRect(drawRectangleMiddle, selectedPaint);
                canvas.drawRect(drawRectangleRight, nonSelected);
                break;
            case NONE:
                canvas.drawRect(drawRectangleLeft, nonSelected);
                canvas.drawRect(drawRectangleMiddle, nonSelected);
                canvas.drawRect(drawRectangleRight, nonSelected);
                break;

        }

    }

    public StartingPosition getStartingPosition() {
        return selection;
    }

    public enum StartingPosition {
        NONE,
        LEFT,
        RIGHT,
        CENTER
    }


}
