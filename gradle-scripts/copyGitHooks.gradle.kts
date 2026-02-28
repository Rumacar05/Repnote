import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

tasks.register("copyGitHooks") {
    doLast {
        val sourceDir = file("hooks")
        val targetDir = file(".git/hooks")
        sourceDir.listFiles()
            ?.forEach { sourceFile ->
                val targetFile = File(targetDir, sourceFile.name)
                if (!targetFile.exists() ||
                    Files.mismatch(sourceFile.toPath(), targetFile.toPath()) != -1L
                ) {
                    Files.copy(
                        sourceFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                    print("> Copied hook: ${sourceFile.name}")
                }
            }
    }
    doLast {
        file(".git/hooks/").walk()
            .forEach { file ->
                if (file.isFile) {
                    try {
                        Files.setPosixFilePermissions(
                            file.toPath(),
                            PosixFilePermissions.fromString("rwxr-xr-x"),
                        )
                    } catch (_: UnsupportedOperationException) {
                        logger.warn("Unable to set POSIX permissions on ${file.name}.")
                    }
                }
            }
    }
}
