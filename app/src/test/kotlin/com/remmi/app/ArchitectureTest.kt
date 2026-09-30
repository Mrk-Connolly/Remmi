package com.remmi.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Deterministic Architecture Contract verification test.
 * Verifies package import boundaries between core/, ui/, and plugins/.
 */
class ArchitectureTest {

    private fun findSourceRoot(): File {
        val userDir = File(System.getProperty("user.dir") ?: ".")
        val candidate1 = File(userDir, "src/main/kotlin/com/remmi/app")
        if (candidate1.exists()) return candidate1
        val candidate2 = File(userDir, "app/src/main/kotlin/com/remmi/app")
        if (candidate2.exists()) return candidate2
        throw IllegalStateException("Source directory com/remmi/app not found from ${userDir.absolutePath}")
    }

    @Test
    fun verifyPluginsDoNotImportRoomDirectly() {
        val root = findSourceRoot()
        val pluginsDir = File(root, "plugins")
        if (!pluginsDir.exists()) return

        val violations = mutableListOf<String>()

        pluginsDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import androidx.room.") ||
                        trimmed.startsWith("import com.remmi.app.core.database.room.")
                    ) {
                        violations.add("${file.name}:${index + 1}: $trimmed")
                    }
                }
            }

        assertTrue(
            "Plugins must not directly import Room DAOs, entities, or Room annotations.\nViolations found:\n" +
                    violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun verifyCoreAndroidDoesNotDependOnUi() {
        val root = findSourceRoot()
        val coreAndroidDir = File(root, "core/android")
        if (!coreAndroidDir.exists()) return

        val violations = mutableListOf<String>()

        coreAndroidDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import com.remmi.app.ui.")) {
                        violations.add("${file.name}:${index + 1}: $trimmed")
                    }
                }
            }

        assertTrue(
            "core/android system capability services must not import UI components.\nViolations found:\n" +
                    violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun verifyUiDoesNotImportRoomDirectly() {
        val root = findSourceRoot()
        val uiDir = File(root, "ui")
        if (!uiDir.exists()) return

        val violations = mutableListOf<String>()

        uiDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("import androidx.room.") ||
                        trimmed.startsWith("import com.remmi.app.core.database.room.")
                    ) {
                        violations.add("${file.name}:${index + 1}: $trimmed")
                    }
                }
            }

        assertTrue(
            "UI components must not directly import Room DAOs or entities.\nViolations found:\n" +
                    violations.joinToString("\n"),
            violations.isEmpty()
        )
    }
}
