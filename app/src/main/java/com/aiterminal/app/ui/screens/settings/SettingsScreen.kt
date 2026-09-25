package com.aiterminal.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiterminal.app.ui.theme.Void
import com.aiterminal.app.ui.theme.ThreadIndigo
import com.aiterminal.app.ui.theme.HairlineLow
import com.aiterminal.app.ui.theme.SignalEmerald
import com.aiterminal.app.ui.theme.SignalEmeraldBg
import com.aiterminal.app.ui.theme.GraphiteHigh
import com.aiterminal.app.ui.theme.GlassFillLow
import com.aiterminal.app.ui.theme.CardShape
import com.aiterminal.app.ui.theme.GradientText
import com.aiterminal.app.ui.theme.PillShape
import com.aiterminal.app.ui.theme.glassSurface
import com.aiterminal.app.ui.theme.TextMuted
import com.aiterminal.app.ui.theme.TextPrimary
import com.aiterminal.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val routingConfig by viewModel.routingConfig.collectAsState()
    val openAiKey by viewModel.openAiKey.collectAsState()
    val claudeKey by viewModel.claudeKey.collectAsState()
    val savedMessage by viewModel.savedMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Void)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        GradientText(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "BYOK (Bring Your Own Key) & Runtime Model Routing",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Success Alert
        if (savedMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GraphiteHigh)
                    .border(1.dp, SignalEmerald, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SignalEmerald,
                    modifier = Modifier.width(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = savedMessage ?: "",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Provider Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassFillLow),
            shape = CardShape
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = ThreadIndigo
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Model Provider",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                routingConfig.providers.forEach { (providerId, providerConfig) ->
                    val isSelected = routingConfig.activeProvider == providerId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectProvider(providerId) },
                            colors = RadioButtonDefaults.colors(selectedColor = ThreadIndigo)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = providerConfig.name,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Model: ${providerConfig.model}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BYOK API Keys
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassFillLow),
            shape = CardShape
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = SignalEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BYOK API Keys",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Encrypted in Android Keystore. Never sent anywhere except direct provider APIs.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // OpenAI Key Field
                Text(
                    text = "OpenAI API Key (GPT-4o / GPT-4o-mini)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = openAiKey,
                    onValueChange = { viewModel.onOpenAiKeyChanged(it) },
                    placeholder = { Text("sk-...", fontSize = 13.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Void,
                        unfocusedContainerColor = Void,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ThreadIndigo,
                        focusedIndicatorColor = ThreadIndigo,
                        unfocusedIndicatorColor = HairlineLow
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Claude Key Field
                Text(
                    text = "Anthropic API Key (Claude 3.5 Sonnet)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = claudeKey,
                    onValueChange = { viewModel.onClaudeKeyChanged(it) },
                    placeholder = { Text("sk-ant-...", fontSize = 13.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Void,
                        unfocusedContainerColor = Void,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ThreadIndigo,
                        focusedIndicatorColor = ThreadIndigo,
                        unfocusedIndicatorColor = HairlineLow
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveKeys() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ThreadIndigo)
                ) {
                    Text(
                        text = "Save Keys to Keystore",
                        color = Void,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
