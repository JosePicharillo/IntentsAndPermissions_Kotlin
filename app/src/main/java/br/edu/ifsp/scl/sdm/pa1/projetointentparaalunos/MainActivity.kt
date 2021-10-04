package br.edu.ifsp.scl.sdm.pa1.projetointentparaalunos

import android.Manifest
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.pa1.projetointentparaalunos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var requisicaoPermissoesActivityLaucher: ActivityResultLauncher<String>
    private lateinit var selecionarImagemActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var escolherAplicativoActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        //Referência a ToolBar pela Main
        with(activityMainBinding.mainTb.appTb) {
            title = "Tratando Intents"
            subtitle = "Principais tipos"
        }

        // Usando Toolbar como ActionBar da Activity
        setSupportActionBar(activityMainBinding.mainTb.appTb)

        requisicaoPermissoesActivityLaucher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
                if (!concedida) {
                    //Requisitar permissão
                    requisitarPermissaoLigacao()
                } else {
                    discagemTelefone()
                }
            }

        selecionarImagemActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
                visualizarImagem(resultado)
            }

        escolherAplicativoActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
                visualizarImagem(resultado)
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.viewMi -> {
                val url = activityMainBinding.parameterEt.text.toString().let {
                    if (!it.contains("http[s]?".toRegex())) "http://${it}" else it
                }
                val siteIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(siteIntent)
                true
            }
            R.id.callMi -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // Requisitar a permissão
                        requisitarPermissaoLigacao()
                    } else {
                        // Executa a discagem
                        discagemTelefone()
                    }
                } else {
                    //Executa a discagem
                    discagemTelefone()
                }
                true
            }
            R.id.dialMi -> {
                val ligacaoIntent = Intent(Intent.ACTION_DIAL)
                ligacaoIntent.data = Uri.parse("tel: ${activityMainBinding.parameterEt.text}")
                startActivity(ligacaoIntent)
                true
            }
            R.id.pickMi -> {
                val pegarImagem = pegarImagemIntent()
                selecionarImagemActivityResultLauncher.launch(pegarImagem)
                true
            }
            R.id.chooserMi -> {
                val escolherAplicativoIntent = Intent(ACTION_CHOOSER)
                escolherAplicativoIntent.putExtra(EXTRA_TITLE, "Escolha um aplicativo para imagens")
                escolherAplicativoIntent.putExtra(EXTRA_INTENT, pegarImagemIntent())
                escolherAplicativoActivityLauncher.launch(escolherAplicativoIntent)
                true
            }
            R.id.exitMi -> {
                finish()
                true
            }
            R.id.actionMi -> {
                // Abrindo outra activity usando uma Intent Action
                val actionIntent = Intent("OPEN_ACTION_ACTIVITY").putExtra(
                    Intent.EXTRA_TEXT,
                    activityMainBinding.parameterEt.text.toString()
                )
                startActivity(actionIntent)
                true
            }
            else -> {
                false
            }

        }
    }

    private fun visualizarImagem(resultado: ActivityResult) {
        if (resultado.resultCode == RESULT_OK) {
            val referenciaImagem =
                resultado.data?.data //Caminho para onde a imagem vai ser armazenada
            val visualizarImagem = Intent(ACTION_VIEW, referenciaImagem)
            startActivity(visualizarImagem)
        }
    }

    private fun pegarImagemIntent(): Intent {
        val pegarImagemIntent = Intent(ACTION_PICK)
        val diretorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path
        pegarImagemIntent.setDataAndType(Uri.parse(diretorio), "image/*")
        return pegarImagemIntent
    }

    private fun discagemTelefone() {
        val discagemIntent = Intent()
        discagemIntent.action = Intent.ACTION_CALL
        discagemIntent.data = Uri.parse("tel: ${activityMainBinding.parameterEt.text}")
        startActivity(discagemIntent)
    }

    private fun requisitarPermissaoLigacao() {
        requisicaoPermissoesActivityLaucher.launch(Manifest.permission.CALL_PHONE)
    }

}