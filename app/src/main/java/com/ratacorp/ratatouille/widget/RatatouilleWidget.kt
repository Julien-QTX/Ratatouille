package com.ratacorp.ratatouille.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ratacorp.ratatouille.MainActivity
import com.ratacorp.ratatouille.RatatouilleApplication
import kotlinx.coroutines.flow.firstOrNull
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.glance.layout.Box
import androidx.glance.appwidget.cornerRadius

class RatatouilleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RatatouilleWidget()
}

class RatatouilleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as RatatouilleApplication
        val repository = app.container.productRepository
        
        // On récupère le produit le plus récent (historique)
        val history = repository.getAllProducts().firstOrNull()
        val latestProduct = history?.firstOrNull()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFFF0F0F0)))
                    .padding(16.dp)
                    .clickable(onClick = actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (latestProduct != null) {
                    Text(
                        text = "Dernier scan",
                        style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = latestProduct.productName ?: "Inconnu",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorProvider(Color.Black)),
                        maxLines = 2
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    
                    // Simple NutriScore representation for Glance
                    val nsColor = when (latestProduct.nutritionGrades?.lowercase()) {
                        "a" -> Color(0xFF038141)
                        "b" -> Color(0xFF85BB2F)
                        "c" -> Color(0xFFFECB02)
                        "d" -> Color(0xFFEE8100)
                        "e" -> Color(0xFFE63E11)
                        else -> Color.Gray
                    }
                    val nsLabel = latestProduct.nutritionGrades?.uppercase() ?: "?"
                    
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(ColorProvider(nsColor))
                            .cornerRadius(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nsLabel,
                            style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )
                    }
                    
                } else {
                    Text(
                        text = "Aucun scan récent",
                        style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 14.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Appuyez pour scanner",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorProvider(Color.Black))
                    )
                }
            }
        }
    }
}
