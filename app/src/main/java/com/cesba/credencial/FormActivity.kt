package com.cesba.credencial

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class FormActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var btnFoto: MaterialButton
    private lateinit var spTipoPeriodo: Spinner
    private lateinit var spNumero: Spinner
    private lateinit var spTipoUsuario: Spinner

    private var pendingFile: File? = null    // foto que se está tomando
    private var confirmedFile: File? = null  // foto aceptada y mostrada

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && pendingFile != null) {
                confirmedFile?.delete()
                confirmedFile = pendingFile
                ivFoto.setImageBitmap(PhotoUtils.load(confirmedFile!!, 800))
                btnFoto.text = "Volver a tomar foto"
            } else {
                pendingFile?.delete()
            }
            pendingFile = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        ivFoto = findViewById(R.id.ivFoto)
        btnFoto = findViewById(R.id.btnFoto)
        spTipoPeriodo = findViewById(R.id.spTipoPeriodo)
        spNumero = findViewById(R.id.spNumero)
        spTipoUsuario = findViewById(R.id.spTipoUsuario)

        // Desplegable: semestre / cuatrimestre
        spTipoPeriodo.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Semestre", "Cuatrimestre")
        )
        updateNumbers("Semestre")
        spTipoPeriodo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                updateNumbers(spTipoPeriodo.selectedItem.toString())
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        spTipoUsuario.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Alumno", "Docente", "Administrativo", "Egresado")
        )

        btnFoto.setOnClickListener { launchCamera() }
        findViewById<MaterialButton>(R.id.btnGenerar).setOnClickListener { generate() }
    }

    private fun updateNumbers(tipo: String) {
        val max = if (tipo == "Semestre") 10 else 12
        val numbers = (1..max).map { "$it°" }
        spNumero.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, numbers
        )
    }

    private fun launchCamera() {
        val dir = File(cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "foto_${System.currentTimeMillis()}.jpg")
        pendingFile = file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        try {
            takePicture.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_LONG).show()
        }
    }

    private fun text(id: Int) =
        findViewById<TextInputEditText>(id).text.toString().trim()

    private fun err(tilId: Int, message: String?) {
        findViewById<TextInputLayout>(tilId).error = message
    }

    private fun generate() {
        val nombre = text(R.id.etNombre)
        val matricula = text(R.id.etMatricula)
        val carrera = text(R.id.etCarrera)
        val grupo = text(R.id.etGrupo)
        val correo = text(R.id.etCorreo)

        var ok = true
        fun check(cond: Boolean, til: Int, msg: String) {
            err(til, if (cond) null else msg)
            if (!cond) ok = false
        }
        check(nombre.isNotEmpty(), R.id.tilNombre, "Escribe tu nombre completo")
        check(matricula.isNotEmpty(), R.id.tilMatricula, "Escribe tu matrícula")
        check(carrera.isNotEmpty(), R.id.tilCarrera, "Escribe tu carrera")
        check(grupo.isNotEmpty(), R.id.tilGrupo, "Escribe tu grupo")
        check(Patterns.EMAIL_ADDRESS.matcher(correo).matches(), R.id.tilCorreo, "Correo no válido")

        if (confirmedFile == null) {
            Toast.makeText(this, "Primero toma tu foto", Toast.LENGTH_SHORT).show()
            ok = false
        }
        if (!ok) return

        val periodo = "${spNumero.selectedItem} ${spTipoPeriodo.selectedItem}"

        startActivity(Intent(this, CredentialActivity::class.java).apply {
            putExtra(CredentialActivity.EXTRA_NOMBRE, nombre)
            putExtra(CredentialActivity.EXTRA_MATRICULA, matricula)
            putExtra(CredentialActivity.EXTRA_CARRERA, carrera)
            putExtra(CredentialActivity.EXTRA_PERIODO, periodo)
            putExtra(CredentialActivity.EXTRA_GRUPO, grupo)
            putExtra(CredentialActivity.EXTRA_CORREO, correo)
            putExtra(CredentialActivity.EXTRA_TIPO, spTipoUsuario.selectedItem.toString())
            putExtra(CredentialActivity.EXTRA_FOTO, confirmedFile!!.absolutePath)
        })
    }
}