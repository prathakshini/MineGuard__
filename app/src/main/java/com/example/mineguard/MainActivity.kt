package com.example.mineguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mineguard.data.InspectionDatabase
import com.example.mineguard.model.Inspection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF101322)
private val Panel = Color(0xFF1B2034)
private val Purple = Color(0xFF9B7BFF)
private val Muted = Color(0xFFADB4C8)
private val White = Color(0xFFF7F7FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = InspectionDatabase(this)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Purple, background = Navy, surface = Panel,
                    onPrimary = Navy, onBackground = White, onSurface = White
                )
            ) {
                MineGuardApp(database)
            }
        }
    }
}

@Composable
private fun MineGuardApp(database: InspectionDatabase) {
    var page by remember { mutableStateOf("Dashboard") }
    var refresh by remember { mutableIntStateOf(0) }
    var selectedRole by remember { mutableStateOf("Inspector") }
    val inspections = remember(refresh) { database.getInspections() }

    Scaffold(
        containerColor = Navy,
        bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = White) {
                listOf("Dashboard", "New Inspection", "History", "Authority").forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = {
                            Icon(
                                when (item) {
                                    "Dashboard" -> Icons.Default.Dashboard
                                    "New Inspection" -> Icons.Default.AddCircle
                                    "History" -> Icons.Default.History
                                    else -> Icons.Default.Assessment
                                }, contentDescription = item
                            )
                        },
                        label = { Text(item, fontSize = 9.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)
        ) {
            Header()
            when (page) {
                "Dashboard" -> DashboardScreen(inspections, onNew = { page = "New Inspection" })
                "New Inspection" -> NewInspectionScreen(
                    onSave = { item ->
                        database.addInspection(item)
                        refresh++
                        page = "History"
                    }
                )
                "History" -> HistoryScreen(inspections)
                "Authority" -> AuthorityScreen(inspections, onUpdate = { id, status ->
                    database.updateStatus(id, status)
                    refresh++
                })
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = Purple.copy(alpha = 0.18f), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Shield, null, tint = Purple, modifier = Modifier.padding(12.dp).size(27.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("MINEGUARD", color = White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp)
            Text("AI  //  MINE SAFETY INTELLIGENCE", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Surface(color = Color(0xFF243B36), shape = RoundedCornerShape(50)) {
            Text("SYSTEM ONLINE", color = Color(0xFF75E0B2), fontSize = 8.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
        }
    }
}

@Composable
private fun DashboardScreen(items: List<Inspection>, onNew: () -> Unit) {
    val high = items.count { it.riskLevel == "High" }
    val medium = items.count { it.riskLevel == "Medium" }
    val open = items.count { it.status != "Closed" }
    Text("GOVERNANCE OVERVIEW", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Monitor mine inspections and compliance risks.", color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        StatCard("INSPECTIONS", items.size.toString(), Purple, Modifier.weight(1f))
        StatCard("HIGH RISK", high.toString(), Color(0xFFFF7474), Modifier.weight(1f))
        StatCard("OPEN", open.toString(), Color(0xFFFFC66D), Modifier.weight(1f))
    }
    Spacer(Modifier.height(18.dp))
    Text("RISK DISTRIBUTION", color = White, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    RiskBar("High", high, items.size, Color(0xFFFF7474))
    RiskBar("Medium", medium, items.size, Color(0xFFFFC66D))
    RiskBar("Low", items.count { it.riskLevel == "Low" }, items.size, Color(0xFF65D6A5))
    Spacer(Modifier.height(18.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("RECENT INSPECTIONS", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        TextButton(onClick = onNew) { Text("+ NEW", color = Purple) }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        items(items.take(4)) { InspectionCard(it) }
    }
}

@Composable
private fun StatCard(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = Panel, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Muted, fontSize = 8.sp)
            Text(value, color = tint, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RiskBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total == 0) 0f else count.toFloat() / total
    Column(Modifier.padding(vertical = 5.dp)) {
        Row {
            Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("$count", color = White, fontSize = 11.sp)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = Panel
        )
    }
}

@Composable
private fun InspectionCard(item: Inspection) {
    Surface(color = Panel, shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.mineName, color = White, fontWeight = FontWeight.SemiBold)
                Text("${item.category} • ${item.createdAt}", color = Muted, fontSize = 10.sp)
            }
            RiskChip(item.riskLevel)
        }
    }
}

@Composable
private fun RiskChip(level: String) {
    val color = when (level) {
        "High" -> Color(0xFFFF7474)
        "Medium" -> Color(0xFFFFC66D)
        else -> Color(0xFF65D6A5)
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(level.uppercase(), color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun NewInspectionScreen(onSave: (Inspection) -> Unit) {
    var mine by remember { mutableStateOf("Coal Mine Alpha") }
    var inspector by remember { mutableStateOf("Inspector") }
    var category by remember { mutableStateOf("PPE") }
    var notes by remember { mutableStateOf("") }
    var ppe by remember { mutableStateOf(false) }
    var equipment by remember { mutableStateOf(false) }
    var ventilation by remember { mutableStateOf(false) }
    var fire by remember { mutableStateOf(false) }
    var electrical by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val issueCount = listOf(ppe, equipment, ventilation, fire, electrical).count { it }
    val score = (issueCount * 20).coerceIn(0, 100)
    val level = when {
        score >= 60 -> "High"
        score >= 20 -> "Medium"
        else -> "Low"
    }

    Text("NEW INSPECTION", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Record observations and assess compliance.", color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(12.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
        item {
            InputField("Mine name", mine) { mine = it }
            InputField("Inspector name", inspector) { inspector = it }
            Text("Inspection category", color = Muted, fontSize = 11.sp)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(category, color = White); Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("PPE", "Equipment", "Ventilation", "Fire Safety", "Electrical").forEach {
                        DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("SAFETY CHECKLIST", color = White, fontWeight = FontWeight.Bold)
            Text("Tick each item when a safety issue is observed.", color = Muted, fontSize = 10.sp)
            CheckItem("PPE non-compliance observed", ppe) { ppe = it }
            CheckItem("Equipment issue observed", equipment) { equipment = it }
            CheckItem("Ventilation issue observed", ventilation) { ventilation = it }
            CheckItem("Fire safety issue observed", fire) { fire = it }
            CheckItem("Electrical issue observed", electrical) { electrical = it }
            InputField("Inspection notes", notes, singleLine = false) { notes = it }
            Surface(color = Panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("RULE-BASED RISK RESULT", color = Muted, fontSize = 9.sp)
                        Text("$score / 100", color = White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    }
                    RiskChip(level)
                }
            }
            if (message.isNotBlank()) {
                Text(message, color = Color(0xFF75E0B2), fontSize = 11.sp)
            }
            Button(
                onClick = {
                    if (mine.isBlank() || inspector.isBlank()) {
                        message = "Please enter the mine and inspector names."
                    } else {
                        val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                        val details = if (notes.isBlank()) "Checklist observations recorded." else notes
                        onSave(Inspection(
                            mineName = mine, inspectorName = inspector, category = category,
                            notes = details, riskScore = score, riskLevel = level, createdAt = date
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Text("SAVE INSPECTION", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun InputField(
    label: String, value: String, singleLine: Boolean = true, onValue: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValue, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = White, unfocusedTextColor = White,
            focusedBorderColor = Purple, unfocusedBorderColor = Muted,
            focusedLabelColor = Purple, unfocusedLabelColor = Muted
        )
    )
}

@Composable
private fun CheckItem(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onCheckedChange = onChecked, colors = CheckboxDefaults.colors(checkedColor = Purple))
        Text(label, color = White, fontSize = 12.sp)
    }
}

@Composable
private fun HistoryScreen(items: List<Inspection>) {
    Text("INSPECTION HISTORY", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("${items.size} saved inspection records", color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(12.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        items(items) { item ->
            Surface(color = Panel, shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.mineName, color = White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        RiskChip(item.riskLevel)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("${item.category} • ${item.inspectorName}", color = Muted, fontSize = 11.sp)
                    Text(item.notes, color = White.copy(alpha = 0.85f), fontSize = 11.sp)
                    Text("Score: ${item.riskScore}/100  •  ${item.createdAt}  •  ${item.status}",
                        color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun AuthorityScreen(items: List<Inspection>, onUpdate: (Long, String) -> Unit) {
    Text("GOVERNANCE CENTER", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Review risk records and update action status.", color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(12.dp))
    val openItems = items.filter { it.status != "Closed" }
    Text("${openItems.size} OPEN ITEMS", color = Purple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            Surface(color = Panel, shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.mineName, color = White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        RiskChip(item.riskLevel)
                    }
                    Text("${item.category} • ${item.status} • Score ${item.riskScore}", color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onUpdate(item.id, "In Progress") }, enabled = item.status != "Closed") {
                            Text("IN PROGRESS", fontSize = 9.sp)
                        }
                        Button(
                            onClick = { onUpdate(item.id, "Closed") },
                            enabled = item.status != "Closed",
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)
                        ) { Text("CLOSE", fontSize = 9.sp) }
                    }
                }
            }
        }
    }
}

