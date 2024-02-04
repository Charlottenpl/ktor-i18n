import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.FileDialog
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var base by remember { mutableStateOf("") }
    var checkText by remember { mutableStateOf("Hello, World!") }
    var readType by remember { mutableStateOf(1) }
    var writeType by remember { mutableStateOf(2) }
    var isCheck by remember { mutableStateOf(true) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Row {
                Button(onClick = {
                    val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
                    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    val result = fileChooser.showOpenDialog(null)

                    base = if (result == JFileChooser.APPROVE_OPTION) {
                        fileChooser.selectedFile.path
                    } else {
                        ""
                    }
                }) {
                    Text("pick file...")
                }
            }
            Row() {
                TextField(
                    value = base,
                    onValueChange = {
                        checkText = "🥳"
                        base = it
                    },
                    label = { Text("请选择父目录地址") },
                    modifier = Modifier.padding()
                )
//                Text(checkText)
            }

            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }
        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
