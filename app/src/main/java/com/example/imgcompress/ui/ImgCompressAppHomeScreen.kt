/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.imgcompress.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.imgcompress.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImgCompressAppHomeScreen(
    modifier: Modifier = Modifier,
) {
    var isPercentSize by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(0.0) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var result by remember { mutableStateOf<CompressedImageResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Compressor") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UploadImageRow(onImageSelected = { uri -> selectedImageUri = uri })

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PercentageSizeSwitchRow(
                        modifier = Modifier.fillMaxWidth(),
                        onPercentSizeChanged = { isPercentSize = it },
                        isPercentSize = isPercentSize
                    )
                    EditNumberField(
                        leadingIcon = R.drawable.ic_launcher_foreground,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        label = if (isPercentSize) R.string.input_percent else R.string.input_mb,
                        initValue = size,
                        modifier = Modifier.fillMaxWidth(),
                        isPercentSize = isPercentSize,
                        onValueChanged = { updatedValue -> size = updatedValue }
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        selectedImageUri?.let {
                            result = CompressImage(
                                context = context,
                                isPercentSize = isPercentSize,
                                size = size,
                                selectedImageUri = it
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Compress Photo")
            }

            CompressionPopup(result = result, onDismiss = { result = null })
        }
    }
}

data class CompressedImageResult(
    val uri: Uri,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long
)

suspend fun CompressImage(
    context: Context,
    isPercentSize: Boolean,
    size: Double, // if MB or percent
    selectedImageUri: Uri
): CompressedImageResult? = withContext(Dispatchers.IO) {
    try {
        // Get original file size
        val fd = context.contentResolver.openFileDescriptor(selectedImageUri, "r")
        val originalSize = fd?.statSize ?: 0L
        fd?.close()

        // Decode bitmap
        val inputStream = context.contentResolver.openInputStream(selectedImageUri)
        var bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
        inputStream?.close()

        // Target bytes
        val targetBytes: Long = if (isPercentSize) {
            (originalSize * (size / 100.0)).toLong()
        } else {
            (size * 1024 * 1024).toLong()
        }

        val outputStream = ByteArrayOutputStream()
        var quality = 100

        // Compression + downscale loop
        while (true) {
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            if (outputStream.size() <= targetBytes || (quality <= 5 && bitmap.width <= 100)) {
                break // either fits or cannot compress/rescale further
            }

            if (quality > 5) {
                quality -= 5 // reduce quality first
            } else {
                // Reduce resolution by 90%
                val newWidth = (bitmap.width * 0.9).toInt()
                val newHeight = (bitmap.height * 0.9).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                quality = 100 // reset quality when resizing
            }
        }

        // Save compressed image into MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "compressed_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Compressed")
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return@withContext null

        var finalSize = 0L
        context.contentResolver.openOutputStream(uri)?.use { fileOut ->
            val data = outputStream.toByteArray()
            fileOut.write(data)
            fileOut.flush()
            finalSize = data.size.toLong()
        }

        return@withContext CompressedImageResult(uri, originalSize, finalSize)
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}


@Composable
fun EditNumberField(
    @DrawableRes leadingIcon: Int,
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    initValue: Double,
    isPercentSize: Boolean,
    onValueChanged: (Double) -> Unit
) {
    var value by remember { mutableStateOf(initValue.toString()) }

    TextField(
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null
            )
        },
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                val parsed = newValue.toDoubleOrNull()
                val isValid = when {
                    parsed == null -> newValue.isEmpty()
                    isPercentSize -> parsed in 1.0..100.0
                    else -> true
                }

                if (isValid) {
                    value = newValue
                    parsed?.let { onValueChanged(it) }
                }
            }
        },
        modifier = modifier,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            Text(if (isPercentSize) "%" else "MB")
        }
    )
}

@Composable
fun PercentageSizeSwitchRow(
    modifier: Modifier = Modifier,
    onPercentSizeChanged: (Boolean) -> Unit,
    isPercentSize: Boolean
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.percent_size_flat_size))
        Switch(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = isPercentSize,
            onCheckedChange = onPercentSizeChanged,
        )
    }
}

@Composable
fun UploadImageRow(
    modifier: Modifier = Modifier,
    onImageSelected: (Uri) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let { onImageSelected(it) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Choose Photo")
        }

        imageUri?.let {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.size(200.dp)
            ) {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CompressionPopup(result: CompressedImageResult?, onDismiss: () -> Unit) {
    if (result != null) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            title = { Text("Photo Saved ✅") },
            text = {
                Text(
                    "Original size: ${"%.2f".format(result.originalSizeBytes / 1024.0)} KB\n" +
                            "Compressed size: ${"%.2f".format(result.compressedSizeBytes / 1024.0)} KB"
                )
            }
        )
    }
}
