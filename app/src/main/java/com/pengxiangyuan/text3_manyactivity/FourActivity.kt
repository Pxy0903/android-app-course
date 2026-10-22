package com.pengxiangyuan.text3_manyactivity

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.pengxiangyuan.text3_manyactivity.databinding.ActivityFourBinding

class FourActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFourBinding

    // 权限申请launcher：申请CAMERA权限
    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // 用户授予相机权限，启动拍照
            takePhotoLauncher.launch(null)
        } else {
            Toast.makeText(this, "没有相机权限，无法拍照", Toast.LENGTH_SHORT).show()
        }
    }

    // 拍照预览契约，拍照成功返回缩略bitmap
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if(bitmap != null){
            binding.imageView.setImageBitmap(bitmap)
            Toast.makeText(this@FourActivity,"拍照成功！图片已显示",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this@FourActivity,"拍照取消或者失败",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            checkAndOpenCamera()
        }
    }

    private fun checkAndOpenCamera() {
        // 检查相机权限
        when {
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(this,"需要相机权限才能拍照",Toast.LENGTH_SHORT).show()
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // 请求权限，会弹出系统权限对话框
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
