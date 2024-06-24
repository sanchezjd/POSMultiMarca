package com.mycompany.posmultimarca.Vistas.Main.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
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