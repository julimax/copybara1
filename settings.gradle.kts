// settings.gradle.kts
plugins {
    id("com.gradle.develocity") version "3.18.1"
}

develocity {
    buildScan {
        // Acepta Términos para scans.gradle.com (necesario en CI)
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")

        // Publicá siempre en CI; en local solo si pasás --scan
        publishing.onlyIf { System.getenv("CI") == "true" || gradle.startParameter.isBuildScan }

        // (Opcional) metadata de CI
        val repo = System.getenv("GITHUB_REPOSITORY")
        val runId = System.getenv("GITHUB_RUN_ID")
        if (repo != null && runId != null) {
            link("GitHub Actions Run", "https://github.com/$repo/actions/runs/$runId")
            tag(System.getenv("GITHUB_REF_NAME") ?: "local")
        }
    }

    // Si tenés servidor privado de Develocity, descomentá:
    // server.set("https://develocity.miempresa.com")
}

rootProject.name = "copybara"
