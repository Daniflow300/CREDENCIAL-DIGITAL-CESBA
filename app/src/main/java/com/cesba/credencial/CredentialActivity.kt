package com.cesba.credencial

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CredentialActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOMBRE = "nombre"
        const val EXTRA_MATRICULA = "matricula"
        const val EXTRA_CARRERA = "carrera"
        const val EXTRA_PERIODO = "periodo"
        const val EXTRA_GRUPO = "grupo"
        const val EXTRA_CORREO = "correo"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_FOTO = "foto"
    }

    private var matricula = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credential)

        val nombre = intent.getStringExtra(EXTRA_NOMBRE).orEmpty()
        matricula = intent.getStringExtra(EXTRA_MATRICULA).orEmpty()
        val carrera = intent.getStringExtra(EXTRA_CARRERA).orEmpty()
        val periodo = intent.getStringExtra(EXTRA_PERIODO).orEmpty()
        val grupo = intent.getStringExtra(EXTRA_GRUPO).orEmpty()
        val correo = intent.getStringExtra(EXTRA_CORREO).orEmpty()
        val tipo = intent.getStringExtra(EXTRA_TIPO).orEmpty()
        val fotoPath = intent.getStringExtra(EXTRA_FOTO).orEmpty()

        findViewById<TextView>(R.id.tvNombre).text = nombre
        findViewById<TextView>(R.id.tvMatricula).text = "Matrícula: $matricula"
        findViewById<TextView>(R.id.tvCarrera).text = carrera
        findViewById<TextView>(R.id.tvPeriodo).text = periodo
        findViewById<TextView>(R.id.tvGrupo).text = "Grupo: $grupo"
        findViewById<TextView>(R.id.tvCorreo).text = correo
        findViewById<TextView>(R.id.tvTipo).text = tipo.uppercase(Locale.getDefault())

        if (fotoPath.isNotEmpty()) {
            findViewById<ImageView>(R.id.ivFotoCred)
                .setImageBitmap(PhotoUtils.load(File(fotoPath), 700))
        }

        // Contenido del QR (texto legible por cualquier lector)
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val qrContent = buildString {
            appendLine("CREDENCIAL DIGITAL CESBA")
            appendLine("Nombre: $nombre")
            appendLine("Matricula: $matricula")
            appendLine("Carrera: $carrera")
            appendLine("Grupo: $grupo")
            appendLine("Periodo: $periodo")
            append("Emitida: $fecha")
        }

        val qr = QrStyler.create(
            content = qrContent,
            sizePx = 800,
            blue = ContextCompat.getColor(this, R.color.cesba_blue),
            blueDark = ContextCompat.getColor(this, R.color.cesba_blue_dark),
            orange = ContextCompat.getColor(this, R.color.cesba_orange)
        )
        findViewById<ImageView>(R.id.ivQr).setImageBitmap(qr)

        findViewById<Button>(R.id.btnGuardar).setOnClickListener { saveToGallery() }
        findViewById<Button>(R.id.btnNueva).setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
    }

    private fun saveToGallery() {
        val card = findViewById<View>(R.id.credentialCard)
        val bitmap = Bitmap.createBitmap(card.width, card.height, Bitmap.Config.ARGB_8888)
        card.draw(Canvas(bitmap))

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Credencial_CESBA_$matricula.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CESBA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            Toast.makeText(this, "No se pudo guardar", Toast.LENGTH_SHORT).show()
            return
        }
        contentResolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(uri, values, null, null)

        Toast.makeText(this, "Credencial guardada en Galería (Pictures/CESBA)", Toast.LENGTH_LONG).show()
    }
}