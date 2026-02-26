package com.duelmath.features.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TextGray = Color(0xFF94A3B8)

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF4F46E5))),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("∑", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Math Duel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = title, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        Text(text = subtitle, fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp))
    }
}