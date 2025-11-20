package com.magnite.demo.compose

import android.content.Context
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NativeAdLayout(
    title: String = "",
    description: String = "",
    rating: Float? = null,
    category: String? = null,
    callToAction: String? = null,
    bitmap: ImageBitmap? = null,
    factory: (Context) -> View,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // NOTE extremely important to specify .height(IntrinsicSize.Max) in order to make the native ad clickable
            .height(IntrinsicSize.Max)
            .background(color = Color(0xffefefef))
    ) {
        Row {
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(160.dp)
                        .padding(16.dp),
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Ad image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (bitmap != null) 0.dp else 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp,
                    ),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )

                rating?.let {
                    Text(
                        text = "Rating: %.1f⭐".format(it),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                category?.let {
                    Text(
                        text = "Category: %s".format(it),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (callToAction?.isNotBlank() ?: false) {
                    // NOTE onClick is configured inside AndroidView after the registerViewForInteraction()
                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { /* do nothing */ },
                    ) {
                        Text(callToAction)
                    }
                }
            }
        }

        Text(
            text = "Ad",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .border(1.dp, Color(0xff888888))
                .padding(horizontal = 8.dp, vertical = 1.dp),
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = factory,
        )
    }
}
