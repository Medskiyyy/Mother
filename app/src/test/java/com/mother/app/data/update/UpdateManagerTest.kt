package com.mother.app.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun testVersionComparison() {
        // Newer versions
        assertTrue(UpdateManager.isNewerVersion("3.5.0", "3.6.0"))
        assertTrue(UpdateManager.isNewerVersion("3.5.0", "v3.6.0"))
        assertTrue(UpdateManager.isNewerVersion("3.5.0", "3.5.1"))
        assertTrue(UpdateManager.isNewerVersion("3.5.0", "4.0.0"))
        assertTrue(UpdateManager.isNewerVersion("3.4.1", "3.5.0"))

        // Same or older versions
        assertFalse(UpdateManager.isNewerVersion("3.5.0", "3.5.0"))
        assertFalse(UpdateManager.isNewerVersion("3.5.0", "v3.5.0"))
        assertFalse(UpdateManager.isNewerVersion("3.6.0", "3.5.0"))
        assertFalse(UpdateManager.isNewerVersion("3.5.1", "3.5.0"))
        assertFalse(UpdateManager.isNewerVersion("4.0.0", "3.5.0"))
    }

    @Test
    fun testParseGitHubReleaseJson() {
        val mockJson = """
            {
                "tag_name": "v3.6.0",
                "name": "Mother v3.6.0: Awesome New Features",
                "body": "- Fitur pengingat baru\n- Peningkatan dark mode",
                "html_url": "https://github.com/Medskiyyy/Mother/releases/tag/v3.6.0",
                "assets": [
                    {
                        "name": "Mother.apk",
                        "browser_download_url": "https://github.com/Medskiyyy/Mother/releases/download/v3.6.0/Mother.apk"
                    }
                ]
            }
        """.trimIndent()

        val release = UpdateManager.parseGitHubReleaseJson(mockJson)
        assertNotNull(release)
        assertEquals("3.6.0", release?.versionName)
        assertEquals("Mother v3.6.0: Awesome New Features", release?.releaseTitle)
        assertEquals("https://github.com/Medskiyyy/Mother/releases/download/v3.6.0/Mother.apk", release?.apkDownloadUrl)
        assertEquals("Mother.apk", release?.apkFileName)
        assertTrue(release?.releaseNotes?.contains("Fitur pengingat baru") == true)
    }
}
