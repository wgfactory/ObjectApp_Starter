package com.wgf.objectapp_starter

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    //TODO - 전역 변수 셋팅
    private val TAG = MainActivity::class.java.simpleName

    /**
     * (1) onCreate() :
     *
     *      생명주기 가장 첫 실행되는 함수
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO - 코드 추가 : checkPermission() 호출
    }

    /**
     * 권한처리
     * (2) checkPermission() :
     *
     *      권한처리를 수행하는 함수
     */
    fun checkPermission(permissions: Array<out String>, flag: Int) : Boolean {
        //TODO - 로그 및 코드 추가

    }

    /**
     * (3) onRequestPermissionsResult() :
     *
     *      Permission(권한) 처리 후 결과를 확인하는 함수
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_PERMISSION_STORAGE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        //TODO - 코드 추가 : 저장소 권한 획득 실패 시 showToast()
                        finish()
                        return
                    }
                }

                setViews()
            }
            REQ_PERMISSION_CAMERA -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        //TODO - 코드 추가 : 카메라 권한 획득 실패 시 showToast()
                        return
                    }
                }
                openCamera()
            }
        }
    }

    /**
     * (4) setViews() :
     *
     *      Permission(권한) 처리 후 버튼 클릭을 대기하는 뷰 함수
     */
    fun setViews(){
        Log.d(TAG, ">> setViews()")

        //TODO - 코드 추가 : 버튼 클릭 시 함수 호출
    }

    /**
     * (5) openCamera() :
     *
     *      시스템 카메라 앱을 호출하여 수행하는 함수
     */
    fun openCamera() {
        Log.d(TAG, ">> openCamera()")

        if (checkPermission(CAMERA_PERMISSION, REQ_PERMISSION_CAMERA)) {

            // 시스템 카메라 앱 호출 Intent
            //TODO - 코드 추가 : Intent 코드 추가

            createImageUri(newFileName(), "image/jpg")?.let { uri ->
                mPhotoURI = uri
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI)
                //TODO - 코드 추가 : Intent 실행하는 코드 추가
            }
        }
    }

    /**
     * (6) openGallery() :
     *
     *      시스템 갤러리 앱을 호출하여 수행하는 함수
     */
    fun openGallery() {
        Log.d(TAG, ">> openGallery()")

        // 시스템 갤러리 앱 호출 Intent
        //TODO - 코드 추가 : Intent 코드 추가
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        //TODO - 코드 추가 : Intent 실행하는 코드 추가
    }

    /**
     * (7) onActivityResult() :
     *
     *      카메라, 갤러리 앱 호출 후 결과를 수행하는 함수
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //TODO - 코드 추가 : RequestCode가 RESULT_OK 인지 체크!
            when(requestCode){
                //TODO - 코드 추가 : 요청 코드가 Camera 일 때
                    if (mPhotoURI != null) {
                        val bitmap = loadBitmapFromMediaStoreBy(mPhotoURI!!)
                        mBitmap = bitmap
                        val image = getCapturedImage(mPhotoURI!!)
                        //TODO - 코드 추가 : runObjectDetection 함수 호출

                        mPhotoURI = null // 사용 후 null 처리
                    }
                }
                //TODO - 코드 추가 : 요청 코드가 Gallery 일 때
                    val uri = data?.data
                    val image = getCapturedImage(uri!!)
                    //TODO - 코드 추가 : runObjectDetection 함수 호출
                }
            }
        }
    }

    /**
     * (8) createImageUri() :
     *
     *      미디어스토어에 카메라 이미지를 저장할 URI를 미리 생성하는 함수
     */
    fun createImageUri(filename: String, mimeType: String) : Uri? {
        Log.d(TAG, ">> createImageUri()")

        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    /**
     * (9) newFileName() :
     *
     *      촬영된 사진의 파일 이름을 생성하는 함수
     */
    fun newFileName() : String {
        Log.d(TAG, ">> newFileName()")

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.jpg"
    }

    /**
     * (10) loadBitmapFromMedia() :
     *      Camera 촬영으로 얻은 원본 이미지 가져오기
     *      미디어 스토어에서 uri로 이미지 불러오는 함수
     */
    fun loadBitmapFromMediaStoreBy(photoUri: Uri): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) { // Api 버전별 이미지 처리
                val source: ImageDecoder.Source =
                        ImageDecoder.createSource(this.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    /**
     * (11) getCapturedImage():
     *     Decodes and center crops the captured image from camera.
     *     Object Detection을 수행하기 전에 이미지 처리를 수행하는 함수
     */
    private fun getCapturedImage(imgUri: Uri): Bitmap {
        Log.d(TAG, ">> getCapturedImage()")

        var srcImage: Bitmap? = null
        try {
            srcImage = InputImage.fromFilePath(baseContext, imgUri!!).bitmapInternal
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // crop image to match imageView's aspect ratio
        val scaleFactor = Math.min(
                srcImage!!.width / imagePreview.width.toFloat(),
                srcImage!!.height / imagePreview.height.toFloat()
        )

        val deltaWidth = (srcImage.width - imagePreview.width * scaleFactor).toInt()
        val deltaHeight = (srcImage.height - imagePreview.height * scaleFactor).toInt()

        val scaledImage = Bitmap.createBitmap(
                srcImage, deltaWidth / 2, deltaHeight / 2,
                srcImage.width - deltaWidth, srcImage.height - deltaHeight
        )
        srcImage.recycle()
        return scaledImage
    }

    /**
     * MLKit Object Detection Function
     * (12) runObjectDetection() :
     *      MLKit Object Detection Function
     *      Object Detection을 수행하는 함수
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        Log.d(TAG, ">> runObjectDetection()")

        // Step 1: create MLKit's VisionImage object
        //TODO - 코드 추가 : image 코드 추가

        // Step 2: acquire detector object
        //TODO - 코드 추가 : option 코드 추가

        //TODO - 코드 추가 : detector 코드 추가

        // Step 3: feed given image to detector and setup callback
        //TODO - 코드 추가 : processor 코드 추가
                .addOnSuccessListener {
                    // Task completed successfully
                    // Post-detection processing : draw result

                    if(it.size == 0) {
                        //TODO - 코드 추가 : 사물을 못 찾았을 때 Toast 표시!
                    }
                    val drawingView = DrawingView(applicationContext, it)
                    //TODO - 코드 추가 : drawingView에 박스 그려주기 코드

                    runOnUiThread {
                        //TODO - 코드 추가 : imageView에 표시해주기
                    }
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    showToast("Oops, something went wrong!")
                }
    }

    /**
     * (13) showToast() :
     *      Make Toast Function
     *     토스트 메세지 표시 하는 함수
     */
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Log.d(TAG, ">> showToast()")

        //TODO - 코드 추가 : Toast 코드 - applicationContext 사용하기!
        toast.setGravity(Gravity.CENTER, 0, 0)
        //TODO - 코드 추가 : toast show 코드 추가
    }
}