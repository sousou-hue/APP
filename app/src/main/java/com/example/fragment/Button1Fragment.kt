package com.example.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import java.io.File

class Button1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_button1, container, false)

        val editbutton: Button = v.findViewById(R.id.editProfileButton)
        val nomVIEW = v.findViewById<TextView>(R.id.userNameText)
        val emailVIEW = v.findViewById<TextView>(R.id.userEmailText)
        val imageVIEW = v.findViewById<ImageView>(R.id.profileImageView)

        val prefs = requireContext().getSharedPreferences("fragmentapp", Context.MODE_PRIVATE)
        val nom = prefs.getString("name", "rien")
        val email = prefs.getString("email", "rien")


        nomVIEW.text = nom
        emailVIEW.text = email
        // ✅ recuperer la photo depuis le fichier
        val photoFile = File(requireContext().filesDir, "photo.jpg")
        if (photoFile.exists()) {
            val imageUri: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
            imageVIEW.setImageURI(imageUri)
        }
        editbutton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragementC, Button2Fragment())
                .commit()
        }
        return v
    }

}
