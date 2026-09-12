package com.aiterminal.app

import com.aiterminal.app.core.security.SecureKeyStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureKeyStoreTest {

    private lateinit var keyStore: SecureKeyStore
    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        keyStore = SecureKeyStore(context = null, customPrefs = fakePrefs)
    }

    @Test
    fun testSaveAndGetApiKey() {
        assertFalse(keyStore.hasApiKey("openai"))
        assertNull(keyStore.getApiKey("openai"))

        keyStore.saveApiKey("openai", "sk-proj-test123456")
        assertTrue(keyStore.hasApiKey("openai"))
        assertEquals("sk-proj-test123456", keyStore.getApiKey("openai"))
    }

    @Test
    fun testCaseInsensitiveProviderLookup() {
        keyStore.saveApiKey("Claude", "sk-ant-testkey")
        assertTrue(keyStore.hasApiKey("claude"))
        assertEquals("sk-ant-testkey", keyStore.getApiKey("CLAUDE"))
    }

    @Test
    fun testClearApiKey() {
        keyStore.saveApiKey("openai", "sk-test")
        keyStore.clearApiKey("openai")
        assertFalse(keyStore.hasApiKey("openai"))
        assertNull(keyStore.getApiKey("openai"))
    }
}
