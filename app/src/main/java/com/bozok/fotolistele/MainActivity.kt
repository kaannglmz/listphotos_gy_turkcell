package com.bozok.fotolistele

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bozok.fotolistele.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.popup.view.*
import java.io.File
import java.io.IOException


//Açılış ekranında üstte bir resim ekle butonu olacak. Bu butona basıldığında alertte kamera ve galeri seçenekleri olan popup çıkacak.
// Kamera seçildiğinde Kamera, Galeri seçildiğinde galeri açılacak. Kameradan resim çekildiğinde ya da galeriden seçildiğinde butonun
// altındaki listeye eklenecek.
//Çekilen resimlerin tümü 2 sütun olarak recyclerview ile listelenecek.
//Resime basıldığında o resim aynı ekranda büyük bir şekilde gösterilecek.
//Resime uzun basıldığında ise alert ile sil/vazgeç seçenekleri çıkacak. Sil seçildiğinde o resim listeden silinecek ve liste yenilenecek


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    var photoUriList=ArrayList<Uri>()

    private lateinit var imagePath:String
    private lateinit var imageUri: Uri

    val reqcodeCamera=0
    val reqcodeGallery=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val lm= GridLayoutManager(applicationContext,2, GridLayoutManager.VERTICAL,false)
        binding.rvList.layoutManager=lm
        binding.rvList.adapter= ListAdapter(this, photoUriList,::imageClick,::imageLongClick)

        binding.btnAddImage.setOnClickListener {
            btnAddImage_OnClick()
        }

    }

    fun imageClick(position:Int){
        showPopUp(photoUriList.get(position))
    }

    fun imageLongClick(position:Int){

        var adb=AlertDialog.Builder(this)
        adb.setTitle("Delete")
        adb.setMessage("Do you want delete?")
        adb.setPositiveButton("Yes",DialogInterface.OnClickListener { dialogInterface, i ->
            photoUriList.removeAt(position)
            binding.rvList.adapter!!.notifyDataSetChanged()

        })
        adb.setNegativeButton("No",null)
        adb.show()


    }


    fun btnAddImage_OnClick(){

        var adb=AlertDialog.Builder(this)
        adb.setTitle("Choose")
        adb.setMessage("Please choose CAMERA or GALLERY")
        adb.setPositiveButton("CAMERA", DialogInterface.OnClickListener { dialogInterface, i ->

            // camera
            checkCameraPermisson()

        })

        adb.setNegativeButton("GALLERY", DialogInterface.OnClickListener { dialogInterface, i ->

            // gallery
            checkGalleryPermission()

        })
        adb.show()


    }

    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryRL.launch(galleryIntent)
    }

    private fun openCamera() {

        var cameraIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val dosya=createImageFile()
        imageUri= FileProvider.getUriForFile(this,packageName,dosya)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraRl.launch(cameraIntent)
    }

    private var cameraRl=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode== RESULT_OK){

            Toast.makeText(this, "Photo Was Taken", Toast.LENGTH_SHORT).show()

            photoUriList.add(imageUri)
            binding.rvList.adapter!!.notifyDataSetChanged()

        }
    }

    private var galleryRL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if (imageData != null) {

                        photoUriList.add(imageData)
                        binding.rvList.adapter!!.notifyDataSetChanged()
                    }
                }
            }
        }

    private fun checkGalleryPermission() {
        val requestList = ArrayList<String>()
        val permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!permissionState) {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestList.isEmpty()) {
            // izin var
            openGallery()
        } else {
            requestPermissions(requestList.toTypedArray(), reqcodeGallery)
        }
    }

    private fun checkCameraPermisson(){
        val requesList=ArrayList<String>()

        var permissionState= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if(!permissionState){
            requesList.add(Manifest.permission.CAMERA)
        }

        permissionState= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if(!permissionState){
            requesList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        permissionState= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if(!permissionState){
            requesList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(requesList.size==0){
            //izin var
            openCamera()
        }else{
            requestPermissions(requesList.toTypedArray(),reqcodeCamera)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allApproved=true
        for (gr in grantResults){
            if(gr != PackageManager.PERMISSION_GRANTED){
                allApproved=false
                break
            }
        }

        if(!allApproved){

            var dontShowAgain=false

            for(permission in permissions){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                    //reddedildi
                }else if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED) {
                    //onaylandı
                }else{
                    //tekrar gösterme seçildi
                    dontShowAgain=true
                    break
                }
            }

            if (dontShowAgain){
                val adb= AlertDialog.Builder(this)
                adb.setTitle("Permission Required")
                adb.setMessage("Go to settings and confirm all permissions")
                adb.setPositiveButton("Settings", { dialog, which ->
                    openSettings()
                })
                adb.setNegativeButton("Give Up",null)
                adb.show()

            }

        }else{
            when(requestCode){
                reqcodeCamera -> openCamera()
                reqcodeGallery -> openGallery()
            }
        }

    }

    private fun openSettings() {

        val intent= Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri= Uri.fromParts("package",packageName,null)
        intent.data=uri

        startActivity(intent)

    }


    @Throws(IOException::class)
    fun createImageFile() : File {

        val dir=getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("resim",".jpg",dir).apply {
            imagePath=absolutePath

        }
    }

    fun showPopUp(positionUri: Uri){ // oncreate kısmında çalışmaz!!!
        val v=layoutInflater.inflate(R.layout.popup,null)
        val popAlert= PopupWindow(v,windowManager.defaultDisplay.width,windowManager.defaultDisplay.height)
        // nerde gösterileceğini belirliyoruz
        popAlert.showAtLocation(v, Gravity.CENTER,0,0)
        v.ivFSPhoto.setImageURI(positionUri)

        v.setOnClickListener {
            popAlert.dismiss()
        }



    }

}






















