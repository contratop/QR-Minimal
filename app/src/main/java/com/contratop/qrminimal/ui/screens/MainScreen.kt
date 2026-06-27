package com.contratop.qrminimal.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.contratop.qrminimal.scanner.BarcodeAnalyzer
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

@Composable
fun MainScreen(hasCameraPermission: Boolean) {
    var scannedBarcode by remember { mutableStateOf<Barcode?>(null) }

    if (scannedBarcode == null) {
        if (hasCameraPermission) {
            CameraScreen(onBarcodeScanned = { barcode ->
                scannedBarcode = barcode
            })
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Camera permission is required to scan QR codes.")
            }
        }
    } else {
        ResultScreen(
            barcode = scannedBarcode!!,
            onBack = { scannedBarcode = null }
        )
    }
}

@Composable
fun CameraScreen(onBarcodeScanned: (Barcode) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                BarcodeAnalyzer { barcode ->
                                    onBarcodeScanned(barcode)
                                }
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                        cameraControl = camera.cameraControl
                    } catch (exc: Exception) {
                        // Handle exceptions
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay for scanner
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .background(Color.Transparent)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            ) {}
        }
        
        // Flashlight button at the top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            IconButton(onClick = { 
                isFlashlightOn = !isFlashlightOn
                cameraControl?.enableTorch(isFlashlightOn)
            }) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Linterna",
                    tint = if (isFlashlightOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Apunta al código",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "El escaneo es automático",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(barcode: Barcode, onBack: () -> Unit) {
    val context = LocalContext.current
    val valueType = barcode.valueType
    val displayValue = barcode.displayValue ?: "Contenido desconocido"
    var showRickRollWarning by remember { mutableStateOf(displayValue.contains("dQw4w9WgXcQ")) }

    if (showRickRollWarning) {
        AlertDialog(
            onDismissRequest = { showRickRollWarning = false },
            title = { Text("⚠️ ¡Peligro de Rick Roll!") },
            text = { Text("Este enlace apunta al clásico videoclip de Rick Astley. ¡Estás a punto de ser Rickrolleado!") },
            confirmButton = {
                TextButton(onClick = { showRickRollWarning = false }) {
                    Text("Asumir el riesgo")
                }
            }
        )
    }

    val (typeText, typeIcon) = when (valueType) {
        Barcode.TYPE_URL -> "Enlace Web" to Icons.Default.Link
        Barcode.TYPE_WIFI -> "Red Wi-Fi" to Icons.Default.Wifi
        Barcode.TYPE_CONTACT_INFO -> "Contacto" to Icons.Default.Person
        Barcode.TYPE_EMAIL -> "Correo Electrónico" to Icons.Default.Email
        Barcode.TYPE_PHONE -> "Teléfono" to Icons.Default.Phone
        Barcode.TYPE_SMS -> "Mensaje SMS" to Icons.Default.Sms
        else -> "Texto Plano" to Icons.Default.TextSnippet
    }

    val details = mutableListOf<Pair<String, String>>()
    var primaryAction: (() -> Unit)? = null
    var primaryActionIcon: ImageVector = Icons.Default.ContentCopy
    var primaryActionText = "Copiar Contenido"

    when (valueType) {
        Barcode.TYPE_URL -> {
            details.add("URL" to (barcode.url?.url ?: displayValue))
            primaryAction = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(barcode.url?.url ?: displayValue)))
                } catch (e: Exception) {
                    Toast.makeText(context, "No se puede abrir el enlace", Toast.LENGTH_SHORT).show()
                }
            }
            primaryActionIcon = Icons.Default.OpenInBrowser
            primaryActionText = "Abrir Enlace"
        }
        Barcode.TYPE_WIFI -> {
            details.add("Nombre (SSID)" to (barcode.wifi?.ssid ?: ""))
            details.add("Contraseña" to (barcode.wifi?.password ?: "Sin contraseña"))
            val encryption = when (barcode.wifi?.encryptionType) {
                Barcode.WiFi.TYPE_WEP -> "WEP"
                Barcode.WiFi.TYPE_WPA -> "WPA/WPA2"
                Barcode.WiFi.TYPE_OPEN -> "Abierta"
                else -> "Desconocida"
            }
            details.add("Seguridad" to encryption)
            
            primaryAction = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Wifi Password", barcode.wifi?.password ?: ""))
                Toast.makeText(context, "Contraseña copiada", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
            }
            primaryActionIcon = Icons.Default.Wifi
            primaryActionText = "Copiar y Conectar"
        }
        Barcode.TYPE_EMAIL -> {
            details.add("Para" to (barcode.email?.address ?: ""))
            if (!barcode.email?.subject.isNullOrEmpty()) details.add("Asunto" to (barcode.email?.subject!!))
            if (!barcode.email?.body.isNullOrEmpty()) details.add("Cuerpo" to (barcode.email?.body!!))
            
            primaryAction = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${barcode.email?.address}")
                    putExtra(Intent.EXTRA_SUBJECT, barcode.email?.subject)
                    putExtra(Intent.EXTRA_TEXT, barcode.email?.body)
                }
                context.startActivity(intent)
            }
            primaryActionIcon = Icons.Default.Email
            primaryActionText = "Escribir Correo"
        }
        Barcode.TYPE_PHONE -> {
            details.add("Número" to (barcode.phone?.number ?: ""))
            primaryAction = {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${barcode.phone?.number}")))
            }
            primaryActionIcon = Icons.Default.Phone
            primaryActionText = "Llamar"
        }
        Barcode.TYPE_SMS -> {
            details.add("Número" to (barcode.sms?.phoneNumber ?: ""))
            if (!barcode.sms?.message.isNullOrEmpty()) details.add("Mensaje" to (barcode.sms?.message!!))
            primaryAction = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${barcode.sms?.phoneNumber}")).apply {
                    putExtra("sms_body", barcode.sms?.message)
                }
                context.startActivity(intent)
            }
            primaryActionIcon = Icons.Default.Sms
            primaryActionText = "Enviar SMS"
        }
        Barcode.TYPE_CONTACT_INFO -> {
            val contact = barcode.contactInfo
            details.add("Nombre" to (contact?.name?.formattedName ?: ""))
            contact?.phones?.firstOrNull()?.number?.let { details.add("Teléfono" to it) }
            contact?.emails?.firstOrNull()?.address?.let { details.add("Email" to it) }
            contact?.organization?.let { details.add("Empresa" to it) }
            contact?.urls?.firstOrNull()?.let { details.add("Sitio web" to it) }
            
            primaryAction = {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    type = ContactsContract.Contacts.CONTENT_TYPE
                    putExtra(ContactsContract.Intents.Insert.NAME, contact?.name?.formattedName)
                    putExtra(ContactsContract.Intents.Insert.PHONE, contact?.phones?.firstOrNull()?.number)
                    putExtra(ContactsContract.Intents.Insert.EMAIL, contact?.emails?.firstOrNull()?.address)
                    putExtra(ContactsContract.Intents.Insert.COMPANY, contact?.organization)
                }
                context.startActivity(intent)
            }
            primaryActionIcon = Icons.Default.PersonAdd
            primaryActionText = "Guardar Contacto"
        }
        else -> {
            details.add("Contenido" to displayValue)
            primaryAction = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("QR Content", displayValue))
                Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
            }
            primaryActionIcon = Icons.Default.ContentCopy
            primaryActionText = "Copiar Texto"
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Volver")
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al escáner", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Icon Header
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = typeText,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = typeText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    details.forEachIndexed { index, pair ->
                        if (pair.second.isNotEmpty()) {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pair.second,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (index < details.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            FilledTonalButton(
                onClick = { primaryAction?.invoke() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(primaryActionIcon, contentDescription = primaryActionText)
                Spacer(Modifier.width(12.dp))
                Text(primaryActionText, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Secondary copy action just in case primary isn't copy
                if (valueType != Barcode.TYPE_TEXT && valueType != Barcode.TYPE_URL) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("QR Content", displayValue))
                            Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
                        Spacer(Modifier.width(8.dp))
                        Text("Copiar Todo")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, displayValue)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir")
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir")
                }
            }
        }
    }
}
