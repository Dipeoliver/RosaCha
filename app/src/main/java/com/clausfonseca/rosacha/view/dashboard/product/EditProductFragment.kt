package com.clausfonseca.rosacha.view.dashboard.product

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.br.jafapps.bdfirestore.util.Util
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentProductEditBinding
import com.clausfonseca.rosacha.databinding.FragmentProductListBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class EditProductFragment : Fragment() {
    private lateinit var binding: FragmentProductEditBinding

    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseStorage = Firebase.storage

        binding.btnDowload.setOnClickListener {
            getImage()
        }

    }

    private fun getImage() {
        val reference =
            firebaseStorage.reference
                .child("Products")
                .child("78911222.jpg")
        reference.downloadUrl.addOnSuccessListener { task ->
            val urlImage = task.toString()
//            product.urlImagem = urlImage
            Glide.with(requireContext()).asBitmap().load(urlImage)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), "Erro ao carregar a imagem: ${e.toString()}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Util.exibirToast(requireContext(), "Imagem carregada com Sucesso")
                        return false
                    }
                }).into(binding.imgDown)

        }.addOnFailureListener { error ->
            Util.exibirToast(requireContext(), "Erro ao carregar imagem ${error.message.toString()}")
        }
    }
}