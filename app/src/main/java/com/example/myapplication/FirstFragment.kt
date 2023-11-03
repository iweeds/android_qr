package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentFirstBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DefaultDecoderFactory


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val TAG = javaClass.simpleName

    private val CAMERA_PERMISSION = "android.permission.CAMERA"


    private var currentTime = 0L
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    /**
     * 권한처리 결과 콜백
     */
    private var permissionListener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            Toast.makeText(requireContext(), "권한 부여됨!", Toast.LENGTH_SHORT).show()
//            startCamera()
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
            Toast.makeText(
                requireContext(),
                "권한 거절!\n$deniedPermissions",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * QR 스캔 결과 콜백
     */
    private val callback = BarcodeCallback { result ->
        if (System.currentTimeMillis() - this.currentTime >= 3000) {
            result?.let {
                val payload = it.result.text
                val point = Gson().fromJson(payload, Point::class.java)
                (activity as MainActivity).insertPoint(point)

                Log.d(TAG, "barcode callback >> $payload")
            }
        }

        this.currentTime = System.currentTimeMillis()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        _binding?.layoutPointArea?.bringToFront()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraPermission()
        initBarcodeScannerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        binding.barcodeScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }


    /**
     * QR 코드 스캐너 초기화
     */
    private fun initBarcodeScannerView() {
        with(binding.barcodeScanner) {
            barcodeView.decoderFactory =
                DefaultDecoderFactory(arrayListOf(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13))
            barcodeView.cameraSettings.requestedCameraId = 0
            barcodeView.cameraSettings.isAutoFocusEnabled = true
            decodeContinuous(callback)
        }
    }

    /**
     * 카메라 스캔 시작
     */
    private fun startCamera() {
        binding.barcodeScanner.resume()
    }


    /**
     * 카메라 권한요청
     */
    private fun requestCameraPermission() {

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(CAMERA_PERMISSION)
            .check()

    }

    fun setupFragmentUI(earnPoint: String) {
        binding.textTotalPoint.text = earnPoint
    }


}