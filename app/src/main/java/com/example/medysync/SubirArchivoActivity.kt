package com.example.medysync

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts

class SubirArchivoActivity : AppCompatActivity() {

    private lateinit var storageRef: StorageReference

    private lateinit var recyclerArchivos: RecyclerView
    private val listaArchivos = mutableListOf<Archivo>()
    private lateinit var adapter: ArchivosAdapter

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            subirArchivo(uri)
        } else {
            Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subir_archivo)

        val btnSeleccionarArchivo = findViewById<Button>(R.id.btnSeleccionarArchivo)
        storageRef = FirebaseStorage.getInstance().reference

        btnSeleccionarArchivo.setOnClickListener {
            pickFile.launch("*/*")
        }

        recyclerArchivos = findViewById(R.id.recyclerArchivos)
        adapter = ArchivosAdapter(listaArchivos,
            onDescargarClick = { archivo -> descargarArchivo(archivo) },
            onEliminarClick = { archivo -> eliminarArchivo(archivo) })

        recyclerArchivos.layoutManager = LinearLayoutManager(this)
        recyclerArchivos.adapter = adapter

        cargarArchivos()
    }

    private fun subirArchivo(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val nombreOriginal = obtenerNombreArchivo(uri)
        val fileName = System.currentTimeMillis().toString() + "_" + nombreOriginal
        val fileRef = storageRef.child("archivosUsuarios/$userId/$fileName")

        // Sube archivo (imagen o documento)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val archivoData = hashMapOf(
                        "nombre" to nombreOriginal,
                        "url" to downloadUrl.toString(),
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(userId)
                        .collection("archivos")
                        .add(archivoData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Archivo '$nombreOriginal' subido y guardado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Error al guardar en Firestore: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al obtener URL del archivo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir archivo: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun descargarArchivo(archivo: Archivo) {
        Toast.makeText(this, "Descargando archivo...", Toast.LENGTH_SHORT).show()
        try {
            val localFile = File.createTempFile("temp", obtenerExtension(archivo.nombre))
            val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(archivo.url)

            fileRef.getFile(localFile)
                .addOnSuccessListener {
                    val uri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        localFile
                    )
                    val mimeType = getMimeType(localFile.name)
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    startActivity(openIntent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al descargar archivo", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarArchivo(archivo: Archivo) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(archivo.url)
        fileRef.delete()
            .addOnSuccessListener {

                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(userId)
                    .collection("archivos")
                    .whereEqualTo("url", archivo.url)
                    .get()
                    .addOnSuccessListener { docs ->
                        for (doc in docs) {
                            doc.reference.delete()
                        }
                        Toast.makeText(this, "Archivo eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al eliminar archivo de Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar archivo de Firebase Storage", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"
        } else {
            "*/*"
        }
    }


    private fun obtenerExtension(nombre: String): String {
        return nombre.substringAfterLast('.', ".tmp")
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "archivo_desconocido"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nombreIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nombreIndex != -1) {
                nombre = cursor.getString(nombreIndex)
            }
        }
        return nombre
    }

    private fun cargarArchivos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .collection("archivos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar archivos: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    listaArchivos.clear()
                    for (doc in snapshot.documents) {
                        val archivo = doc.toObject(Archivo::class.java)
                        if (archivo != null) {
                            listaArchivos.add(archivo)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
