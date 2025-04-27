package com.example.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

class Button2Fragment : Fragment() {

    private lateinit var image_user: ImageView
    private lateinit var nom_user: EditText
    private lateinit var email_user: EditText
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File

    // ✅ Lanceur pour prendre la photo
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            image_user.setImageURI(photoUri)
        } else {
            Toast.makeText(requireContext(), "Photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Lanceur pour la permission caméra
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(photoUri)
        } else {
            Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Lanceur pour choisir une image depuis la galerie
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // 1) Copier l'image choisie dans photoFile
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(photoFile)
                inputStream?.copyTo(outputStream)

                outputStream.close()
                inputStream?.close()

                // 2) Mettre à jour le ImageView en utilisant l'URI du FileProvider
                val newUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
                image_user.setImageURI(null)
                image_user.setImageURI(newUri)

        } else {
            Toast.makeText(requireContext(), "Aucune image sélectionnée", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Lanceur pour permission de galerie
    private val requestPermissionLauncherCapture = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Récupère à nouveau la photo dans le fichier quand on revient sur ce fragment
    override fun onResume() {
        super.onResume()
        if (photoFile.exists()) {
            val imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
            image_user.setImageURI(null)
            image_user.setImageURI(imageUri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_button2, container, false)

        // ✅ Prépare les vues
        image_user = v.findViewById(R.id.editProfileImage)
        nom_user = v.findViewById(R.id.editName)
        email_user = v.findViewById(R.id.editEmail)

        val saveButton = v.findViewById<Button>(R.id.saveProfileButton)
        val changeImageButton = v.findViewById<Button>(R.id.changeImageButton)

        // ✅ Crée le fichier unique
        photoFile = File(requireContext().filesDir, "photo.jpg")
        if (!photoFile.exists()) { photoFile.createNewFile() }
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)

        saveButton.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("fragmentapp", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("name", nom_user.text.toString())
            editor.putString("email", email_user.text.toString())
            editor.apply()
            gotofragment(Button1Fragment())
        }

        changeImageButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), changeImageButton)
            popup.menuInflater.inflate(R.menu.pop, popup.menu)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.photo -> {
                        // Permission d'accès aux images
                        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }

                        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                            pickImageLauncher.launch("image/*")
                        } else {
                            requestPermissionLauncherCapture.launch(permission)
                        }
                        true
                    }

                    R.id.camera -> {
                        // Permission pour la caméra
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            takePictureLauncher.launch(photoUri)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        return v
    }

    fun gotofragment(frag: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragementC, frag)
            .commit()
    }
}
