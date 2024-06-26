package com.mycompany.posmultimarca.Vistas.Main.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mycompany.posmultimarca.Vistas.Main.viewModels.MainViewModel
import com.mycompany.posmultimarca.ui.theme.POSMultiMarcaTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        mainViewModel.inicializarViewModel(context , context)
        setContent {
            POSMultiMarcaTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainView(mainViewModel)
                }
            }
        }
    }
}


@Preview
@Composable
fun MainView_Preview() {
    POSMultiMarcaTheme {
        Text("Serial:")

    }
}


@Composable
fun MainView(mainViewModel: MainViewModel) {
    POSMultiMarcaTheme {
        if(mainViewModel.showMenuAppsID) {
            Dialog(onDismissRequest = { /*TODO*/ }) {
                Surface(shape = MaterialTheme.shapes.medium) {
                    Column(
                        Modifier
                            .padding(4.dp)
                            .size(width = 300.dp, height = 250.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Seleccione Aplicación", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.size(20.dp))

                        for (appLabel in mainViewModel.listadoApp) {

                            Button(
                                modifier = Modifier.fillMaxWidth(),

                                onClick = {
                                    mainViewModel.onAPPSelectView(appLabel.first)
                                    mainViewModel.showMenuAppsID = false
                                })
                            {
                                Text( text = appLabel.second,)
                            }
                        }

                        Button( onClick = {
                            mainViewModel.onAPPSelectView(-1)
                            mainViewModel.showMenuAppsID = false

                        }) {
                            Text(text = "Cancelar")
                        }

                    }
                }
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Serial: ${mainViewModel.serial}")
            Text("Mensaje: ${mainViewModel.mensaje}")

            Button(
                onClick = { mainViewModel.initSale() })
            {
                Text("VENTA")
            }
        }

    }
}